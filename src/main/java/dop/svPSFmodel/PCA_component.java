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
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealVector;

import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.RealSum;

/**
 * Class for calculating the covariance matrix of PSF parameters and evaluate its eigen-value decomposition
 **/


public class PCA_component {
	
	/** load parameters from file and call for main calculations **/
	public static < T extends RealType<T> & NativeType<T>> EigenDecomposition  generate_PCAcomponents( ArrayRealVector param_means, centerList cList) 
	{
		ImgOpener imgOpener = new ImgOpener();
		onePSF oPSF = (onePSF) new onePSF();

		// Initialise an array of lists for each parameter
		@SuppressWarnings("unchecked")
		List<Double>[] param = (List<Double>[]) new List<?>[(int)param_means.getDimension()];
		Arrays.setAll(param	, element->new ArrayList<>());

		// Iterate through each PSF file and generate a PSF subarray (param)
		for(File file:cList.files)
		{
			//System.out.println(file);
			try {
				@SuppressWarnings("unchecked")
				Img<T> img = ( Img< T > ) imgOpener.openImgs( file.getAbsolutePath() ).get( 0 );

				RealVector one_param = null;
				one_param = oPSF.generateOnePSF_pixel( img, param.length );

				if(!one_param.isNaN())
					for(int i=0;i<param.length;i++)
						param[i].add(one_param.getEntry(i));

				img = null;
				one_param = null;
				System.gc();
			} catch (ImgIOException e) {
				e.printStackTrace();
			}
		}

		// Calculate parameter covariance matrix and get it's eigenvalue decomposition
		return calculatePCA_eig(param, param_means);
	}
	
	/** main **/
	private static EigenDecomposition calculatePCA_eig(List<Double>[] param, ArrayRealVector param_means)
	{
		int no_of_param = param.length, no_of_PSFs = param[0].size();
		Array2DRowRealMatrix covar_matrix = new Array2DRowRealMatrix(no_of_param,no_of_param); //covariance matrix
		int i,j,k; //indexes
		int counter; //counter
		RealSum realSum = new RealSum(); //a RealSum object to be used

		//Calculate the mean for each parameter
		for(i=0; i<no_of_param; i++)
		{
			counter=0;
			realSum = new RealSum();
			for(k=0; k<no_of_PSFs; k++)
			{
				realSum.add(param[i].get(k));
				counter++;
			}
			param_means.setEntry(i, realSum.getSum()/counter);
		}

		//Subtract the mean from each parameter
		for(i=0; i<no_of_param; i++)
		{
			for(k=0; k<no_of_PSFs; k++)
			{
				param[i].set(k, param[i].get(k)-param_means.getEntry(i));
			}
		}

		//Calculate covariance matrix
		for(i=0; i<no_of_param; i++)
		{
			for(j=i; j<no_of_param; j++)
			{
				realSum = new RealSum();
				for(k=0; k<no_of_PSFs; k++)
				{
					realSum.add(param[i].get(k)*param[j].get(k));
				}
				covar_matrix.setEntry(i, j, realSum.getSum());
				covar_matrix.setEntry(j, i, covar_matrix.getEntry(i, j));
			}
		}

		return new EigenDecomposition(covar_matrix);	
	}
}

/** coefficient class as a tool with PCA_component **/
class Coefficient
{
	public int x,y;
	public double[] coefficient;

	Coefficient()
	{
		x=0; y=0;
		coefficient = new double [10];
	}

	Coefficient(int size)
	{
		x=0; y=0;
		coefficient  = new double [size];
	}
}
