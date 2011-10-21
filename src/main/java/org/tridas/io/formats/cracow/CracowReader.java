package org.tridas.io.formats.cracow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.InvalidDendroFileException.PointerType;
import org.tridas.io.formats.catras.CatrasReader;
import org.tridas.io.formats.cracow.CracowToTridasDefaults.DefaultFields;
import org.tridas.io.util.FileHelper;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class CracowReader extends AbstractDendroFileReader {

	private static final Logger log = LoggerFactory.getLogger(CracowReader.class);
	private CracowToTridasDefaults defaults = new CracowToTridasDefaults();
	private ArrayList<Integer> ringWidthValues = null;
	
	public CracowReader() {
		super(CracowToTridasDefaults.class);
	}

	
	/**
	 * @throws IncorrectDefaultFieldsException
	 * @throws InvalidDendroFileException
	 * @see org.tridas.io.IDendroCollectionWriter#loadFile(java.lang.String)
	 */
	@Override
	public void loadFile(String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		FileHelper fileHelper = new FileHelper();
		log.debug("loading file from: " + argFilename);
		byte[] bytes = fileHelper.loadBytes(argFilename);
		if (bytes == null) {
			throw new IOException(I18n.getText("fileio.loadfailed"));
		}
		loadFile(bytes, argDefaultFields);
	}
	
	@Override
	public void loadFile(String argPath, String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		FileHelper fileHelper = new FileHelper(argPath);
		log.debug("loading file from: " + argFilename);
		byte[] bytes = fileHelper.loadBytes(argFilename);
		if (bytes == null) {
			throw new IOException(I18n.getText("fileio.loadfailed"));
		}
		loadFile(bytes, argDefaultFields);
	}
	
	public void loadFile(byte[] argFileBytes, IMetadataFieldSet argDefaults) throws IncorrectDefaultFieldsException,
			InvalidDendroFileException {
		if (!argDefaults.getClass().equals(getDefaultFieldsClass())) {
			throw new IncorrectDefaultFieldsException(getDefaultFieldsClass());
		}
		parseFile(argFileBytes, argDefaults);
	}
	
	public void loadFile(byte[] argFileBytes) throws InvalidDendroFileException {
		parseFile(argFileBytes, constructDefaultMetadata());
	}
	
	
	/**
	 * @param argFileBytes
	 * @param argDefaultFields
	 */
	protected void parseFile(byte[] argFileBytes, IMetadataFieldSet argDefaultFields) throws InvalidDendroFileException 
	{
		checkFile(argFileBytes);
		defaults = (CracowToTridasDefaults) argDefaultFields;
		ringWidthValues = new ArrayList<Integer>();
		
		// Extract sapwood count 
		Integer startSW = CatrasReader.getIntFromByte(argFileBytes[1]);
		Integer endSW = CatrasReader.getIntFromByte(argFileBytes[3]);
		if(startSW!=0 && endSW!=0)
		{
			if(endSW>startSW)
			{
				defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).setValue(endSW-startSW+1);
			}
		}
		
		
		for(int i=6; i<argFileBytes.length; i=i+2)
		{
			Integer largeNum = CatrasReader.getIntFromByte(argFileBytes[i]);
			Integer smallNum = CatrasReader.getIntFromByte(argFileBytes[i+1]);
			
			Integer val = (largeNum*100) + smallNum;
			
			//System.out.println("Ring value = "+val);
			
			ringWidthValues.add(val);
		}
		
		defaults.getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(ringWidthValues.size());
		
	}
	
	/**
	 * Check this is a valid Cracow file
	 * 
	 * @param argFileBytes
	 * @throws InvalidDendroFileException
	 */
	protected void checkFile(byte[] argFileBytes) throws InvalidDendroFileException{
	
		if (argFileBytes == null) {
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 
					1, PointerType.BYTE );
		}
		else if (argFileBytes.length < 10) {
			throw new InvalidDendroFileException(I18n.getText("fileio.tooShort"), 
					argFileBytes.length, PointerType.BYTE );
		}
		
		
		//CatrasReader.debugAsIntSingleByte(0, argFileBytes.length-2, argFileBytes);
		//CatrasReader.debugAsIntBytePairs(0, argFileBytes.length, argFileBytes);

		
		
		// Bytes 1, 3, 5, 6 should all be zero
		if(CatrasReader.getIntFromByte(argFileBytes[0])!=0)
		{
			throw new InvalidDendroFileException(I18n.getText("cracow.invalidSignature"), 
					0, PointerType.BYTE );
		}
		
		if(CatrasReader.getIntFromByte(argFileBytes[2])!=0)
		{
			throw new InvalidDendroFileException(I18n.getText("cracow.invalidSignature"), 
					2, PointerType.BYTE );
		}
		
		if(CatrasReader.getIntFromByte(argFileBytes[4])!=0)
		{
			throw new InvalidDendroFileException(I18n.getText("cracow.invalidSignature"), 
					4, PointerType.BYTE );
		}
		
		if(CatrasReader.getIntFromByte(argFileBytes[5])!=0)
		{
			throw new InvalidDendroFileException(I18n.getText("cracow.invalidSignature"), 
					5, PointerType.BYTE );
		}
			
		
	}
	
	// *******************************
	// NOT SUPPORTED - BINARY FORMAT
	// *******************************
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) {
		throw new UnsupportedOperationException(I18n.getText("general.binaryNotText"));
	}
	
	@Override
	public void loadFile(String[] argFileStrings) throws InvalidDendroFileException {
		throw new UnsupportedOperationException("Binary file type, cannot load from strings");
	}
	

	@Override
	protected void resetReader() {
		defaults = null;
		ringWidthValues = null;

	}

	@Override
	public int getCurrentLineNumber() {
		return 0;
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { "AVR", "AVS" };
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("cracow.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("cracow.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("cracow.about.shortName");
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	private TridasProject getProject() {
		
		// Create entities
		TridasProject p = defaults.getProjectWithDefaults();
		TridasObject o = defaults.getObjectWithDefaults();
		TridasElement e = defaults.getElementWithDefaults();
		TridasSample s = defaults.getSampleWithDefaults();
		TridasRadius r = defaults.getRadiusWithDefaults(false);
		
		ITridasSeries series;
		
		// Compile TridasValues array
		ArrayList<TridasValue> tridasValues = new ArrayList<TridasValue>();
		for (Integer intval : ringWidthValues) {
			TridasValue v = new TridasValue();
			v.setValue(String.valueOf(intval));
			tridasValues.add(v);
		}
		TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
		valuesGroup.setValues(tridasValues);
		ArrayList<TridasValues> vlist = new ArrayList<TridasValues>();
		vlist.add(valuesGroup);
				
		ArrayList<TridasSample> sList = new ArrayList<TridasSample>();
		sList.add(s);
		e.setSamples(sList);
		
		ArrayList<TridasElement> eList = new ArrayList<TridasElement>();
		eList.add(e);
		o.setElements(eList);
		
		ArrayList<TridasObject> oList = new ArrayList<TridasObject>();
		oList.add(o);		
		p.setObjects(oList);
		
		// Now build up our measurementSeries
		series = defaults.getDefaultTridasMeasurementSeries();

		// Compile project
		series.setValues(vlist);
		
		ArrayList<TridasMeasurementSeries> seriesList = new ArrayList<TridasMeasurementSeries>();
		seriesList.add((TridasMeasurementSeries) series);
		r.setMeasurementSeries(seriesList);
	
		ArrayList<TridasRadius> rList = new ArrayList<TridasRadius>();
		rList.add(r);
		s.setRadiuses(rList);
		
		return p;
		
	}
	
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getProjects()
	 */
	@Override
	public TridasProject[] getProjects() {
		TridasProject projects[] = new TridasProject[1];
		projects[0] = this.getProject();
		return projects;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getTridasContainer()
	 */
	public TridasTridas getTridasContainer() {
		TridasTridas container = new TridasTridas();
		List<TridasProject> list = Arrays.asList(getProjects());
		container.setProjects(list);
		return container;
	}
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"AVR"};
		
		return new DendroFileFilter(exts, getShortName());

	}

}
