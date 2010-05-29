package org.tridas.io.formats.trims;

import java.util.ArrayList;
import java.util.List;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.util.TridasHierarchyHelper;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRadiusPlaceholder;
import org.tridas.schema.TridasSample;

public class TrimsWriter extends AbstractDendroCollectionWriter {

	IMetadataFieldSet defaults;
	INamingConvention naming = new UUIDNamingConvention();
	
	public TrimsWriter() {
		super(TridasToTrimsDefaults.class);
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
				// Create a TrimsFile for each and add to file list
				TrimsFile file = new TrimsFile(defaults);
				naming.registerFile(file, argProject, ds);
				file.setSeries(ds);
				this.addToFileList(file);
			}		
		} catch (NullPointerException e){}

		// Loop through Objects
		List<TridasObject> obList;
		try{
			obList = argProject.getObjects();
			} catch (NullPointerException e)
			{
				throw new IncompleteTridasDataException(I18n.getText("fileio.objectMissing"));
			}	
		for (TridasObject obj : obList)
		{
			
			// Loop through Elements
			ArrayList<TridasElement> elList;
			try{
				elList = TridasHierarchyHelper.getElementList(obj);
				} catch (NullPointerException e)
				{
					throw new IncompleteTridasDataException(I18n.getText("fileio.elementMissing"));
				}
			
			for(TridasElement el : elList)
			{
				// Loop through Samples
				List<TridasSample> sList;
				try{
					sList = el.getSamples();
				} catch (NullPointerException e)
				{
					throw new IncompleteTridasDataException(I18n.getText("fileio.sampleMissing"));
				}
				
				for(TridasSample s : sList)
				{				
					// Check this isn't a placeholder
					TridasRadiusPlaceholder rph = null;
					try{
						rph = s.getRadiusPlaceholder();
					} catch (NullPointerException e){}
					
					if(rph!=null) {	continue; }
				
					// Loop through radii
					List<TridasRadius> rList;
					try{
						rList = s.getRadiuses();
					} catch (NullPointerException e)
					{
						throw new IncompleteTridasDataException(I18n.getText("fileio.radiusMissing"));
					}
					
					for(TridasRadius r : rList)
					{
						// Loop through series
						List<TridasMeasurementSeries> serList = null;
						try{
							serList = r.getMeasurementSeries();
						} catch (NullPointerException e) {}

						if(serList!=null)
						{
							for (TridasMeasurementSeries ser : serList)
							{
								// Create a TrimsFile for each and add to file list
								TrimsFile file = new TrimsFile(defaults);
								naming.registerFile(file, argProject, obj, el, s, r, ser);
								file.setSeries(ser);
								this.addToFileList(file);	
							}
						}	
					}
				}	
			}
		}
		

		// No series found
		if (this.getFileList().size()==0)
		{
			throw new IncompleteTridasDataException(I18n.getText("fileio.noData"));	
		}
		
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return this.defaults;
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("trims.about.description");
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("trims.about.fullName");
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("trims.about.shortName");
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

}
