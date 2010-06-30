package org.tridas.io.formats.besancon;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.TridasHierarchyHelper;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

public class BesanconWriter extends AbstractDendroCollectionWriter {

	private TridasToBesanconDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	public BesanconWriter() {
		super(TridasToBesanconDefaults.class);
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
	public void setNamingConvention(INamingConvention argConvention) {
		naming = argConvention;
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
		return I18n.getText("besancon.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("besancon.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("besancon.about.shortName");
	}

	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
		defaults = (TridasToBesanconDefaults) argDefaults;
		defaults.populateFromTridasProject(argProject);
		
		for (TridasObject o : TridasHierarchyHelper.getObjectList(argProject)) {
			TridasToBesanconDefaults objectDefaults = (TridasToBesanconDefaults) defaults.clone();
			objectDefaults.populateFromTridasObject(o);
			
			for (TridasElement e : o.getElements()) {
				TridasToBesanconDefaults elementDefaults = (TridasToBesanconDefaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);
				
				for (TridasSample s : e.getSamples()) {
					TridasToBesanconDefaults sampleDefaults = (TridasToBesanconDefaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					
					for (TridasRadius r : s.getRadiuses()) {
						TridasToBesanconDefaults radiusDefaults = (TridasToBesanconDefaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
							TridasToBesanconDefaults msDefaults = (TridasToBesanconDefaults) radiusDefaults
									.clone();
							msDefaults.populateFromTridasMeasurementSeries(ms);
							msDefaults.populateFromWoodCompleteness(ms, r);
							
							for (int i = 0; i < ms.getValues().size(); i++) {
								boolean skipThisGroup = false;

								TridasValues tvsgroup = ms.getValues().get(i);
								
								// Check we can handle this variable
								if(tvsgroup.isSetVariable())
								{
									if (!tvsgroup.getVariable().isSetNormalTridas())
									{
										this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
									}
									else
									{
										switch(tvsgroup.getVariable().getNormalTridas())
										{
										case RING_WIDTH:
											// Able to handle this variable but need to convert units
											tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);											
											
											break;
										default:
											// All other variables not representable
											this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().toString().toLowerCase().replace("_", " "))));
											skipThisGroup = true;
										}
									}
								}
								
								// Dodgy variable so skip
								if(skipThisGroup) continue;
								
								TridasToBesanconDefaults tvDefaults = (TridasToBesanconDefaults) msDefaults.clone();
								tvDefaults.populateFromTridasValues(tvsgroup);
								
								
								BesanconFile file = new BesanconFile();
								
								// Add series to file	
								file.addSeries(ms, tvDefaults);
								
								// Set naming convention
								naming.registerFile(file, argProject, o, e, s, r, ms);
								
								// Add file to list
								addToFileList(file);
							}
							
						}
					}
				}
			}
		}
		
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) {
			TridasToBesanconDefaults dsDefaults = (TridasToBesanconDefaults) defaults.clone();
			dsDefaults.populateFromTridasDerivedSeries(ds);
			
			
			for (int i = 0; i < ds.getValues().size(); i++) {
				boolean skipThisGroup = false;

				TridasValues tvsgroup = ds.getValues().get(i);
				
				// Check we can handle this variable
				if(tvsgroup.isSetVariable())
				{
					if (!tvsgroup.getVariable().isSetNormalTridas())
					{
						this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
					}
					else
					{
						switch(tvsgroup.getVariable().getNormalTridas())
						{
						case RING_WIDTH:
							// Able to handle this variable but need to convert units
							tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);											
							
							break;
						default:
							// All other variables not representable
							this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().toString().toLowerCase().replace("_", " "))));
							skipThisGroup = true;
						}
					}
				}
				
				// Dodgy variable so skip
				if(skipThisGroup) continue;
				
				TridasToBesanconDefaults tvDefaults = (TridasToBesanconDefaults) dsDefaults.clone();
				tvDefaults.populateFromTridasValues(tvsgroup);
				
				BesanconFile file = new BesanconFile();
				
				// Add series to file	
				file.addSeries(ds, tvDefaults);
								
				// Set naming convention
				naming.registerFile(file, argProject, ds);
				
				// Add file to list
				addToFileList(file);
			}
			
		}
		
	}


}
