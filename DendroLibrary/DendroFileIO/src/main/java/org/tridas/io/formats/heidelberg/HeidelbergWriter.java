package org.tridas.io.formats.heidelberg;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.util.TridasHierarchyHelper;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

/**
 * Writer for heidelberg files.  Can only handle basic tree and chronology data, doesn't 
 * handle other measurements like earlywood and latewood.
 * 
 * @author daniel
 *
 */
public class HeidelbergWriter extends AbstractDendroCollectionWriter {

	private TridasToHeidelbergDefaults defaults;
	private INamingConvention naming = new HierarchicalNamingConvention();
	
	public HeidelbergWriter(){
		super(TridasToHeidelbergDefaults.class);
	}

	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
		defaults = (TridasToHeidelbergDefaults) argDefaults;
		defaults.populateFromTridasProject(argProject);
		
		for(TridasObject o : TridasHierarchyHelper.getObjectList(argProject)){
			
			for(TridasElement e : o.getElements()){
				TridasToHeidelbergDefaults elementDefaults = (TridasToHeidelbergDefaults) defaults.clone();
				elementDefaults.populateFromTridasElement(e);
				
				for(TridasSample s : e.getSamples()){
					
					for(TridasRadius r : s.getRadiuses()){
						
						for(TridasMeasurementSeries ms :r.getMeasurementSeries()){
							TridasToHeidelbergDefaults msDefaults = (TridasToHeidelbergDefaults) elementDefaults.clone();
							msDefaults.populateFromMS(ms);
							
							for(int i=0; i< ms.getValues().size(); i++){
								TridasValues tvs = ms.getValues().get(i);
								TridasToHeidelbergDefaults tvDefaults = (TridasToHeidelbergDefaults) msDefaults.clone();
								tvDefaults.populateFromTridasValues(tvs);
								
								HeidelbergFile file = new HeidelbergFile(this, tvDefaults);
								file.setSeries(ms, i);
								naming.registerFile(file, argProject, o, e, s, r, ms);
								addToFileList(file);
							}
							
						}
					}
					
					/*if( s.isSetRadiusPlaceholder()){
						// we have to search through all derived series to find the one matching our placeholder id
						for(TridasDerivedSeries ds : argProject.getDerivedSeries()){
							if(ds.getId() == null){
								throw new IncompleteTridasDataException("Id in derived series was null");
							}
							if(ds.getId().equals(s.getRadiusPlaceholder().getMeasurementSeriesPlaceholder().getId())){
								TridasToHeidelbergDefaults dsDefaults = (TridasToHeidelbergDefaults) elementDefaults.clone();
								dsDefaults.populateFromDerivedSeries(ds);
								
								for(int i=0; i< ds.getValues().size(); i++){
									TridasValues tvs = ds.getValues().get(i);
									TridasToHeidelbergDefaults tvDefaults = (TridasToHeidelbergDefaults) dsDefaults.clone();
									tvDefaults.populateFromTridasValues(tvs);
									
									HeidelbergFile file = new HeidelbergFile(this, tvDefaults);
									file.setSeries(ds, i);
									naming.registerFile(file, argProject, ds);
									addToFileList(file);
								}
							}
						}
					}*/
				}
			}
		}
		
		for(TridasDerivedSeries ds : argProject.getDerivedSeries()){
			TridasToHeidelbergDefaults dsDefaults = (TridasToHeidelbergDefaults) defaults.clone();
			dsDefaults.populateFromDerivedSeries(ds);
			
			for(int i=0; i< ds.getValues().size(); i++){
				TridasValues tvs = ds.getValues().get(i);
				TridasToHeidelbergDefaults tvDefaults = (TridasToHeidelbergDefaults) dsDefaults.clone();
				tvDefaults.populateFromTridasValues(tvs);
				
				HeidelbergFile file = new HeidelbergFile(this, tvDefaults);
				file.setSeries(ds, i);
				naming.registerFile(file, argProject, ds);
				addToFileList(file);
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
		return defaults;
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("heidelberg.about.description");
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("heidelberg.about.fullName");
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("heidelberg.about.shortName");
	}

}
