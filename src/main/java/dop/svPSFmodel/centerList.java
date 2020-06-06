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
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JOptionPane;

import io.scif.img.IO;
import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Class for reading the position of focal spot measurements from a text file
 **/

public class centerList {

	public Point centre = null; // centre position variable
	public File[] files;
	public File dir;
	public int pixelNo = 0;

	public < T extends RealType<T> & NativeType<T>> centerList(String PSFfolder) {
		super();
		try {
			this.dir = new File(PSFfolder);
			this.files =  dir.listFiles((d, name) -> name.endsWith(".tif")); 

			@SuppressWarnings("unchecked")
			Img<T> image = (Img<T>) IO.openImgs(files[0].getAbsolutePath()).get(0);
			pixelNo = get_pixelNo(image);

		} catch (IllegalArgumentException iae) {
			System.out.println("Input PSF samples folder Not Found");
		}
	}

	/** main **/
	public void readCenters(String positionFile, String separator) 
	{

		//Open positions.txt file
		File positions = null;
		try {
			positions = new File(dir.getAbsolutePath() + File.separator + positionFile);
		} catch (IllegalArgumentException iae) {
			System.out.println("Positions File Not Found.");
		}

		//Initialise variables for opening each image
		this.centre = new Point(2*files.length);
		Scanner sc_pos = null;
		try {
			sc_pos = new Scanner(positions); //Create scanner for txt file
		} catch (FileNotFoundException e) {
			System.out.println("Cannot open or find the position file.");
		} 
		String line = null; //String for reading each line in txt file
		int stepIdx = 0;

		//Go through each line of txt file (a line represents an image)
		while(sc_pos.hasNextLine())
		{
			//Read image path
			line=sc_pos.nextLine(); //read line
			files[stepIdx] = new File(dir.getAbsolutePath()+ File.separator +line.substring(1,line.indexOf(".tif")+4)); 
			//System.out.println(stepIdx);
			//System.out.println(files[stepIdx]);		

			//Read output mode position of the PSF 
			String[] centre_coordinates = line.substring(line.indexOf(".tif")+4+1).trim().split(separator); // read positions
			centre.setPosition(Integer.parseInt(centre_coordinates[0]), stepIdx*2+1); // set horizontal position
			centre.setPosition(Integer.parseInt(centre_coordinates[1]), stepIdx*2); // set vertical position
			//System.out.println(centre);

			stepIdx++;

		}
		sc_pos.close(); //close scanner

	}

	/** get the number of pixels per row/column in PSF images **/
	private  < T extends RealType<T> & NativeType<T>> int get_pixelNo(Img<T> image)
	{
		long h_lim = image.dimension(0);
		long v_lim = image.dimension(1);
		int HpixelNo = (int) h_lim;
		int VpixelNo = (int) v_lim;

		if ( HpixelNo == VpixelNo ) {
			pixelNo = HpixelNo;
		} else {
			System.out.println("PSF images are not square.");
			JOptionPane.showMessageDialog( null,
					"PSF images are not square.", "PCA_component->onePSF error",
					JOptionPane.ERROR_MESSAGE);
		}
		//System.out.println(VpixelNo);
		//System.out.println(HpixelNo);

		return pixelNo;	

	}
}