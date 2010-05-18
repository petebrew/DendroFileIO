package org.tridas.io.formats.excelmatrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.write.WriteException;

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

	/**
	 * Save all associated files to disk
	 */
	@Override
	public void saveAllToDisk(String argOutputFolder){
	
		FileHelper helper;
		
		boolean absolute = argOutputFolder.startsWith("/") || argOutputFolder.startsWith("\\");
		// add ending file separator
		if(!argOutputFolder.endsWith("\\") && !argOutputFolder.endsWith("/") && argOutputFolder.length() != 0){
			argOutputFolder += File.separatorChar;
		}
		
		if(absolute){
			helper = new FileHelper(argOutputFolder);
		}else{
			helper = new FileHelper();
		}
		
		for (IDendroFile dof: fileList){
			
			String filename = null;
			if(absolute){
				filename = getNamingConvention().getFilename(dof)+"."+dof.getExtension();
			}else{
				filename = argOutputFolder+getNamingConvention().getFilename(dof)+"."+dof.getExtension();
			}
			
			try {
				((ExcelMatrixFile)dof).saveToDisk(helper.createOutput(filename));
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
		
	}
	
}
