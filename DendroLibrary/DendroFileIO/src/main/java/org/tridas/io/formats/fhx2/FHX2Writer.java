package org.tridas.io.formats.fhx2;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NamingConventionGrouper;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.Certainty;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.NormalTridasRemark;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRemark;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasWoodCompleteness;

public class FHX2Writer extends AbstractDendroCollectionWriter {
	private static final Logger log = LoggerFactory.getLogger(FHX2Writer.class);

	IMetadataFieldSet defaults;
	INamingConvention naming = new NumericalNamingConvention();
	
	public FHX2Writer() {
		super(TridasToFHX2Defaults.class, new FHX2Format());
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
		
		ArrayList<FHX2Series> seriesList = new ArrayList<FHX2Series>();
		
		// Grab all derivedSeries from project
		/*try {
			List<TridasDerivedSeries> lst = argProject.getDerivedSeries();
			for (TridasDerivedSeries ds : lst) {
				
				// add to list
				seriesList.add(ds);
				
				for(TridasValues tvsgroup : ds.getValues())
				{
					if(!tvsgroup.isSetValues())
					{
						this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.noDataValues")));
					}
				}
				
			}
		} catch (NullPointerException e) {}
		*/
		
		FHX2File file = new FHX2File();
		TridasToFHX2Defaults defaults = new TridasToFHX2Defaults();
		NamingConventionGrouper ncgroup = new NamingConventionGrouper();
		ncgroup.add(argProject);
		
		try {
			for (TridasObject o : TridasUtils.getObjectList(argProject)) {
				ncgroup.add(o);
				defaults.populateFromTridasObject(o);
				
				
				for (TridasElement e : o.getElements()) {
					ncgroup.add(e);
					defaults.populateFromTridasElement(e);
					
					for (TridasSample s : e.getSamples()) {
						ncgroup.add(s);
						defaults.populateFromTridasSample(s);
						
						for (TridasRadius r : s.getRadiuses()) {
							ncgroup.add(r);
													
							for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
								ncgroup.add(ms);
								
								if(!ms.isSetInterpretation())
								{
									log.warn("Dating information is missing from this series.");
									this.addWarning(new ConversionWarning(WarningType.IGNORED, "Dating information is missing from this series."));
									continue;
								}	
								
								if(!ms.getInterpretation().isSetFirstYear())
								{
									log.warn("First year information is missing from this series.");
									this.addWarning(new ConversionWarning(WarningType.IGNORED, "First year information is missing from this series."));
									continue;
								}
								
								if((!ms.isSetValues()) || !ms.getValues().get(0).isSetValues())
								{
									log.warn("This series doesn't include data values.");
									this.addWarning(new ConversionWarning(WarningType.IGNORED, "This series doesn't include data values."));
									continue;
								}
								
								Boolean hasFireFlags = false;
								for(TridasValue v : ms.getValues().get(0).getValues())
								{
									if(v.isSetRemarks())
									{
										for(TridasRemark remark : v.getRemarks())
										{
											Boolean ringRemarkFound = false;
											if((remark.isSetNormalTridas() && remark.getNormalTridas().equals(NormalTridasRemark.FIRE_DAMAGE))||
											   (remark.isSetNormalStd() && remark.getNormalStd().equals(FHX2File.FHX_DOMAIN))
											  )
											{
												hasFireFlags = true;
												if(ringRemarkFound) this.addWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, "Multiple fire events/remarks found in a single ring.  FHX2 format can only represent one remark per ring"));
												ringRemarkFound = true;
											}
										}
									}
								}
								
								if(!hasFireFlags)
								{
									log.warn("This series doesn't include fire history data.");
									this.addWarning(new ConversionWarning(WarningType.IGNORED, "This series doesn't include fire history data."));
									//continue;
								}
								
								TridasWoodCompleteness wc = null;
								if(ms.isSetWoodCompleteness())
								{
									wc = ms.getWoodCompleteness();
								}
								else if (r.isSetWoodCompleteness())
								{
									wc = r.getWoodCompleteness();
								}
								else
								{
									this.addWarning(new ConversionWarning(WarningType.IGNORED, "This series does not include required information about pith and bark"));
									continue;
								}	
								
										
								FHX2Series series = new FHX2Series(ms, wc);
								seriesList.add(series);
								
							}
						}
					}		
				}
			}

		} catch (NullPointerException e) {}
		
		// No series found
		if (seriesList.size() == 0) {
			clearWarnings();
			throw new ImpossibleConversionException(I18n.getText("fileio.noData"));
		}
				
		file.setSeriesList(seriesList);
		addToFileList(file);
		file.setDefaults(defaults);
		naming.registerFile(file, argProject, null);

	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
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
	
	public class FHX2Series
	{
		public ITridasSeries series;
		public TridasWoodCompleteness wc;
		
		public FHX2Series(ITridasSeries series, TridasWoodCompleteness wc)
		{
			this.series =series;
			this.wc = wc;
		}
		
		/**
		 * Determine if we know where the pith is
		 * 
		 * @return
		 */
		public Boolean isPithKnown()
		{
			if(series.isSetInterpretation())
			{
				if(series.getInterpretation().isSetPithYear())
				{
					if(series.getInterpretation().getPithYear().getCertainty().equals(Certainty.EXACT))
					{
						return true;
					}
				}
			}
			if(wc.isSetPith())
			{
				TridasPith pith = wc.getPith();
				if(pith.getPresence().equals(ComplexPresenceAbsence.COMPLETE))
				{
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Determine if we know where the bark is
		 * 
		 * @return
		 */
		public Boolean isBarkKnown()
		{
			if(series.isSetInterpretation())
			{
				if(series.getInterpretation().isSetDeathYear())
				{
					if(series.getInterpretation().getDeathYear().getCertainty().equals(Certainty.EXACT))
					{
						return true;
					}
				}
			}
			if(wc.isSetBark())
			{
				TridasBark bark = wc.getBark();
				if(bark.getPresence().equals(PresenceAbsence.PRESENT))
				{
					return true;
				}
			}
			return false;
		}
		
		public SafeIntYear getFirstYear()
		{
			if(!series.getInterpretation().isSetFirstYear())
			{
				log.warn("No first year in series");
				return null;
			}
			else
			{
				return new SafeIntYear(series.getInterpretation().getFirstYear());
			}
		
		}
		
		public SafeIntYear getLastYear()
		{
			if(series.getInterpretation().isSetLastYear())
			{
				return new SafeIntYear(series.getInterpretation().getLastYear());
			}
			else
			{
				return getFirstYear().add(series.getValues().get(0).getValues().size());
			}
		}
		
		public TridasValue getValueForYear(SafeIntYear year)
		{			
			if(year.compareTo(getFirstYear())<0) return null;
			if(year.compareTo(getLastYear())>0) return null;
			
			int difference = year.diff(getFirstYear());
			
			try{
				return series.getValues().get(0).getValues().get(difference);
			} catch (Exception e)
			{
				return null;
			}
		}
	}

}
