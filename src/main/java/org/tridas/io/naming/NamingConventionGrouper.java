package org.tridas.io.naming;

import java.util.HashSet;
import java.util.Iterator;

import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * Helper class for compiling 
 * @author pwb48
 *
 */
public class NamingConventionGrouper {

	private HashSet<TridasProject> projects;
	private HashSet<TridasObject>  objects;
	private HashSet<TridasElement> elements;
	private HashSet<TridasSample>  samples;
	private HashSet<TridasRadius>  radii;
	private HashSet<TridasDerivedSeries> dseries;
	private HashSet<TridasMeasurementSeries> mseries;

	
	public NamingConventionGrouper()
	{
		projects = new HashSet<TridasProject>();
		objects  = new HashSet<TridasObject>();
		elements = new HashSet<TridasElement>();
		samples  = new HashSet<TridasSample>(); 
		radii    = new HashSet<TridasRadius>();
		dseries  = new HashSet<TridasDerivedSeries>();
		mseries  = new HashSet<TridasMeasurementSeries>();	
		
	}
	
	public void add(TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries)
	{
		if(argProject!=null) projects.add(argProject);
		if(argObject!=null)   objects.add(argObject);
		if(argElement!=null) elements.add(argElement);
		if(argSample!=null)   samples.add(argSample);
		if(argRadius!=null)     radii.add(argRadius);
		if(argSeries!=null)   mseries.add(argSeries);
	}
	
	public void add(TridasProject argProject, TridasDerivedSeries argSeries)
	{
		if(argProject!=null) projects.add(argProject);
		if(argSeries!=null)   dseries.add(argSeries);
	}
	
	public void add(TridasProject argProject)
	{
		projects.add(argProject);
	}
	
	public void add(TridasObject argObject)
	{
		objects.add(argObject);
	}
	
	public void add(TridasElement argElement)
	{
		elements.add(argElement);
	}
	
	public void add(TridasSample argSample)
	{
		samples.add(argSample);
	}
	
	public void add(TridasRadius argRadius)
	{
		radii.add(argRadius);
	}
	
	public void add(TridasMeasurementSeries argSeries)
	{
		mseries.add(argSeries);
	}
	
	public void add(TridasDerivedSeries argSeries)
	{
		dseries.add(argSeries);
	}
	
	public TridasProject getProject()
	{
		if (projects.size()==1) 
		{
			Iterator<TridasProject> it = projects.iterator();
			while (it.hasNext()) {
				return it.next();
			}
		}
		return null;		
	}
	
	public TridasObject getObject()
	{
		if (objects.size()==1) 
		{
			Iterator<TridasObject> it = objects.iterator();
			while (it.hasNext()) {
				return it.next();
			}
		}
		return null;		
	}
	
	public TridasElement getElement()
	{
		if (elements.size()==1) 
		{
			Iterator<TridasElement> it = elements.iterator();
			while (it.hasNext()) {
				return it.next();
			}
		}
		return null;		
	}
	
	public TridasSample getSample()
	{
		if (samples.size()==1) 
		{
			Iterator<TridasSample> it = samples.iterator();
			while (it.hasNext()) {
				return it.next();
			}
		}
		return null;		
	}
	
	public TridasRadius getRadius()
	{
		if (radii.size()==1) 
		{
			Iterator<TridasRadius> it = radii.iterator();
			while (it.hasNext()) {
				return it.next();
			}
		}
		return null;			
	}
	
	public TridasDerivedSeries getDerivedSeries()
	{
		if (dseries.size()==1) 
		{
			Iterator<TridasDerivedSeries> it = dseries.iterator();
			while (it.hasNext()) {
				return it.next();
			}
		}
		return null;			
	}
	
	public TridasMeasurementSeries getMeasurementSeries()
	{
		if (mseries.size()==1) 
		{
			Iterator<TridasMeasurementSeries> it = mseries.iterator();
			while (it.hasNext()) {
				return it.next();
			}
		}
		return null;			
	}
	
	public boolean containsDerived()
	{
		return(dseries.size()>0);
	}
}
