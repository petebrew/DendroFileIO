package org.tridas.io.formats.csvmatrix;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.AbstractDendroFormat;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.formats.heidelberg.HeidelbergReader;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults;
import org.tridas.io.formats.nottingham.NottinghamFormat;
import org.tridas.io.formats.nottingham.NottinghamToTridasDefaults;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;

import au.com.bytecode.opencsv.CSVReader;

public class CSVMatrixReader extends AbstractDendroFileReader {

	private int currentLineNum = 0;
	private MatrixToTridasDefaults defaults;
	private static final Logger log = LoggerFactory.getLogger(CSVMatrixReader.class);

	public CSVMatrixReader() {
		super(MatrixToTridasDefaults.class, new CSVMatrixFormat());
	}

	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		log.debug("Parsing: " + argFileString);
		defaults = (MatrixToTridasDefaults) argDefaultFields;
		
		if(argFileString==null || argFileString.length==0) throw new InvalidDendroFileException("File is empty");
		
		String fileString="";
		for(String line : argFileString)
		{
			fileString+=line+System.lineSeparator();
		}
		
		InputStream is = new ByteArrayInputStream(fileString.getBytes(StandardCharsets.UTF_8));
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		CSVReader reader = new CSVReader(br);
		try {
			List myEntries = reader.readAll();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	@Override
	protected void resetReader() {
		currentLineNum = 0;
		defaults = null;
	}

	@Override
	public int getCurrentLineNumber() {
		return this.currentLineNum;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public TridasProject[] getProjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TridasTridas getTridasContainer() {
		// TODO Auto-generated method stub
		return null;
	}

}
