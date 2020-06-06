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

This file was adapted from Project: Directional Image Analysis - OrientationJ plugins
by author: Daniel Sage
from: Organization: Biomedical Imaging Group (BIG), 
Ecole Polytechnique Federale de Lausanne (EPFL), Lausanne, Switzerland

 */

package dop.svUserDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
//import javax.swing.JCheckBox;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dop.svUserDialog.GridPanel;
import dop.svUserDialog.GridToolbar;
import dop.svUserDialog.SpinnerInteger;
import dop.svPSFmodel.svPSF;


import ij.gui.GUI;

/**
 * The class generate the graphical user interface for use with the class svPSFmodel
 */

public class UserDialog extends JDialog implements ActionListener, ChangeListener, WindowListener, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JButton			bnRun				= new JButton("Run");
	protected JButton			bnClose				= new JButton("Close");
	private JButton				bnSelInput			= new JButton("Input folder");
	private JButton				bnSelOutput			= new JButton("Output folder");
	private JButton				bnSelPosition		= new JButton("Position file");

	private JTextField			tfInput 			= new JTextField("select input folder",50);
	private JTextField			tfOutput			= new JTextField("select output folder",50);
	private JTextField			tfSelPosition		= new JTextField("select position file - optional                    ",50);
	//private JCheckBox			cbSelPosition		= new JCheckBox("Read focus positions from file", true);

	private SpinnerInteger		spnMode				= new SpinnerInteger(15, 5, 21, 1);
	private JTextField			tfMode				= new JTextField("225");

	public UserDialog() {
		super(new JFrame(), "svmPSF ");
	}

	public void showDialog() {

		tfInput.setEnabled(false);
		tfOutput.setEnabled(false);
		tfSelPosition.setEnabled(false);
		tfMode.setEnabled(false);

		// Panel Tensor
		GridToolbar pnMetric = new GridToolbar(false, 3);
		pnMetric.place(0, 0, new JLabel("Mode number: "));
		pnMetric.place(0, 1, tfMode);
		pnMetric.place(0, 2, spnMode);

		GridPanel pnMain1 = new GridPanel("Inputs", 3);
		pnMain1.place(0, 0, pnMetric);
		pnMain1.place(1, 0, 1, 1, bnSelInput);
		pnMain1.place(1, 3, 50, 1, tfInput);
		pnMain1.place(2, 0, 1, 1, bnSelOutput); 
		pnMain1.place(2, 3, 50, 1, tfOutput);
		pnMain1.place(3, 0, 1, 1, bnSelPosition);
		pnMain1.place(3, 3, 50, 1, tfSelPosition); 
		//pnMain1.place(4, 0, 1, 1, cbSelPosition); 


		GridPanel pnMain = new GridPanel(false);
		pnMain.place(0, 0, 50, 1, pnMain1);
		pnMain.place(1, 25, 4, 1, bnRun);
		pnMain.place(2, 25, 4, 1, bnClose);
		pnMain.place(3, 2, 4, 1, new JLabel("Dynamic Optics and Photonics Group - University of Oxford",4));

		GridPanel pn = new GridPanel(false, 3);
		JTabbedPane tab = new JTabbedPane();
		Reference ref = new Reference();
		ref.setPreferredSize(pnMain.getSize());
		tab.add("Model", pnMain);
		tab.add("Reference", ref.getPane());
		pn.place(0, 0, tab);

		// Listener
		bnRun.addActionListener(this);
		bnClose.addActionListener(this);
		bnSelInput.addActionListener(this);
		bnSelOutput.addActionListener(this);
		bnSelPosition.addActionListener(this);
		spnMode.addChangeListener(this);
		//cbSelPosition.addChangeListener(this);

		// Finalize
		addWindowListener(this);
		getContentPane().add(pn);
		pack();
		setResizable(false);
		GUI.center(this);
		setVisible(true);

	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {

		//getParameters();
		UserDialogPSF thisDialog = (UserDialogPSF) new UserDialogPSF();


		if (e.getSource() == bnSelInput) {
			String inputString = thisDialog.SelectFolder();
			tfInput.setText(inputString);
		}
		if (e.getSource() == bnSelOutput) {
			String outputString = thisDialog.SelectFolder();
			tfOutput.setText(outputString);
		}
		if (e.getSource() == bnSelPosition) {
			String PositionString = thisDialog.SelectFile();
			tfSelPosition.setText(PositionString);
		}
		if (e.getSource() == bnRun) {

			if ( tfInput.getText().compareTo( "select input folder" ) == 0 || tfOutput.getText().compareTo( "select output folder" ) == 0 ) {
				JOptionPane.showMessageDialog(null, "Input and Output folders must be selected.","Folders", JOptionPane.INFORMATION_MESSAGE);

			} else {

				svPSF thisPSFmodel = (svPSF) new svPSF();
				int value = (Integer) spnMode.getValue();
				int mode = value*value;
				// required inputs for svPSF
				//String folderIn = "/Users/raphael/Dropbox/Oxford/Oxford Projects/SVD/WorkingData/AnalysisNA2/PSF_in";
				//String folderOut = "/Users/raphael/Dropbox/Oxford/Oxford Projects/SVD/WorkingData/AnalysisNA2/PSF_out_2";
				//String[] position = {"positions.txt","  "}; // name of PSF info file and separator in file

				//if (cbSelPosition.isSelected() == false) {

				/** generate PSF model **/
				//thisPSFmodel.generatePSFmodel(mode, tfInput.getText(), tfOutput.getText());
				//thisPSFmodel.generatePSFmodel(225, folderIn, folderOut);

				//} else {

				String[] position = {tfSelPosition.getText(),"  "}; // name of PSF info file and separator in file

				/** generate PSF model **/
				thisPSFmodel.generatePSFmodel(mode, tfInput.getText(), tfOutput.getText(), position);
				//thisPSFmodel.generatePSFmodel(225, folderIn, folderOut, position);

				//}
				System.out.println("END OF MODELLING");
				//dispose();
			}

		}
		if (e.getSource() == bnClose) {
			dispose();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == spnMode) {
			int value = (Integer) spnMode.getValue();
			int Mode = value*value;
			tfMode.setText(Integer.toString(Mode));
		}
//		if (e.getSource() == cbSelPosition) {
//			if (cbSelPosition.isSelected() == true) {
//				bnSelPosition.setEnabled(true);
//			} else {
//				bnSelPosition.setEnabled(false);
//			}
//		}

	}

	@Override
	public void run() {		
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		dispose();
	}

}
