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

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * This class extends the Java JEditorPane for providing references with ease in the panel as HTML information.
 **/

public class Reference extends JEditorPane {

	private static final long serialVersionUID = 1L;
	private String	html		= "";
	private String	header		= "";
	private String	footer		= "";
	private String	font		= "verdana";
	private String	color		= "#222222";
	private String	background	= "#f8f8f8";

	private String ref0 = 
			"Turcotte R. et al. "
					+ "Open-source modeling of spatially varying focus for multimode fiber imaging, \n"
					+ "TBD.\n";

	private String ref1 = 
			"Sage D. et al. "
					+ "DeconvolutionLab2: An open-source software for deconvolution microscopy, \n"
					+ "Methods, vol. 115, pp.28-41, 2017.\n";

	private String ref2 = 
			"Lauer T. et al. "
					+"Deconvolution with a spatially-variant PSF, \n"
					+"in Proceedings of SPIE Conference on Astronomical Data Analysis II \n"
					+ "(International Society for Optics and Photonics, Waikoloa, Hawaii, 2002), pp 167–173.";

	public Reference() {
		header += "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\n";
		header += "<html><head>\n";
		header += "<style>body {background-color:" + background + "; color:" + color + "; font-family: " + font + ";margin:4px}</style>\n";
		header += "<style>h1 {color:#555555; font-size:1.0em; font-weight:bold; padding:1px; margin:1px; padding-top:5px}</style>\n";
		header += "<style>h2 {color:#333333; font-size:0.9em; font-weight:bold; padding:1px; margin:1px;}</style>\n";
		header += "<style>h3 {color:#000000; font-size:0.9em; font-weight:italic; padding:1px; margin:1px;}</style>\n";
		header += "<style>p  {color:" + color + "; font-size:0.9em; padding:1px; margin:0px;}</style>\n";
		header += "<style>pre  {font-size:0.8em; padding:1px; margin:0px;}</style>\n";
		header += "</head>\n";
		header += "<body>\n";
		footer += "</body></html>\n";
		setEditable(false);
		setContentType("text/html; charset=ISO-8859-1");

		append("<div style=\"text-align:center\">");
		append("</div>");

		append("h2", "Reference on the methods and plugins");
		append("p", ref0);
		append("p", "Dynamic Optics and Photonics Group - University of Oxford");
		append("p", " ");


		append("h2", "Reference on the model");
		append("p", ref2);
		append("p", " ");

		append("h2", "Reference for the GUI");
		append("p", ref1);
		append("p", " ");

	}

	@Override
	public String getText() {
		Document doc = this.getDocument();
		try {
			return doc.getText(0, doc.getLength());
		}
		catch (BadLocationException e) {
			e.printStackTrace();
			return getText();
		}
	}

	public void append(String content) {
		html += content;
		setText(header + html + footer);
		setCaretPosition(0);
	}

	public void append(String tag, String content) {
		html += "<" + tag + ">" + content + "</" + tag + ">";
		setText(header + html + footer);
		setCaretPosition(0);
	}

	public JScrollPane getPane() {
		JScrollPane scroll = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scroll;
	}

	public void show(int w, int h) {
		JScrollPane scroll = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(w, h));
		JFrame frame = new JFrame();
		frame.getContentPane().add(scroll);
		frame.pack();
		frame.setVisible(true);
	}
}
