package org.tridas.io.formats.vformat;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.vformat.VFormatToTridasDefaults.VFormatParameter;
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

public class VFormatWriter extends AbstractDendroCollectionWriter {
	
	private TridasToVFormatDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	public VFormatWriter() {
		super(TridasToVFormatDefaults.class);
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
		return I18n.getText("vformat.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("vformat.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("vformat.about.shortName");
	}
	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
		defaults = (TridasToVFormatDefaults) argDefaults;
		defaults.populateFromTridasProject(argProject);
		
		for (TridasObject o : TridasHierarchyHelper.getObjectList(argProject)) {
			TridasToVFormatDefaults objectDefaults = (TridasToVFormatDefaults) defaults.clone();
			objectDefaults.populateFromTridasObject(o);
			
			for (TridasElement e : o.getElements()) {
				TridasToVFormatDefaults elementDefaults = (TridasToVFormatDefaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);
				elementDefaults.populateFromTridasLocation(o, e);
				
				for (TridasSample s : e.getSamples()) {
					TridasToVFormatDefaults sampleDefaults = (TridasToVFormatDefaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					
					for (TridasRadius r : s.getRadiuses()) {
						TridasToVFormatDefaults radiusDefaults = (TridasToVFormatDefaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
							TridasToVFormatDefaults msDefaults = (TridasToVFormatDefaults) radiusDefaults
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
										case EARLYWOOD_WIDTH:
										case LATEWOOD_WIDTH:
											// Handled ok - but needs unit convertion
											tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);				
											break;		
										case LATEWOOD_PERCENT:
										case MAXIMUM_DENSITY:
											// All handled ok 
											// TODO handle unit conversion
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
								
								TridasToVFormatDefaults tvDefaults = (TridasToVFormatDefaults) msDefaults.clone();
								tvDefaults.populateFromTridasValues(tvsgroup);
								
								VFormatFile file = new VFormatFile(tvDefaults);
								
								// Add series to file
								file.setSeries(ms);
								file.setDataValues(tvsgroup);
								
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
			TridasToVFormatDefaults dsDefaults = (TridasToVFormatDefaults) defaults.clone();
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
						case EARLYWOOD_WIDTH:
						case LATEWOOD_WIDTH:
							// Handled ok - but needs unit convertion
							tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);				
							break;		
						case LATEWOOD_PERCENT:
						case MAXIMUM_DENSITY:
							// All handled ok 
							// TODO handle unit conversion
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
				
				TridasToVFormatDefaults tvDefaults = (TridasToVFormatDefaults) dsDefaults.clone();
				tvDefaults.populateFromTridasValues(tvsgroup);
				
				VFormatFile file = new VFormatFile(tvDefaults);
				
				// Add series to file
				file.setSeries(ds);
				file.setDataValues(tvsgroup);
								
				// Set naming convention
				naming.registerFile(file, argProject, ds);
				
				// Add file to list
				addToFileList(file);
			}
			
		}
	}
}
