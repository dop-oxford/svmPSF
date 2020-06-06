/*
This file is part of Free spatially-variant PSF modelling software (svmPSF).

svmPSF is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

svmPSF is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with svmPSF.  If not, see <http://www.gnu.org/licenses/>
 */

package dop.svPSFmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.analysis.interpolation.BicubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Class for generation of coefficient maps for the eigen-PSF model
 **/

public class PCA_coefficient {

	/** get coefficient for each eigen-vectors **/
	private static void add_input_PCA_coeff(RealVector param,  Point centre, RealVector param_means, EigenDecomposition eig, List<Coefficient> PCA_input_coeff)
	{
		int no_param = param_means.getDimension();
		Coefficient coeff = new Coefficient(no_param);
		coeff.x = centre.getIntPosition(0);
		coeff.y = centre.getIntPosition(1);

		if(param.isNaN())
			for(int i=0; i<no_param; i++)
				coeff.coefficient[i]=0;
		else
		{
			param = param.subtract(param_means);
			for(int i=0; i<no_param; i++)
				coeff.coefficient[i]=param.dotProduct(eig.getEigenvector(i));
		}

		PCA_input_coeff.add(coeff);
	}

	/** calculate coefficient everywhere by interpolation **/
	private static void calculate_PCA_coeff_maps(RealMatrix[] PCA_coeff, List<Coefficient> PCA_input_coeff)
	{
		int no_comp = PCA_coeff.length;
		ArrayList<Integer> x_list = new ArrayList<>(), y_list = new ArrayList<>();
		Integer[] xval= new Integer[1], yval=new Integer[1];
		int x,y,size_x,size_y,x_min,x_max,y_min,y_max;
		int i,j,k; //index
		boolean is_in_list;


		//Create an array of xval and yval used in bicubic interpolation
		for(Coefficient inp_coeff:PCA_input_coeff)
		{
			size_x = x_list.size();
			size_y = y_list.size();
			x = inp_coeff.x;
			y = inp_coeff.y;
			i=0; is_in_list=false;
			while(i<size_x)
			{
				if(x==x_list.get(i))
				{
					is_in_list=true;
					break;
				}
				if(x<x_list.get(i))
					break;
				i++;
			}
			if(!is_in_list)
				x_list.add(i, x);

			i=0; is_in_list=false;
			while(i<size_y)
			{
				if(y==y_list.get(i))
				{
					is_in_list=true;
					break;
				}
				if(y<y_list.get(i))
					break;
				i++;
			}
			if(!is_in_list)
				y_list.add(i, y);
		}
		xval = x_list.toArray(xval);
		yval = y_list.toArray(yval);

		//Generate the f(x,y) sample matrix
		size_x = xval.length;
		size_y = yval.length;
		double[] xval_d = new double[size_x], yval_d = new double[size_y];
		double[][][] fval = new double[no_comp][size_x][size_y];
		for(Coefficient inp_coeff:PCA_input_coeff)
		{
			for(i=0;i<size_x;i++)
				if(xval[i]==inp_coeff.x)
					break;

			for(j=0;j<size_y;j++)
				if(yval[j]==inp_coeff.y)
					break;

			for(k=0;k<no_comp;k++)
				fval[k][i][j]=inp_coeff.coefficient[k];
		}

		BicubicInterpolatingFunction bif = null;
		BicubicInterpolator bi = new BicubicInterpolator();

		//Conversion from Integer to double
		for(i=0;i<size_x;i++)
			xval_d[i]=xval[i].doubleValue();
		for(j=0;j<size_y;j++)
			yval_d[j]=yval[j].doubleValue();

		x_min = xval[0];
		y_min = yval[0];
		x_max = xval[size_x-1];
		y_max = yval[size_y-1];
		for(k=0;k<no_comp;k++)
		{
			bif = bi.interpolate(xval_d,yval_d,fval[k]);
			for(i=x_min;i<=x_max;i++)
				for(j=y_min;j<=y_max;j++)
					PCA_coeff[k].setEntry(j, i, bif.value(i, j));
		}

		extrapolate_coeff_grid(PCA_coeff,x_min+1,x_max-1,y_min+1,y_max-1);
	}

