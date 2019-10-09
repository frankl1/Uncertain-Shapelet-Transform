/**
 * Decision Tree Classification With Uncertain Data (UDT)
 * Copyright (C) 2009, The Database Group,
 * Department of Computer Science, The University of Hong Kong
 *
 * This file is part of UDT.
 *
 * UDT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UDT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.decisiontree.datagen;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.decisiontree.convertor.SampleByteArrayConvertor;
import com.decisiontree.data.PointAttribute;
import com.decisiontree.data.Range;
import com.decisiontree.data.RangeAttribute;
import com.decisiontree.param.GlobalParam;

/**
 *
 * SampleDataGen - Generates interval-valued sampled distributed uncertain data from point-valued data.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SampleDataGen extends RangeDataGen {

	private static Logger log = Logger.getLogger(SampleDataGen.class);

	private int noSamples;

	private long seed = GlobalParam.DEFAULT_SEED;

	public SampleDataGen(String input, String nameFile, int noSamples, boolean varies) {
		super(input, nameFile, varies);
		setNoSamples(noSamples);
	}

	public SampleDataGen(String input, String nameFile, int noSamples, long seed, boolean varies) {
		super(input, nameFile, varies);
		setNoSamples(noSamples);
		setSeed(seed);
	}

	public SampleDataGen(String input, String nameFile, int noSamples, int precision, long seed, boolean varies) {
		super(input, nameFile, precision, varies);
		setNoSamples(noSamples);
		setSeed(seed);
	}

	private String getFileName(String input, int tupleNum, int attrNum){
		StringBuffer sb = new StringBuffer(input);
		sb.append(GlobalParam.SAMPLE_PATH);
		sb.append("T");
		sb.append(tupleNum);
		sb.append("A");
		sb.append(attrNum);
		return sb.toString();

	}

	@Override
	public void storeGeneratedData(String input, double[] range)  {
		BufferedWriter writer = null;
		BufferedReader reader = null;

		try {
			writer = new BufferedWriter(new FileWriter(input
					+ GlobalParam.SAMPLE_FILE));

			int noTuples = dataSet.getNoTuples();
			int noAttr = dataSet.getNoAttr();

			for (int k = 0; k < noAttr; k++) {
				if (!dataSet.isContinuous(k))
					continue;
				if (range != null) {
					setAttrRange(k, range[k] * dataSet.getDomainSize(k));
				}
			}

			File pdf = new File(input + GlobalParam.SAMPLE_PATH);
			pdf.mkdir();

			reader = new BufferedReader(new FileReader(input
					+ GlobalParam.POINT_FILE));
			String data = "";
			for (int i = 0; (data = reader.readLine()) != null && i < noTuples; i++) {
				String dataArray[] = data.split(GlobalParam.SEPERATOR);
				for (int k = 0; k < noAttr; k++) {
					if (!dataSet.isContinuous(k)) {
						writer.write(dataArray[k] + GlobalParam.SEPERATOR);
						continue;
					}
					PointAttribute t = new PointAttribute(Double.parseDouble(dataArray[k]));
					RangeAttribute rt = genError(t, k);

					Range rg = createPDF(getFileName(input, i,k),
							rt.getStart(), rt.getEnd());
					writer.write((rg.getStart() - 0.01) + GlobalParam.TO
							+ (rg.getEnd() + 0.01) + GlobalParam.SEPERATOR);
				}
				writer.write(dataArray[dataArray.length - 1]);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Cannot read or write dataset files. Please try again.");
			System.exit(1);
		}finally{
			try {
				if(writer != null) writer.close();
				if(reader != null) reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void storeGeneratedTestData(String test, double[] range) {

		BufferedWriter writer = null;
		BufferedReader reader = null;
		try {
			writer = new BufferedWriter(new FileWriter(test
					+ GlobalParam.SAMPLE_FILE));

			int noAttr = dataSet.getNoAttr();

			File pdf = new File(test + GlobalParam.SAMPLE_PATH);
			pdf.mkdir();
			reader = new BufferedReader(new FileReader(test
					+ GlobalParam.POINT_FILE));
			String data = "";
			for (int i = 0; (data = reader.readLine()) != null; i++) {
				String dataArray[] = data.split(GlobalParam.SEPERATOR);
				for (int k = 0; k < noAttr; k++) {
					if (!dataSet.isContinuous(k)) {
						writer.write(dataArray[k] + GlobalParam.SEPERATOR);
						continue;
					}
					PointAttribute t = new PointAttribute(Double.parseDouble(dataArray[k]));
					RangeAttribute rt = genError(t, k);

					Range rg = createPDF(getFileName(test , i , k), rt
							.getAbsStart(), rt.getAbsEnd());
					writer.write((rg.getStart() - 0.01) + GlobalParam.TO
							+ (rg.getEnd() + 0.01) + GlobalParam.SEPERATOR);

				}
				writer.write(dataArray[dataArray.length - 1]);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(writer != null) writer.close();
				if(reader != null) reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Deprecated
	public static byte[] doubleToByte(double d) {
		byte[] b = new byte[8];
		long l = Double.doubleToRawLongBits(d);
		for (int i = 0; i < 8; i++) {
			b[i] = new Long(l).byteValue();
			l = l >> 8;
		}
		return b;
	}

	public Range createPDF(String filename, double start, double end) {

		BufferedOutputStream writer = null;
		try {
			writer = new BufferedOutputStream(
					new FileOutputStream(filename));
			double value = 0;
			Random r = new Random(seed++);

			double samples[] = new double[noSamples];
			for (int i = 0; i < noSamples; i++) {
				do
					value = r.nextGaussian();
				while (value >= NUM_STDEV/2 || value <= -NUM_STDEV/2);
				samples[i] = value;
			}
			Arrays.sort(samples);
			byte[] b;
			for (int i = 0; i < noSamples; i++) {
				double data = start + (NUM_STDEV/2 + samples[i]) * (end - start) / NUM_STDEV;
				b = SampleByteArrayConvertor.doubleToByteArray(data);
				writer.write(b, 0, 8);
				double cdist = (i + 1) * 1.0 / noSamples;
				b = SampleByteArrayConvertor.doubleToByteArray(cdist);
				writer.write(b, 0, 8);
			}

			Range rg = new Range(start + (NUM_STDEV/2 + samples[0]) * (end - start) / NUM_STDEV,
					start + (NUM_STDEV/2 + samples[noSamples - 1]) * (end - start) / NUM_STDEV);

			return rg;
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error in reading samples!");
		} finally{
			try {
				if(writer != null) writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void setNoSamples(int noSamples) {
		this.noSamples = noSamples;
	}

	public int getNoSamples() {
		return noSamples;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	/**
	 * This main method is for testing only.
	 * @param args
	 */
	private static void main(String args[]) {

	    BasicConfigurator.configure();
	    Logger.getRootLogger().setLevel(Level.INFO);

		try {

			boolean help = false;

			for (int i = 0; i < args.length; i++)
				if (args[i].equals("-h"))
					help = true;

			if (help || args.length < 1) {
				System.out.println("Usage: java DataGen [-d <training>]");
				System.out.println();
				System.out.println("Options:");
				System.out.println();
				System.out.println("Data Sets:");
				System.out.println("\t-d <training>\t\tSpecify the training data for generation.");
				System.out
						.println("\t-t <testing>\t\tSpecify the testing data for generation. [default: no testing data]");
				System.out.println();
				System.out.println("Operations:");
				System.out.println("\t-n <noSamples>\t\tNo of samples of each numerical pdf used [default = 100]");
				System.out.println("\t-p <IntSize>\t\tUncertain interval size [default: 0.1]");
				System.out.println("\t-h\t\t\tHelp options");
				System.exit(1);
			}

			//default values
			String training = null;
			String testing = null;
			String nameFile = null;
			boolean test = false;
			boolean varies = false;
			double width = GlobalParam.DEFAULT_WIDTH;
			int noSamples = GlobalParam.DEFAULT_NO_SAMPLES;

			long seed = GlobalParam.DEFAULT_SEED;

			for (int i = 0; i < args.length; i++) {

				if (args[i].equals("-d")) {
					training = args[++i];
					continue;
				}

				if (args[i].equals("-t")) {
					test = true;
					testing = args[++i];
					continue;
				}

				if (args[i].equals("-f")) {
					nameFile = args[++i];
					continue;
				}
				
				if (args[i].equals("-n")) {
					noSamples = Integer.parseInt(args[++i]);
				}

				if (args[i].equals("-p")) {
					width = Double.parseDouble(args[++i]);
				}

				if (args[i].equals("-s")) {
					seed = Long.parseLong(args[++i]);
				}

			}

			if (training == null) {
				System.out
						.println("Please input training set using -d option.");
				System.exit(1);
			}
			
			if(nameFile == null){
				nameFile = training;
			}

			log.info("Start generation...");
			SampleDataGen gen = new SampleDataGen(training, nameFile, noSamples, seed, varies);
			log.info("Initialization...");
			double range[] = new double[gen.getNoAttr()];

			for (int i = 0; i < gen.getNoAttr(); i++)
				range[i] = width;

			gen.storeGeneratedData(training, range);

			if (test)
				gen.storeGeneratedTestData(testing, range);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}


}
