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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.RealSum;
import net.imglib2.view.Views;

/**
 * Class for processing the input PSF measurements for use in the eigen-PSF model
 **/

public class onePSF {

	public Point maxLocation = null;

	private final double threshold = 0.1;


	/** get cropped and normalised PSFs from full PSF images **/
	public < T extends RealType<T> & NativeType<T>> RealVector generateOnePSF_pixel( Img<T> img, int paramNo )
	{

		RealSum integral = new RealSum();
		double normalIntRatio, normalIntValue = 1;

		RealVector param = new ArrayRealVector(paramNo,Double.NaN);
		boolean verify = true;

		maxLocation = new Point( img.numDimensions() );

		verify = verify_and_getPSF(img,maxLocation,threshold);

		// Crop the PSF only part
		long PSF_mask_max = (long) Math.sqrt(paramNo), PSF_mask_min = (long) Math.sqrt(paramNo);
		long up_space,down_space,left_space,right_space;
		long x_lim = img.dimension(0), y_lim = img.dimension(1);

		long x_max = maxLocation.getLongPosition(0), y_max = maxLocation.getLongPosition(1);
		long PSF_mask_size, PSF_mask_halfSize = (PSF_mask_max-1)/2;
		up_space = y_max-1;
		down_space = y_lim-y_max;
		left_space = x_max-1;
		right_space = x_lim-x_max;
		PSF_mask_halfSize = Math.min(PSF_mask_halfSize, Math.min(Math.min(up_space, down_space), Math.min(right_space, left_space)));
		PSF_mask_size = 2*PSF_mask_halfSize + 1;

		if(verify && PSF_mask_size>=PSF_mask_min)
		{			
			IterableInterval<T> PSF_crop = Views.interval(img, new long[] {x_max-PSF_mask_halfSize,y_max-PSF_mask_halfSize}, 
					new long[] {x_max+PSF_mask_halfSize,y_max+PSF_mask_halfSize});
			Cursor<T> cursor = PSF_crop.localizingCursor();
			T type = null;

			while(cursor.hasNext())
			{
				type = cursor.next();
				param.setEntry((int) (PSF_mask_size * (cursor.getIntPosition(1)-y_max+PSF_mask_halfSize) + cursor.getIntPosition(0)-x_max+PSF_mask_halfSize), type.getRealDouble());
			}

			for(int i=0;i<paramNo;i++)
				integral.add(param.getEntry(i));
			normalIntRatio = normalIntValue / integral.getSum();
			for(int i=0;i<paramNo;i++)
				param.setEntry(i, param.getEntry(i)*normalIntRatio);
		}
		img = null;
		System.gc();
		return param;
	}

	/** verify PSF values **/
	public static < T extends Comparable< T > & Type< T > & RealType<T>> boolean verify_and_getPSF(
			final IterableInterval< T > input, final Point maxLocation , final double threshold)
	{
		// create a cursor for the image (the order does not matter)
		final Cursor<T> cursor = input.cursor();

		// initialise max with the first image value
		T type = cursor.next();
		T max = type.copy();

		//Count all values using the RealSum class
		//It prevents numerical instabilities for large numbers
		final RealSum realSum = new RealSum();
		long count=0;
		double average, verify;

		// loop over the rest of the data and determine min and max value
		while ( cursor.hasNext() )
		{
			type = cursor.next();

			if ( type.compareTo( max ) > 0 )
			{
				max.set( type );
				maxLocation.setPosition(cursor);
			}
			realSum.add(type.getRealDouble());
			++count;
		}

		//Calculate pixel value average and the average/maximum ratio 
		average = realSum.getSum() / count;
		verify = average / max.getRealDouble();
		System.gc();
		//Verify if average/maximum ratio is under a certain threshold and if the PSF is not saturated
		if(verify<=threshold && max.getRealDouble()<255)
			return true;
		else
			return false;
	}
}