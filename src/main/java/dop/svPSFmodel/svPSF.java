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

import org.apache.commons.math3.linear.*;


/**
 * Class for generation of the eigen-PSF model for system with a spatially-varying point response.
 **/

public class svPSF {

	/** set various timers **/
	private svTool.Timer tAll = svTool.getTimer(); // Total time
	private svTool.Timer tEig = svTool.getTimer(); // Time for eigen-value decomposition
	private svTool.Timer tCoeff = svTool.getTimer(); // Time for generating eigen coefficient maps
	private svTool.Timer tKernel = svTool.getTimer(); // Time for generating PSF kernels
	private svTool.Timer tcList = svTool.getTimer(); // Time for generating PSF kernels

	/** parameter requiring access **/
	public svParametersPSFmodel paramPSF = (svParametersPSFmodel) new svParametersPSFmodel(225);

	/** initialisation **/
	public svPSF() {
		super();
	}

	/** calling the PSF model generation **/
	public svPSF generatePSFmodel() {
		return modelGeneration( paramPSF.get_paramNo(), paramPSF.get_folderPSFin(), paramPSF.get_folderPSFout(), paramPSF.get_readPosition());    
	}

	/** calling the PSF model generation **/
	public svPSF generatePSFmodel(int paramNo) {
		this.paramPSF.set_paramNo(paramNo) ;
		return modelGeneration( paramPSF.get_paramNo(), paramPSF.get_folderPSFin(), paramPSF.get_folderPSFout(), paramPSF.get_readPosition());    
	}

	/** calling the PSF model generation **/
	public svPSF generatePSFmodel(int paramNo, String folderPSFin, String folderPSFout) {
		this.paramPSF.set_paramNo(paramNo) ;
		this.paramPSF.set_folderPSFin(folderPSFin);
		this.paramPSF.set_folderPSFout(folderPSFout);
		return modelGeneration( paramPSF.get_paramNo(), paramPSF.get_folderPSFin(), paramPSF.get_folderPSFout(), paramPSF.get_readPosition());
	}

	/** calling the PSF model generation **/
	public svPSF generatePSFmodel(int paramNo, String folderPSFin, String folderPSFout, String[] readPosition) {
		this.paramPSF.set_paramNo(paramNo) ;
		this.paramPSF.set_folderPSFin(folderPSFin);
		this.paramPSF.set_folderPSFout(folderPSFout);
		this.paramPSF.set_readPosition(readPosition); 
		return modelGeneration( paramPSF.get_paramNo(), paramPSF.get_folderPSFin(), paramPSF.get_folderPSFout(), paramPSF.get_readPosition());
	}

	/** main **/
	private svPSF modelGeneration(int paramNo, String folderPSFin, String folderPSFout, String[] readPositions) {

		tAll.start();

		// Loading parameters and setting model ID
		this.paramPSF = (svParametersPSFmodel) new svParametersPSFmodel( paramNo, folderPSFin, folderPSFout, readPositions);

		// Setting model unique identifier
		this.paramPSF.set_modelID( svTool.dater() );

		// Get PSF positions in object space from files and filenames 
		tcList.start();
		System.out.println("File list started");
		centerList cList = (centerList) new centerList(folderPSFin);
		this.paramPSF.set_pixelNo( cList.pixelNo ); // set the number of pixel per line
		if ( readPositions != null ) { cList.readCenters(readPositions[0], readPositions[1]); }
		System.out.println("File list completed");
		tcList.stop();

		// Perform eigen-value decomposition
		tEig.start();
		System.out.println("PCA decomposition started");
		ArrayRealVector param_means = new ArrayRealVector(paramPSF.get_paramNo());
		EigenDecomposition eig = PCA_component.generate_PCAcomponents( param_means, cList );
		System.gc(); // Explicit variable release for memory saving
		System.out.println("PCA decomposition generated");
		tEig.stop();

		// Generate PCA coefficients
		tCoeff.start();
		System.out.println("PCA coefficient started");
		PCA_coefficient.generate_PCAcoefficientmaps( eig, param_means, paramPSF.get_pixelNo(), cList, paramPSF );
		System.gc(); // Explicit variable release for memory saving
		System.out.println("PCA coefficient generated");
		tCoeff.stop();

		// Generate the kernels 
		tKernel.start();
		System.out.println("PSF eigenvector started");
		svKernels.generateKernels( eig, param_means, paramPSF );
		System.gc(); // Explicit variable release for memory saving
		System.out.println("PSF eigenvector generated");
		tKernel.stop();

		tAll.stop();

		// Setting log information
		final logModel lModel = (logModel) new logModel( paramPSF );
		lModel.writeInputs( paramPSF );
		lModel.writeTimers( tcList, tEig, tCoeff, tKernel, tAll );

		return this;
	}

}