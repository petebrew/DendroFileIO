package org.tridas.io.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.formats.catras.CatrasReader;


public class CatrasSpeciesList {
	private static final Logger log = LoggerFactory.getLogger(CatrasSpeciesList.class);

	/**
	 * Simple standalone util for extracting code numbers from a CATRAS 
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length!=2){
			System.out.println("Need two arguments, the first the input filename and the second the output filename");
			System.exit(1);
		}
		
		String argFilename = args[0];
		String outputFilename = args[1];
		
		FileHelper fileHelper = new FileHelper();
		log.debug("loading file from: " + argFilename);
		byte[] bytes = fileHelper.loadBytes(argFilename);
		
		fileHelper.createFile(outputFilename);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(outputFilename));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
		try {
			writer.write("Taxon" + "\t"+"Code"+"\n");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
  		
		for (int i=15 ; i < bytes.length; i=i+16)
		{
			
			try {
				

				byte bt = bytes[i];
				int code = CatrasReader.getIntFromByte(bt);
				
				if(code==0) continue;
				
				int start = i-15;
				int end = i;
				
				String name = new String(CatrasReader.getSubByteArray(bytes, start, end), "Cp437").trim();
				log.debug(name + " = "+code);
				writer.write(name + "\t"+code+"\n");


			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
