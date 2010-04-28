package org.tridas.io;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.YearRange;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * This is a helper class designed for collect together Tridas entities ready for export
 * to legacy file formats.  
 * 
 * @author peterbrewer
 *
 */
public class TridasSeriesPack  {

	private TridasProject prj;
	private TridasObject obj;
	private TridasElement elm;
	private TridasSample smp;
	private TridasRadius rad; 
	private List<TridasMeasurementSeries> mSeriesList;
	private List<TridasDerivedSeries> dSeriesList;
	
	/**
	 * Simple constructor to use when you have a TridasProject.  If a project has multiple
	 * objects, we use details in the first object, likewise for element, sample and radius.  
	 * This is because all formats (excluding TRiDaS) provide just one set of metadata for 
	 * all series in the file.  
	 * 
	 * @param p
	 */
	TridasSeriesPack(TridasProject p)
	{
		try	{ prj = p; } 
			catch(NullPointerException e){ System.err.println("TridasProject is null");}
		try	{ obj = p.getObjects().get(0); } 
			catch(NullPointerException e){ System.err.println("TridasObject is null");}
		try	{ elm = obj.getElements().get(0); } 
			catch(NullPointerException e){ System.err.println("TridasElement is null");}
		try	{ smp = elm.getSamples().get(0); } 
			catch(NullPointerException e){ System.err.println("TridasSample is null");}
		try	{ rad = smp.getRadiuses().get(0); } 
			catch(NullPointerException e){ System.err.println("TridasRadius is null");}
		try	{ dSeriesList = prj.getDerivedSeries(); } 
			catch(NullPointerException e){	}
		try	{ mSeriesList = rad.getMeasurementSeries(); } 
			catch(NullPointerException e){	}
			
		if (dSeriesList==null && mSeriesList==null)
		{
			System.err.println("No series in project");
		}
		unsetChildren();
	}

	/**
	 * Constructor to use when you want to explicitly specify each parent TridasEntity
	 * of a list of measurementSeries
	 * 
	 * @param p
	 * @param o
	 * @param e
	 * @param sa
	 * @param r
	 * @param seLst
	 */
	TridasSeriesPack(TridasProject p, TridasObject o, TridasElement e, TridasSample sa, 
			TridasRadius r, List<TridasMeasurementSeries> seLst)
	{
		
		prj = p;
		obj = o;
		elm = e;
		smp = sa;
		rad = r;	
		mSeriesList = new ArrayList<TridasMeasurementSeries>(seLst);
		unsetChildren();
		
	}
	
	/**
	 * Constructor for use when you want to explicitly specify each parent TridasEntity
	 * of a specific single measurementSeries
	 * 
	 * @param p
	 * @param o
	 * @param e
	 * @param sa
	 * @param r
	 * @param se
	 */
	TridasSeriesPack(TridasProject p, TridasObject o, TridasElement e, TridasSample sa, 
			TridasRadius r, ITridasSeries se)
	{

		List<TridasMeasurementSeries> lst = new ArrayList<TridasMeasurementSeries>();
		lst.add((TridasMeasurementSeries) se);
		
		prj = p;
		obj = o;
		elm = e;
		smp = sa;
		rad = r;	
		mSeriesList = lst;	
		unsetChildren();	
	}
	
	/**
	 * Remove all child entities from each of the TridasEntities.  Data for a TridasObject
	 * should be extracted from the specific TridasObject member and *not* from the 
	 * getObjects() function on the TridasProject member.  This function strips all of the 
	 * child entities embedded within each member to remove temptation/confusion!
	 */
	private void unsetChildren()
	{
		/*if (prj!=null)	
		{	
			prj.unsetObjects();
			prj.unsetDerivedSeries();
		}*/
		if (obj!=null) obj.unsetElements();
		if (elm!=null) elm.unsetSamples();
		if (smp!=null) smp.unsetRadiuses();
		if (rad!=null) rad.unsetMeasurementSeries();	
	}

	public TridasObject getTridasObject(){
		return obj;
	}
	
