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

package dop.svmPSF;

import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import dop.svUserDialog.UserDialog;


/**
 * This class svmPSF generate a command for creation of an imageJ plugin of svPSFmodel
 */
@Plugin(type = Command.class, menuPath = "Plugins>svmPSF")
public class svmPSF<T extends RealType<T>> implements Command {

    @Override
    public void run() {
    	/** generate a GUI with callback to svPSFmodel **/
    	UserDialog orientation = new UserDialog();
		orientation.showDialog();	

    }

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     */
    public static void main(final String... args) throws Exception {
    	UserDialog orientation = new UserDialog();
		orientation.showDialog();

    }

}
