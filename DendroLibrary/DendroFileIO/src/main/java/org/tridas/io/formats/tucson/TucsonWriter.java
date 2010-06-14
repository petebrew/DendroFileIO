package org.tridas.io.formats.tucson;

import java.util.List;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.util.TridasHierarchyHelper;
import org.tridas.io.warningsandexceptions.IncompleteTridasDataException;
import org.tridas.io.warningsandexceptions.UnrepresentableTridasDataException;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

/**
 * Writer for the Tucson file format.
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 */
public class TucsonWriter extends AbstractDendroCollectionWriter {
	
	TridasToTucsonDefaults defaults;
	INamingConvention naming = new HierarchicalNamingConvention();
	
	/**
	 * Standard constructor
	 */
	public TucsonWriter() {
		super(TridasToTucsonDefaults.class);
	}
	

	@Override
	public void parseTridasProject(TridasProject p, IMetadataFieldSet argDefaults) 
	throws IncompleteTridasDataException, UnrepresentableTridasDataException {
	
		// Base defaults for all the output files
		defaults = (TridasToTucsonDefaults) argDefaults;
		
		// Set project level fields
		defaults.populateFromTridasProject(p);
				
		// Extract any TridasDerivedSeries from project
		List<TridasDerivedSeries> dsList = null;
		try { dsList = p.getDerivedSeries();
		} catch (NullPointerException e) {}
				
		if (dsList.size()>0) 
		{
			/**
			 * CHRONOLOGY FILE
			 *
			 * There is a derived series in this project so we will be creating a .crn file.
			 * 
			 */
		
			for (TridasDerivedSeries ds : dsList) 
			{
				TucsonFile file = new TucsonFile(defaults);
				file.addSeries(ds);
				naming.registerFile(file, p, ds);
				addToFileList(file);
			}
			
		}
		
		if (TridasHierarchyHelper.getMeasurementSeriesFromTridasProject(p).size()>0)
		{
			/**
			 * RWL FILE(S)
			 * 
			 * The project contains one or more measurement series so we will save these
			 * to one or more RWL files.  RWL files should contain only one batch of metadata, 
			 * so we create a new file for each object.
			 */
			
			for (TridasObject o : p.getObjects()) {
				
				// Clone defaults and set fields specific to this object
				TridasToTucsonDefaults objectDefaults = (TridasToTucsonDefaults) defaults.clone();
				objectDefaults.populateFromTridasObject(o);
				
				for (TridasElement e : TridasHierarchyHelper.getElementList(o)) {
					TridasToTucsonDefaults elementDefaults = (TridasToTucsonDefaults) objectDefaults.clone();
					elementDefaults.populateFromTridasElement(e);
					
					for (TridasSample s : e.getSamples()) {
						
						for (TridasRadius r : s.getRadiuses()) {
							
							for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
								TridasToTucsonDefaults msDefaults = (TridasToTucsonDefaults) elementDefaults
										.clone();
								msDefaults.populateFromTridasMeasurementSeries(ms);
								
								for (int i = 0; i < ms.getValues().size(); i++) {
									TridasValues tvs = ms.getValues().get(i);
									
									// Check there are no non-number values
									for (TridasValue v : tvs.getValues()) {
										try {
											Integer.parseInt(v.getValue());
										} catch (NumberFormatException e2) {
											throw new IncompleteTridasDataException(
													"One or more data values are not numbers!  This is technically acceptable in TRiDaS but not supported in this library.");
										}
									}
									
									TridasToTucsonDefaults tvDefaults = (TridasToTucsonDefaults) msDefaults.clone();
									tvDefaults.populateFromTridasValues(tvs);
									
									TucsonFile file = new TucsonFile(tvDefaults);
									file.addSeries(ms);
									naming.registerFile(file, p, o, e, s, r, ms);
									addToFileList(file);
								}
							}
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
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("tucson.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("tucson.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("tucson.about.shortName");
	}
}
