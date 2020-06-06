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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imagej.ImgPlus;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Various tools for savers, timers and loggers
 **/

public final class svTool {

	/** forbid the construction of this class **/
	private svTool() {}

	/** the tool implementation in use*/
	static private svTool.Logger currentLogger;

	/** simple logger */
	public interface Logger {
		public void writeTrace(String message); 
		public void writeError(String message, boolean fatal);
		public void writeShortMessage(String message);
	}

	/** inits a standard tool */
	static {
		// basic logger goes to System.out
		currentLogger = new svTool.Logger() {
			public void writeTrace(String message) {
				System.out.println( "[svPSFmodel] "+message);
				System.out.flush();
			}

			public void writeError(String message, boolean fatal) {
				String prefix = (fatal)?("[fsFATAL]"):("[fsERROR]");
				System.err.println( prefix+" "+message);
				System.err.flush();
			}

			public void writeShortMessage(String message) {
				System.out.println( "-svPSFmodel- "+message);
				System.out.flush();
			}
		};
	}

	/** write a trace message */
	static public final void trace(String message) {
		if (currentLogger!=null)
			currentLogger.writeTrace( message );
	}

	/** implement and pass Tool.Logger to redirect log output **/
	public static void setLogger( svTool.Logger t ) {
		currentLogger = t;
	}
	
	public static class outLog {

		private PrintStream originalStdout = System.out;
		private PrintStream fileStream;

		public void initLog(String name) {
			try {
				File file = new File(name);
				file.createNewFile();
				System.out.println("File: " + file);
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				fileStream = new PrintStream(name);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		public void txtLog() {
			System.setOut(fileStream);

		}

		public void cslLog() {
			System.setOut(originalStdout);
		}		
	}

	public static String inLog( int lineNo, String folder, String name ) {

		// The name of the file to open.
		String fileName = folder + File.separator + name;

		// This will reference one line at a time
		String line = null;
		String lineOut = null;

		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(fileName);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			int counter = 0;
			while((line = bufferedReader.readLine()) != null) {
				if (counter == lineNo) {
					int pos1 = line.indexOf(':');
					lineOut = line.substring(pos1+1).trim();
					//System.out.println(lineOut);		
				}
				counter ++;
			}   

			// Always close files.
			bufferedReader.close();


		}
		catch(FileNotFoundException ex) {
			System.out.println(
					"Unable to open file '" + 
							fileName + "'");                
		}
		catch(IOException ex) {
			System.out.println(
					"Error reading file '" 
							+ fileName + "'");
		}
		return lineOut;
	}

	/** return a Tool.Timer, which is automatically started. */
	static public Timer getTimer() { return new Timer(); };

	/** timer **/
	public static class Timer {
		long start, stop, runtime, outtime;
		Timer() { 
			start();
		}
		/** start the timer */
		public void start() { 
			//start = System.currentTimeMillis(); 
			start =  System.nanoTime(); 
		};
		/** stop the timer (next start resets it) */
		public void stop() { 
			//stop = System.currentTimeMillis(); 
			stop = System.nanoTime(); 
			runtime += stop-start;
			outtime=runtime;
			runtime=0;
		}
		/** pause the timer (next start continues) */
		public void hold(){
			//stop = System.currentTimeMillis();
			stop = System.nanoTime(); 
			runtime += stop-start;
			outtime  = runtime;
			start =stop;
		}
		/** output the amount of milliseconds counted */
		@Override public String toString(){ 
			return String.format("%10.3f ms",(outtime/1000000.));
		}
		/** get the milliseconds on this timer */
		public double msElapsed() {
			return outtime/1000000.;
		}

	}

	/** output a current date and time string **/
	public static String dater() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");//("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		//System.out.println(dateFormat.format(date));
		String stringDate = dateFormat.format(date);
		return stringDate;
	}

	/** convert Img to ImgPlus **/
	public static ImgPlus<FloatType> convertImg2ImgP(Img<FloatType> finImgUint32){
		return new ImgPlus<FloatType>(finImgUint32);
	}

	/** save of images **/
	public static < T extends Comparable< T > & Type< T > & RealType<T>>  void save ( Img<T> finalImgUi32, String folder, String name ) {
		ImgSaver saverB = (ImgSaver) new ImgSaver();
		try {
			saverB.saveImg(folder + File.separator + name, finalImgUi32);
		} catch (ImgIOException e) {
			e.printStackTrace();
		} catch (IncompatibleTypeException e) {
			e.printStackTrace();
		}	
	}

}



