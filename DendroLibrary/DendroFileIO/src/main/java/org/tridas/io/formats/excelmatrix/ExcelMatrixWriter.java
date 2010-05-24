package org.tridas.io.formats.excelmatrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.write.WriteException;

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.util.FileHelper;
import org.tridas.io.util.TridasHierarchyHelper;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;

public class ExcelMatrixWriter extends AbstractDendroCollectionWriter {
	
	private SimpleLogger log = new SimpleLogger(ExcelMatrixWriter.class);
	IMetadataFieldSet defaults;
	INamingConvention naming = new UUIDNamingConvention();
	
	public ExcelMatrixWriter() {
		super(TridasToExcelMatrixDefaults.class);
	}

	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
		defaults = argDefaults;
		
		ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
		
		// Grab all derivedSeries from project
		try{
			List<TridasDerivedSeries> lst = argProject.getDerivedSeries();	
			for (TridasDerivedSeries ds : lst)
			{
				// add to list
				seriesList.add(ds);
			}		
		} catch (NullPointerException e){}

	
		try{
			List<TridasMeasurementSeries> lst = (List<TridasMeasurementSeries>) TridasHierarchyHelper.getMeasurementSeriesFromTridasProject(argProject);
			for (TridasMeasurementSeries ser : lst)
			{
				// add to list
				seriesList.add(ser);
			}
		} catch (NullPointerException e){}
		
		// No series found
		if (seriesList.size()==0)
		{
			throw new IncompleteTridasDataException(I18n.getText("fileio.noData"));	
		}

		
		ExcelMatrixFile file = new ExcelMatrixFile(argDefaults, this);
		
		file.setSeriesList(seriesList);
		this.addToFileList(file);
		naming.registerFile(file, argProject, null);
		
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return this.defaults;
	}

	@Override
	public String getDescription() {
		return I18n.getText("excelmatrix.about.description");
	}

	@Override
	public String getFullName() {
		return I18n.getText("excelmatrix.about.fullName");
	}

	@Override
	public INamingConvention getNamingConvention() {
		return this.naming;
	}

	@Override
	public String getShortName() {
		return I18n.getText("excelmatrix.about.shortName");
	}

	@Override
	public void setNamingConvention(INamingConvention argConvention) {
		naming = argConvention;
	}
	
	@Override
	protected void saveFileToDisk(String argOutputFolder, String argFilename, IDendroFile argFile) {
		
		FileHelper helper;
		
		boolean absolute = argOutputFolder.startsWith("/");

		if(absolute){
			helper = new FileHelper(argOutputFolder);
		}else{
			helper = new FileHelper();
			argFilename = argOutputFolder+argFilename;
		}
		
		try {
			((ExcelMatrixFile)argFile).saveToDisk(helper.createOutput(argFilename+"."+argFile.getExtension()));
		} catch (WriteException e) {
			log.dbe(DebugLevel.L2_ERROR, e);
		} catch (IOException e) {
			log.dbe(DebugLevel.L2_ERROR, e);
		}
	}
}
