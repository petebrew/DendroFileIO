package org.tridas.io.formats.tucson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.TridasHierarchyHelper;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.SeriesLink.IdRef;


/**
 * Writer for the Tucson file format.  
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 *
 */
public class TucsonWriter extends AbstractDendroCollectionWriter {

	IMetadataFieldSet defaults;
	INamingConvention naming = new HierarchicalNamingConvention();

	/**
	 * Standard constructor
	 */
	public TucsonWriter(){
		super(TridasToTucsonDefaults.class);
	}
	
	@SuppressWarnings("null")
	@Override
	public void parseTridasProject(TridasProject p, IMetadataFieldSet argDefaults) throws IncompleteTridasDataException{
		defaults = argDefaults;
		
		List<TridasObject> objList =null;
		List<TridasDerivedSeries> dsList =null;
		
		// See if this project contains any TridasDerivedSeries
		try	{dsList = p.getDerivedSeries(); } 
			catch(NullPointerException e){ }
		if(dsList!=null)
		{	
			// It does, so this is a chronology file!
			/**
			 * TODO 
			 * 
			 * 1) Get a list of series ID's to which this derivedSeries links
			 * 2) Drill down through project to get these series
			 * 3) Compare TridasObjects of these series and if they are the same 
			 * 
			 * Could this be done with XPath?  Seems like it would be much easier.
			 * 
			 */
			
			// Get a list of derivedSeries in the project
			for(TridasDerivedSeries ds : dsList)
			{
				// List of refs in the linkSeries tag of our derivedSeries
				Set<String> refsInDSeries = new HashSet<String>();  
				
				ArrayList<Object> lstRefedSeries = new ArrayList<Object>();
				
				// Get all the SeriesLinks for this DerivedSeries
				try{List<SeriesLink> linkedToSeries = ds.getLinkSeries().getSeries();
			
						// Compile list of IdRefs
						for (SeriesLink sl : linkedToSeries)
						{
							/*try{String thisid = sl.getIdRef().getRef().toString();
							refsInDSeries.add(thisid);
							} 
							catch (NullPointerException e3){}
							*/
							Object blah = sl.getIdRef().getRef();
							lstRefedSeries.add(blah);
							
						}
					}
					catch(NullPointerException e){
						throw new IncompleteTridasDataException(I18n.getText("tucson.linkSeriesMissing"));
					}
					
				// Get all the top level objects associated with the project
				try	{objList = p.getObjects(); } 
					catch(NullPointerException e){ throw new IncompleteTridasDataException("TridasObject(s) missing");}
			
				// Loop through the objects 
				for(TridasObject o : objList)
				{
					// List of refs of measurementSeries in this object
					Set<IdRef> refsInObject = new HashSet<IdRef>();
					
					// Grab all the MeasurementSeries within this object
					Set<TridasMeasurementSeries> mseriesForObj = TridasHierarchyHelper.getMeasurementSeriesFromTridasObject(o);
					
					// Loop through these MeasurementSeries 
					Iterator<TridasMeasurementSeries> it = mseriesForObj.iterator();
					while(it.hasNext())
					{
						// Check whether the MeasurementSeries ID is in our list of IDs that
						// we obtained from the DerivedSeries.LinkSeries tag
						String seriesID = it.next().getId();	
						if (refsInDSeries.contains(seriesID)) System.out.println("Found one!");
					}
					
				}
				
			}
			
			
			
		}
		
		// 
		// This project must contain TridasMeasurementSeries instead
		//
		
		// Get all the top level objects
		try	{objList = p.getObjects(); } 
			catch(NullPointerException e){ throw new IncompleteTridasDataException(I18n.getText("fileio.objectMissing"));}
				
		// Loop through them parsing each one
		for(TridasObject o : objList)
		{
			try { parseObject(p, o);}
				catch(ConversionWarningException w){ this.addWarningToList(w.getWarning());};			
		}

	}
	