	public TridasElement getTridasElement(){
		return elm;
	}
	
	public TridasSample getTridasSample(){
		return smp;
	}
	
	public TridasRadius getTridasRadius(){
		return rad;
	}
	
	/*
	public TridasMeasurementSeries getTridasMeasurementSeries(Integer index)
	{
		return mSeriesList.get(index);
	}
	
	public TridasDerivedSeries getTridasDerivedSeries(Integer index)
	{
		return dSeriesList.get(index);
	}	
	*/
	
	/**
	 * Get a generic list of series for this pack, agnostic to whether
	 * we're talking derived or measurementSeries
	 * 
	 * @return
	 */
	public List<ITridasSeries> getSeriesList()
	{
		List<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();;
		if(dSeriesList!=null)
		{
			for (TridasDerivedSeries ds : dSeriesList)
			{
				seriesList.add((ITridasSeries) ds);
			}
		}
		else if (mSeriesList!=null)
		{
			for (TridasMeasurementSeries ms : mSeriesList)
			{
				seriesList.add((ITridasSeries) ms);
			}			
		}
		
		return seriesList;
	}

	/**
	 * Get the year range for *all* series in this pack
	 * @return
	 */
	public YearRange getRange()
	{
		YearRange rng = getRange(0);
		
		
		for(int i =0; i< getSeriesList().size(); i++)
		{
			YearRange thisRng = getRange(i);
			if (thisRng!=null) rng.union(getRange(i));
		}
		
		return rng;
	}
		
	/**
	 * Get the year range for a specific index 
	 * @param index
	 * @return
	 */
	public YearRange getRange(Integer index)
	{
		ITridasSeries ser = null;
		DatingSuffix firstYearSuffix = null;
		DatingSuffix lastYearSuffix = null;
		SafeIntYear firstYear = null;
		SafeIntYear lastYear = null;
		
		try { ser = getSeriesList().get(index); } 
			catch(NullPointerException e) { return null;} 
		try { firstYearSuffix = ser.getInterpretation().getFirstYear().getSuffix();}
			catch(NullPointerException e) { return null;} 
		try { lastYearSuffix = ser.getInterpretation().getFirstYear().getSuffix();}
			catch(NullPointerException e) { return null;} 			
			
		// Calculate firstYear 
		if (firstYearSuffix.equals(org.tridas.schema.DatingSuffix.BC))
		{
			// BC year so turn into negative year
			try { firstYear = new SafeIntYear("-" + ser.getInterpretation().getFirstYear().getValue().toString());}
				catch(NullPointerException e) { return null;}
		}
		else if (firstYearSuffix.equals(org.tridas.schema.DatingSuffix.BP))
		{
			// BP year so work from 1950
			try { firstYear = new SafeIntYear(BigInteger.valueOf(1950)
					.subtract(ser.getInterpretation().getFirstYear().getValue())
					.toString());}
				catch(NullPointerException e) { return null;}
		}
		else
		{
			// AD year so simple
			try { firstYear = new SafeIntYear(ser.getInterpretation().getFirstYear().getValue().toString());}
				catch(NullPointerException e) { return null;}
		}
		
		// Calculate lastYear 
		if (lastYearSuffix.equals(org.tridas.schema.DatingSuffix.BC))
		{
			// BC year so turn into negative year
			try{lastYear = new SafeIntYear("-" + ser.getInterpretation().getLastYear().getValue().toString());}
				catch(NullPointerException e) { return null;}
		}
		else if (lastYearSuffix.equals(org.tridas.schema.DatingSuffix.BP))
		{
			// BP year so work from 1950
			try {lastYear = new SafeIntYear(BigInteger.valueOf(1950)
					.subtract(ser.getInterpretation().getLastYear().getValue())
					.toString());}
				catch(NullPointerException e) { return null;}
		}
		else
		{
			// AD year so simple
			try { lastYear = new SafeIntYear(ser.getInterpretation().getLastYear().getValue().toString());}
				catch(NullPointerException e) { return null;}	
		}
		
		
		return new YearRange(firstYear, lastYear);
	}
	
}