	/** extrapolate coefficient outside the grid **/
	private static void extrapolate_coeff_grid (RealMatrix[] PCA_coeff, int x_min, int x_max, int y_min, int y_max)
	{
		int no_out_x = PCA_coeff[0].getColumnDimension();  //No of pixels in x direction
		int no_out_y = PCA_coeff[0].getRowDimension();    //No of pixels in y direction
		int no_comp = PCA_coeff.length;        //No of components
		int xc = no_out_x/2, yc = no_out_y/2;  //centre coordinates
		int x1,y1,k; //indexes
		Double angle, x_margin, y_margin;
		int x_mar_int, y_mar_int;

		//Up
		for(y1=0;y1<y_min;y1++)
			for(x1=y1;x1<=no_out_y-y1-1;x1++)
			{
				angle = Math.atan2(x1-xc, yc-y1); //Angle is measured from Oy+ axis and positive in the CW direction
				x_margin = xc + Math.tan(angle)*(yc-y_min); //The x coord of the pixel at the same angle.
				x_margin = Math.rint(x_margin); //Round to the nearest integer
				x_mar_int = x_margin.intValue(); //Change from Double to int
				for(k=0;k<no_comp;k++)
					PCA_coeff[k].setEntry(y1, x1, PCA_coeff[k].getEntry(y_min, x_mar_int));
			}

		//Down
		for(y1=y_max+1;y1<no_out_y;y1++)
			for(x1=no_out_y-y1-1;x1<=y1;x1++)
			{
				angle = Math.atan2(x1-xc, y1-yc); //Angle is measured from Oy- axis and positive in the CCW direction
				x_margin = xc + Math.tan(angle)*(y_max-yc); //The x coord of the pixel at the same angle.
				x_margin = Math.rint(x_margin); //Round to the nearest integer
				x_mar_int = x_margin.intValue(); //Change from Double to int
				for(k=0;k<no_comp;k++)
					PCA_coeff[k].setEntry(y1, x1, PCA_coeff[k].getEntry(y_max, x_mar_int));
			}

		//Left
		for(x1=0;x1<x_min;x1++)
			for(y1=x1+1;y1<no_out_x-x1-1;y1++)
			{
				angle = Math.atan2(y1-yc, xc-x1); //Angle is measured from Ox- axis and positive in the CW direction
				y_margin = yc + Math.tan(angle)*(xc-x_min); //The x coord of the pixel at the same angle.
				y_margin = Math.rint(y_margin); //Round to the nearest integer
				y_mar_int = y_margin.intValue(); //Change from Double to int
				for(k=0;k<no_comp;k++)
					PCA_coeff[k].setEntry(y1, x1, PCA_coeff[k].getEntry(y_mar_int, x_min));
			}

		//Right
		for(x1=x_max+1;x1<no_out_x;x1++)
			for(y1=no_out_x-x1;y1<x1;y1++)
			{
				angle = Math.atan2(y1-yc, x1-xc); //Angle is measured from Ox+ axis and positive in the CCW direction
				y_margin = yc + Math.tan(angle)*(x_max-xc); //The x coord of the pixel at the same angle.
				y_margin = Math.rint(y_margin); //Round to the nearest integer
				y_mar_int = y_margin.intValue(); //Change from Double to int
				for(k=0;k<no_comp;k++)
					PCA_coeff[k].setEntry(y1, x1, PCA_coeff[k].getEntry(y_mar_int, x_max));
			}
	}

	/** main **/
	public static < T extends RealType<T> & NativeType<T>> void generate_PCAcoefficientmaps ( EigenDecomposition PCA_eig,
			ArrayRealVector param_means, int no_pixels, centerList cList, svParametersPSFmodel paramPSF )
	{

		// get dimensions		
		int no_param = (int) param_means.getDimension();
		int step = 0;
		onePSF oPSF = (onePSF) new onePSF();
		ImgOpener imgOpener = new ImgOpener();
		
		//Initialise a RealVector used as output for the parameter calculation.
		RealVector param = null;

		//Initialise an array of Array2DRowRealMatrix
		Array2DRowRealMatrix[] PCA_coeff = new Array2DRowRealMatrix[no_param];
		Arrays.setAll(PCA_coeff, element->new Array2DRowRealMatrix(no_pixels,no_pixels));

		//Initialise a list of Coefficient objects for storing the inputs.
		LinkedList<Coefficient> PCA_input_coeff = new LinkedList<Coefficient>();

		//Go through each line of txt file (a line represents an image)
		for(File file:cList.files)
		{	
			try {
				@SuppressWarnings("unchecked")
				Img<T> img = ( Img< T > ) imgOpener.openImgs( file.getAbsolutePath() ).get( 0 );
				
				//param = oPSF.generateOnePSF_pixel(file, no_param );
				param = oPSF.generateOnePSF_pixel(img, no_param );

				// Set the PSF location
				Point centre = new Point(2);
				if ( cList.centre != null ) { // if PSF locations were read from file
					//System.out.println("Reading PSF centre from file");
					//Read output mode position of the PSF 
					centre.setPosition(cList.centre.getIntPosition(2*step), 0);  //save c position
					centre.setPosition(cList.centre.getIntPosition(2*step+1), 1);  //save y position
				} else {		
					//System.out.println("Get PSF centre from max");
					centre = oPSF.maxLocation;
				}
				//System.out.println(centre.getIntPosition(0));
				//System.out.println(oPSF.maxLocation.getIntPosition(0));
				//System.out.println(centre.getIntPosition(1));
				//System.out.println(oPSF.maxLocation.getIntPosition(1));

				add_input_PCA_coeff(param, centre, param_means, PCA_eig, PCA_input_coeff);
			} catch (ImgIOException e) {
				e.printStackTrace();
			}
			step++;
		}

		calculate_PCA_coeff_maps(PCA_coeff,PCA_input_coeff);

		convert3DMatrix( PCA_coeff, paramPSF );     

		//return PCA_coeff;        
	}

	/** convert and save coefficient map **/
	private static void convert3DMatrix( Array2DRowRealMatrix[] PCA_coeff, svParametersPSFmodel paramPSF ){

		FloatType pixel;
		int x,y,z;
		double coeff;

		int pix = PCA_coeff[0].getColumnDimension();
		int slice = PCA_coeff.length;

		final ImgFactory< FloatType > imgFactory = new ArrayImgFactory< FloatType >();
		Img<FloatType> imgD = imgFactory.create(new long[] { pix, pix, slice }, new FloatType() );;

		Cursor<FloatType> cursor = null;

		cursor = imgD.localizingCursor();
		while(cursor.hasNext())
		{
			pixel = cursor.next();
			x = cursor.getIntPosition(0);
			y = cursor.getIntPosition(1);
			z = cursor.getIntPosition(2);
			coeff = PCA_coeff[z].getEntry(y, x);
			pixel.setReal(coeff);
		}
		String savename = paramPSF.get_modelID() + "_svPSFmodel_CoeffMaps.tif";
		svTool.save( imgD,  paramPSF.get_folderPSFout(), savename);

	}

}