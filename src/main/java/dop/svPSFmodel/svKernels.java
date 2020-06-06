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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealVector;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Fraction;

/**
 * Class for generation of eigen-PSFs for the eigen-PSF model
 **/

public class svKernels {

	/** main **/
	public static List<Img<DoubleType>> generateKernels (EigenDecomposition eig, RealVector param_means, svParametersPSFmodel paramPSF){//, int paramNo){

		long[] k_dims = new long[2];
		List<Img<DoubleType>> kernels = new ArrayList<>();
		RealVector eigenvector = null;
		Integer dim;
		long dim_k;
		double[] data;

		Fraction fr = new Fraction(); //initiated with 1

		eigenvector = eig.getEigenvector(0);
		dim = eigenvector.getDimension();
		data = new double[dim];
		// Kernels must be square
		dim_k = Math.round(Math.sqrt(dim.doubleValue()));
		k_dims[0]=dim_k;
		k_dims[1]=dim_k;

		data = param_means.toArray();
		DoubleArray dataD = new DoubleArray(data);
		ArrayImg<DoubleType,DoubleArray> kernel = new ArrayImg<>(dataD,k_dims,fr);
		kernel.setLinkedType(new DoubleType(kernel));
		kernels.add(kernel);

		for(int i=0;i<(dim-1);i++)
		{
			eigenvector = eig.getEigenvector(i);
			data = eigenvector.toArray();
			dataD = new DoubleArray(data);
			kernel = new ArrayImg<>(dataD,k_dims,fr);
			kernel.setLinkedType(new DoubleType(kernel));
			kernels.add(kernel);
		}

		converttoImgFloat( kernels, paramPSF );

		return kernels;
	}

	/** convert and save eigen-PSFs **/
	public static void converttoImgFloat(List<Img<DoubleType>> data, svParametersPSFmodel paramPSF)
	{

		Long dim = data.get(0).dimension(0);   //Set image dimensions

		int pos_x,pos_y,pos_z; //Cursor positions
		int[] pos = new int[2]; //Create an array of cursor positions
		DoubleType pixelOutImgs;

		int slice = data.size(); 
		List<RandomAccess<DoubleType>> randAcc = new ArrayList<RandomAccess<DoubleType>>();  //List of random accesses to the individual images

		int i; //index
		for(i=0;i<slice;i++)          //Generate the list
			randAcc.add(data.get(i).randomAccess());
		RandomAccess<DoubleType> indRandAcc = null;  //Individual random access

		FloatType pixelF;

		final ImgFactory< FloatType > imgFactory = new ArrayImgFactory< FloatType >();
		Img<FloatType> imgD = imgFactory.create(new long[] { dim, dim, slice }, new FloatType() );

		Cursor<FloatType> cursor = null;

		cursor = imgD.localizingCursor();

		while(cursor.hasNext())    //For each pixel in the final image get the same position pixels in the deconv. images and add them
		{
			pixelF = cursor.next();
			pos_x = cursor.getIntPosition(0);
			pos_y = cursor.getIntPosition(1);
			pos_z = cursor.getIntPosition(2);
			pos[0] = pos_x;
			pos[1] = pos_y;
			indRandAcc = randAcc.get(pos_z);
			indRandAcc.setPosition(pos);
			pixelOutImgs  = indRandAcc.get();
			double poi = (double) pixelOutImgs.get();
			pixelF.setReal(poi);
		}

		String savename = paramPSF.get_modelID() + "_svPSFmodel_EigenPSFs.tif";
		svTool.save( imgD, paramPSF.get_folderPSFout(), savename);
		//return imgD;

	}

}