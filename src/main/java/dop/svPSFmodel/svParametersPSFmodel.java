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
import javax.swing.JOptionPane;

/**
 * Class for handling all the parameters related to the eigen-PSF model
 **/

public class svParametersPSFmodel {

	private int paramNo = 225; // number of parameter for each PSF measurement to use in the evaluation of the co-variance matrix
	private String folderPSFin = null; // Folder containing input focal spot measurements
	private String folderPSFout = null; // svmPSF output folder
	private String modelID = null; // Unique identifier for each PSF model
	private int pixelNo = 120; // number of pixel per line in PSF images. Images must be square.
	private String[] readPosition = null; // number of pixel per line in PSF images. Images must be square.

	public svParametersPSFmodel() {
		super();
	}


	public svParametersPSFmodel(int paramNo) {
		super();
		if (check_paramNo(paramNo)) {this.paramNo = paramNo;}
	}


	public svParametersPSFmodel(int paramNo, String folderPSFin, String folderPSFout, String[] readPosition ) {
		super();
		if (check_paramNo(paramNo)) {this.paramNo = paramNo;}
		if (check_folderPSFin(folderPSFin)) {this.folderPSFin = folderPSFin;}
		if (check_folderPSFout(folderPSFout)) {this.folderPSFout = folderPSFout;}
		this.readPosition = readPosition;
	}


	public svParametersPSFmodel set_paramNo(int paramNo) {
		if (check_paramNo(paramNo)) {this.paramNo = paramNo;}
		return this;
	}


	public int get_paramNo() {
		return paramNo;
	}


	public svParametersPSFmodel set_folderPSFin(String folderPSFin) {
		if (check_folderPSFin(folderPSFin)) {this.folderPSFin = folderPSFin;}
		return this;
	}


	public String get_folderPSFin() {
		return folderPSFin;
	}


	public svParametersPSFmodel set_folderPSFout(String folderPSFout) {
		if (check_folderPSFout(folderPSFout)) {this.folderPSFout = folderPSFout;}
		return this;
	}


	public String get_folderPSFout() {
		return folderPSFout;
	}


	public svParametersPSFmodel set_modelID(String modelID) {
		this.modelID = modelID;
		return this;
	}


	public String get_modelID() {
		return modelID;
	}


	public svParametersPSFmodel set_pixelNo(int pixelNo) {
		this.pixelNo = pixelNo;
		return this;
	}


	public int get_pixelNo() {
		return pixelNo;
	}

	public svParametersPSFmodel set_readPosition(String[] readPosition) {
		this.readPosition = readPosition;
		return this;
	}


	public String[] get_readPosition() {
		return readPosition;
	}

	/** check that the number of parameters has in integer root-square **/
	private boolean check_paramNo(int paramNo) {

		boolean check = false;  // return false if non-integer root-square
		double sqr_root = (double) Math.sqrt(paramNo);
		//System.out.println(sqr_root);
		//System.out.println(sqr_root%1);
		if (sqr_root%1 == 0) {check  = true;}// return true if integer square root
		else {
			System.out.println("The number of parameters for eigen-decomposition must have an integer square root. The default value (225) will be used.");
			JOptionPane.showMessageDialog( null,
					"The number of parameters for eigen-decomposition must have an integer square root. The default value (225) will be used.", "svParameterPSFmodel error",
					JOptionPane.ERROR_MESSAGE);
		}
		return check;
	}

	/** check that the output PSF folder exists **/
	private boolean check_folderPSFin(String folderPSFin) {

		boolean check = true;  // return true if folder exists
		try {
			new File(folderPSFin);
		} catch (IllegalArgumentException iae) {
			System.out.println("Input PSF samples folder Not Found");
			JOptionPane.showMessageDialog( null,
					"Input PSF samples folder Not Found.", "svParameterPSFmodel error",
					JOptionPane.ERROR_MESSAGE);
			check = false;
		}

		return check;
	}


	/** check that the output PSF folder exists. otherwise try to create it. **/
	private boolean check_folderPSFout(String folderPSFout) {

		boolean check = true;  // return true if folder exists
		File outputImageFolder = new File(folderPSFout);
		if (!outputImageFolder.exists()) {
			if (outputImageFolder.mkdir()) {
				System.out.println("Output directory is created.");
			} else {
				System.out.println("Failed to create output directory.");
				JOptionPane.showMessageDialog( null,
						"Failed to create output directory.", "svParameterPSFmodel error",
						JOptionPane.ERROR_MESSAGE);
				check = false;
			}
		}

		return check;
	}


}
