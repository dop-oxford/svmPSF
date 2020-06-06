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

import dop.svPSFmodel.svTool.Timer;
import dop.svPSFmodel.svTool.outLog;

/**
 * Class for logging all the metadata related to the eigen-PSF model generation to a text file
 **/

public class logModel{

	outLog oLog = (outLog) new outLog();

	/** set the logger **/
	public logModel(svParametersPSFmodel paramPSF) {

		//Generate output text file
		String savename = paramPSF.get_folderPSFout() + File.separator + paramPSF.get_modelID() + "_svPSFmodel_log.txt";
		oLog.initLog(savename);
		oLog.txtLog();

		// output header
		svTool.trace(" ---------------------------------------- ");
		svTool.trace("svPSFSmodel LOG");
		svTool.trace("");
		oLog.cslLog(); // print to console
	}

	/** write the parameters **/
	public void writeInputs(svParametersPSFmodel paramPSF) {

		oLog.txtLog(); // print to text file
		// output PSF ID
		svTool.trace(" ---- PSF ID ---- ");
		svTool.trace("PSF model ID: " + paramPSF.get_modelID());
		svTool.trace(" -------- ");
		svTool.trace("");
		// output parameters
		svTool.trace(" ---- Input parameters ---- ");
		svTool.trace(String.format("# of Parameters: %d", paramPSF.get_paramNo()));
		svTool.trace(String.format("# of Pixels per line: %d", paramPSF.get_pixelNo()));
		svTool.trace("Folder for PSF input:" + paramPSF.get_folderPSFin());
		svTool.trace("Folder for PSF output:" + paramPSF.get_folderPSFout());
		if ( paramPSF.get_readPosition() != null ) {
			svTool.trace("Reference position file:" + paramPSF.get_readPosition()[0]);
		} else {
			svTool.trace("Reference position file: none");
		}
		svTool.trace(" -------- ");
		svTool.trace("");
		oLog.cslLog(); // print to console

	}

	/** write the timers **/
	public void writeTimers(Timer tcList, Timer tEig, Timer tCoeff, Timer tKernel, Timer tAll) {

		oLog.txtLog(); // print to text file
		// output timings
		svTool.trace(" ---- Timings ---- ");
		svTool.trace(" Centre list:			" + tcList);
		svTool.trace(" Eigen decomposition:		" + tEig);
		svTool.trace(" Coefficient maps generation:	" + tCoeff);
		svTool.trace(" PSF kernels generation:		" + tKernel);
		svTool.trace(" ---- ");
		svTool.trace(" All:				" + tAll);
		svTool.trace(" -------- ");
		svTool.trace(" ");
		svTool.trace(" PSF model generation completed");
		svTool.trace(" ---------------------------------------- ");
		oLog.cslLog(); // print to console

	}

	/** reading options (unused) **/
	public static svParametersPSFmodel readModel(svParametersPSFmodel paramPSF, String folder, String name) {

		paramPSF.set_modelID(svTool.inLog(4, folder, name));
		paramPSF.set_paramNo(Integer.valueOf(svTool.inLog(8, folder, name)));
		paramPSF.set_pixelNo(Integer.valueOf(svTool.inLog(9, folder, name)));
		return paramPSF;
	}
}