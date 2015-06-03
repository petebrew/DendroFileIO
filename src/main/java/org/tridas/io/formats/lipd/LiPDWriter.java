/**
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tridas.io.formats.lipd;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.formats.catras.CatrasFile;
import org.tridas.io.formats.csvmatrix.TridasToMatrixDefaults;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.FileHelper;
import org.tridas.io.util.FilePermissionException;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValues;

/**
 * Writer for the LiPD JSON file format.
 * 
 * @author peterbrewer
 */
public class LiPDWriter extends AbstractDendroCollectionWriter {
	private static final Logger log = LoggerFactory.getLogger(LiPDWriter.class);

	private INamingConvention naming = new NumericalNamingConvention();
	
	public LiPDWriter()
	{
		super(TridasMetadataFieldSet.class, new LiPDFormat());
	}
		
	@Override
	public void parseTridasContainer(TridasTridas argContainer,
			IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
		
		if (argContainer == null) {
			throw new ImpossibleConversionException("Tridas container is null!");
			
		}

		for(TridasProject project : argContainer.getProjects())
		{
			parseTridasProject(project, argDefaults);
		}
		 
	
		
	}
	
	@Override
	public void parseTridasProject(TridasProject p, IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
		
		if (p == null) {
			throw new ImpossibleConversionException("Project is null!");
			
		}

		TridasProject project = p;
		
		TridasToLiPDDefaults defaults = new TridasToLiPDDefaults();
		defaults.populateFromTridasProject(project);
		
		ArrayList<TridasObject> objects = TridasUtils.getObjectList(project);
		for (TridasObject object : objects) 
		{	
			TridasToLiPDDefaults odefaults = (TridasToLiPDDefaults) defaults.clone();
			odefaults.populateFromTridasObject(object);
			
			for (TridasElement element : object.getElements()) 
			{			
				TridasToLiPDDefaults edefaults = (TridasToLiPDDefaults) odefaults.clone();
				edefaults.populateFromTridasElement(element);
				
				for (TridasSample sample : element.getSamples()) 
				{
					
					for (TridasRadius radius : sample.getRadiuses()) 
					{
						
						for (TridasMeasurementSeries series : radius.getMeasurementSeries()) 
						{							
							TridasToLiPDDefaults mdefaults = (TridasToLiPDDefaults) edefaults.clone();
							
							mdefaults.setCollectionName(object.getTitle()+" "+element.getTitle()+" "+sample.getTitle()+" "+radius.getTitle()+" "+series.getTitle());
							
							
							LiPDFile file = new LiPDFile(mdefaults, series.getValues());
							
							for(TridasValues values : series.getValues())
							{
								file.addSeries(new TridasToMatrixDefaults(), series, values);
							}
							
							naming.registerFile(file, project, object, element, sample, radius, series);
							addToFileList(file);
						}
					}
				}
			}
		}
		

	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getNamingConvention()
	 */
	@Override
	public INamingConvention getNamingConvention() {
		return naming;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		naming = argConvension;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return null;
	}
	
	
	@Override
	public void saveFileToDisk(String argOutputFolder, String argFilename, IDendroFile argFile) throws FilePermissionException {
		
		FileHelper helper;
		
		boolean absolute = argOutputFolder.startsWith("/");
		
		if (absolute) {
			helper = new FileHelper(argOutputFolder);
		}
		else {
			helper = new FileHelper();
			argFilename = argOutputFolder + argFilename;
		}
		
		try {
			((LiPDFile) argFile).saveToDisk(helper.createOutput(argFilename + "." + argFile.getExtension()));
		} catch (Exception e) {
			log.error("Error saving file to disk", e);
		} 
	}
	

}