	/**
	 * Creates a new TucsonFile for the object.  Tucson files can contain multiple series in one file, but 
	 * one file can only contain one batch of metadata.  This means that one Tucson file can only contain
	 * series for a single TridasObject.  If there are sub-objects in the project we use metadata from the
	 * top level object and ignore the rest.  
	 *  
	 * @param o
	 * @param topLevel
	 * @throws IncompleteTridasDataException
	 * @throws ConversionWarningException 
	 */
	private void parseObject(TridasProject p, TridasObject o) throws IncompleteTridasDataException, ConversionWarningException{
		parseObject(p, o, true);
	}
	
	/**
	 * Creates a new TucsonFile for the object.  Tucson files can contain multiple series in one file, but 
	 * one file can only contain one batch of metadata.  This means that one Tucson file can only contain
	 * series for a single TridasObject.  If there are sub-objects in the project we use metadata from the
	 * top level object and ignore the rest.  
	 *  
	 * @param o
	 * @param topLevel
	 * @throws IncompleteTridasDataException
	 * @throws ConversionWarningException 
	 */
	private void parseObject(TridasProject p, TridasObject o, Boolean topLevel) throws IncompleteTridasDataException, ConversionWarningException{
		
		TucsonFile file = new TucsonFile(defaults, this);  // Class representation of the data file that we'll end up with
		
		/**
		 * Set what object metadata the file can handle
		 */
		if(topLevel)
		{
			// Set Principle Investigator metadata
			try{file.setInvestigator(p.getInvestigator().toString());}
			catch(NullPointerException e){}
			catch(ConversionWarningException w){ this.addWarningToList(w.getWarning());};
			
			// Site Code
			try{file.setSiteCode(o.getIdentifier().getValue().toString());}
			catch(NullPointerException e){}
			//catch(ConversionWarningException w){ this.addWarningToList(w.getWarning());};
			
			// Site Name
			try{file.setSiteName(o.getTitle());}
			catch(NullPointerException e){}
			catch(ConversionWarningException w){ this.addWarningToList(w.getWarning());};
	
			// LatLong
			try{List<Double> coords = o.getLocation().getLocationGeometry().getPoint().getPos().getValues();
			    if (coords.size()==2) file.setLatLong(coords.get(0), coords.get(1)); }
			catch(NullPointerException e){}
			catch(ConversionWarningException w){ this.addWarningToList(w.getWarning());};
			
			// Comp date
			try{file.setCompDate(o.getLastModifiedTimestamp().getValue());}
			catch(NullPointerException e){}
			catch(ConversionWarningException w){ this.addWarningToList(w.getWarning());};
		}
			
		// Traverse through TRiDaS hierarchy adding all series we find to file
		if (TridasHierarchyHelper.getElementList(o).size()==0) throw new IncompleteTridasDataException(I18n.getText("fileio.elementMissing"));	
		for(TridasElement e : TridasHierarchyHelper.getElementList(o))
		{
			// Species Code and Name
			try{
				file.setSpeciesName(e.getTaxon().getNormal().toString());
			    file.setSpeciesCode(ITRDBTaxonConverter.getCodeFromName(e.getTaxon().getNormal().toString()));
			}
			catch(NullPointerException e2){}
			catch(ConversionWarningException w){
				this.addWarningToList(w.getWarning());
			}
			
			try{
				e.getSamples();
			}
			catch(NullPointerException e1){throw new IncompleteTridasDataException(I18n.getText("fileio.sampleMissing"));};
			
			for(TridasSample s : e.getSamples())
			{
				try{
					s.getRadiuses();
				}
				catch(NullPointerException e1){throw new IncompleteTridasDataException(I18n.getText("fileio.radiusMissing"));};	
				for(TridasRadius r : s.getRadiuses())
				{
					for(TridasMeasurementSeries ser : r.getMeasurementSeries())
					{					
						file.addSeries((ITridasSeries) ser);
					}
					
					if (r.getMeasurementSeries().size()>0)
					{
						// This file should contain all the series that we can output for in one file so add to the 
						// convertor's file list
						naming.registerFile(file, p, o, e, s, r, null);
						addToFileList(file);
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
