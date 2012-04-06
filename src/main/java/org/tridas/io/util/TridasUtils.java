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
package org.tridas.io.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridas;
import org.tridas.io.I18n;
import org.tridas.io.formats.tridas.TridasReader;
import org.tridas.schema.NormalTridasRemark;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRemark;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.util.TridasObjectEx;

public class TridasUtils {
	
	private static final Logger log = LoggerFactory.getLogger(TridasUtils.class);
	
	public TridasUtils() {

	}
	
	public static ArrayList<TridasMeasurementSeries> getMeasurementSeriesFromTridasProject(TridasProject p) {
		ArrayList<TridasMeasurementSeries> serlist = new ArrayList<TridasMeasurementSeries>();
		
		for (TridasObject o : p.getObjects()) {
			serlist.addAll(TridasUtils.getMeasurementSeriesFromTridasObject(o));
		}
		
		return serlist;
	}
	
	public static Set<TridasMeasurementSeries> getMeasurementSeriesFromTridasObject(TridasObject o) {
		Set<TridasMeasurementSeries> seriesSet = new HashSet<TridasMeasurementSeries>();
		List<TridasElement> el = null;
		
		// Get a list of subobjects and recursively call this function to get their
		// measurementSeries
		List<TridasObject> sol = null;
		try {
			sol = o.getObjects();
		} catch (NullPointerException e) {};
		for (TridasObject so : sol) {
			seriesSet.addAll(TridasUtils.getMeasurementSeriesFromTridasObject(so));
		}
		
		// Try to get a list of elements from the object
		try {
			el = o.getElements();
		} catch (NullPointerException e) {
			return seriesSet;
		}
		
		// Loop through elements
		for (TridasElement e : el) {
			// Try to get a list of samples from the element
			List<TridasSample> sl = null;
			try {
				sl = e.getSamples();
			} catch (NullPointerException e1) {}
			
			// No samples so go to next element
			if (sl == null) {
				continue;
			}
			
			// Loop through samples
			for (TridasSample s : sl) {
				// Try to get a list of radii from the sample
				List<TridasRadius> rl = null;
				try {
					rl = s.getRadiuses();
				} catch (NullPointerException e1) {}
				
				// No radii so check for radiusPlaceholders instead
				if (rl == null) {
					continue;
					
					// TODO Implement placeholder support
					
					/*
					 * TridasRadiusPlaceholder rph = null;
					 * try{ rph= s.getRadiusPlaceholder();}
					 * catch(NullPointerException e1){ }
					 * // no radius placeholder either so continue to next sample;
					 * if(rph==null) continue;
					 * rph.getMeasurementSeriesPlaceholder();
					 */
				}
				
				// Loop through radii
				for (TridasRadius r : rl) {
					List<TridasMeasurementSeries> msl = null;
					try {
						msl = r.getMeasurementSeries();
					} catch (NullPointerException e2) {}
					
					// No measurement series so continue to next radius
					if (msl == null) {
						continue;
					}
					
					for (TridasMeasurementSeries ms : msl) {
						seriesSet.add(ms);
					}
				}
				
			}
			
		}
		
		return seriesSet;
		
	}
	
	/**
	 * Recursively work through objects and sub-objects compiling a list
	 * of all the TridasElements associated with them.
	 * 
	 * @param o
	 * @return
	 */
	public static ArrayList<TridasElement> getElementList(TridasObject o) {
		ArrayList<TridasElement> els = new ArrayList<TridasElement>();
		
		// Loop through any sub-objects calling this function recursively
		// TridasObjects can have sub-Objects so we need to delve into
		// them to find the data
		try {
			o.getObjects();
			for (TridasObject subobj : o.getObjects()) {
				els.addAll(getElementList(subobj));
			}
		} catch (NullPointerException e) {};
		
		els.addAll(o.getElements());
		return els;
	}
	
	/**
	 * Get a list of TridasObjects from a project recursively
	 * 
	 * @param p
	 * @return
	 */
	public static ArrayList<TridasObject> getObjectList(TridasProject p) {
		ArrayList<TridasObject> ols = new ArrayList<TridasObject>();
		
		try {
			for (TridasObject o : p.getObjects()) {
				ols.addAll(getObjectList(o));
			}
		} catch (NullPointerException e) {

		}
		return ols;
		
	}
	
	/**
	 * Recursively work through objects and sub-objects compiling a list
	 * of all the TridasObjects associated with this TridasObject.
	 * 
	 * @param p
	 * @return
	 */
	public static ArrayList<TridasObject> getObjectList(TridasObject o) {
		ArrayList<TridasObject> ols = new ArrayList<TridasObject>();
		
		// Loop through any sub-objects calling this function recursively
		// TridasObjects can have sub-Objects so we need to delve into
		// them to find the data
		try {
			o.getObjects();
			for (TridasObject subobj : o.getObjects()) {
				ols.addAll(getObjectList(subobj));
			}
		} catch (NullPointerException e) {};
		
		ols.add(o);
		return ols;
	}
	
	/**
	 * Takes a list of projects and merges those together than have the same title
	 * 
	 * @param projects
	 * @return
	 */
	public static ArrayList<TridasProject> consolidateProjects(ArrayList<TridasProject> projects)
	{
		ArrayList<ITridas> returnProjects = new ArrayList<ITridas>();
		
		// First consolidate Projects
		for(TridasProject p1 : projects)
		{
			if(getMatchInArrayByTitle(returnProjects, p1)==null)
			{
				// Project not in return list, so add
				returnProjects.add(p1);
			}
			else
			{
				// Project is in return list, so grab 
				TridasProject p2 = (TridasProject) getMatchInArrayByTitle(returnProjects, p1);
				
				// Remove existing from list
				returnProjects = removeEntriesThatMatchTitle(returnProjects, p1);
				
				// Append extra objects
				p2.getObjects().addAll(p1.getObjects());
				
				// Add back to list
				returnProjects.add(p2);
			}
		}
				
		
		// Cast to ArrayList<TridasProject>
		ArrayList<TridasProject> returnProjects2 = new ArrayList<TridasProject>();
		for(ITridas i : returnProjects)
		{
			TridasProject i2 = (TridasProject) i;
			ArrayList<TridasObject> objlist = new ArrayList<TridasObject>();
			for(TridasObject o : i2.getObjects())
			{
				objlist.add(o);
			}

			i2.getObjects().clear();
			i2.getObjects().addAll(consolidateObjects(objlist));
			returnProjects2.add(i2);
		}
		return returnProjects2;
		
	}
	
	/**
	 * Takes a list of objects and merges those together than have the same title.  
	 * 
	 * @param objects
	 * @return
	 */
	public static ArrayList<TridasObject> consolidateObjects(List<TridasObject> objects)
	{
		ArrayList<ITridas> returnObjects = new ArrayList<ITridas>();
		
		// First consolidate Objects
		for(TridasObject o1 : objects)
		{
			if(getMatchInArrayByTitle(returnObjects, o1)==null)
			{
				// Object not in return list, so add
				returnObjects.add(o1);
			}
			else
			{
				// Object is in return list, so grab 
				TridasObject o2 = (TridasObject) getMatchInArrayByTitle(returnObjects, o1);
								
				// Remove existing from list
				returnObjects = removeEntriesThatMatchTitle(returnObjects, o1);
				
				// Append extra elements
				o2.getElements().addAll(o1.getElements());
				
				// Add back to list
				returnObjects.add(o2);
			}
		}
		
		
		// Cast to ArrayList<TridasObject>
		ArrayList<TridasObject> returnObjects2 = new ArrayList<TridasObject>();
		for(ITridas i : returnObjects)
		{
			TridasObject i2 = (TridasObject) i;
			ArrayList<TridasObject> objlist = new ArrayList<TridasObject>();
			for(TridasObject o : i2.getObjects())
			{
				objlist.add(o);
			}

			i2.getObjects().clear();
			i2.getObjects().addAll(consolidateObjects(objlist));
			
			
			ArrayList<TridasElement> ellist = new ArrayList<TridasElement>();
			for(TridasElement e : i2.getElements())
			{
				ellist.add(e);
			}
			
			i2.getElements().clear();
			i2.getElements().addAll(consolidateElements(ellist));
			
			returnObjects2.add(i2);

		}
		return returnObjects2;
		
	}
	
	/**
	 * Takes a list of elements and merges those together than have the same title
	 * 
	 * @param elements
	 * @return
	 */
	public static ArrayList<TridasElement> consolidateElements(ArrayList<TridasElement> elements)
	{
		ArrayList<ITridas> returnElements = new ArrayList<ITridas>();
		
		// First consolidate Elements
		for(TridasElement e1 : elements)
		{
			if(getMatchInArrayByTitle(returnElements, e1)==null)
			{
				// Element not in return list, so add
				returnElements.add(e1);
			}
			else
			{
				// Element is in return list, so grab 
				TridasElement e2 = (TridasElement) getMatchInArrayByTitle(returnElements, e1);
				
				// Remove existing from list
				returnElements = removeEntriesThatMatchTitle(returnElements, e1);
				
				// Append extra samples
				e2.getSamples().addAll(e1.getSamples());
				
				// Add back to list
				returnElements.add(e2);
			}
		}
				
		
		// Recurse to merge child entries
		ArrayList<TridasElement> returnElements2 = new ArrayList<TridasElement>();
		for(ITridas i : returnElements)
		{
			TridasElement i2 = (TridasElement) i;
			ArrayList<TridasSample> sampList = new ArrayList<TridasSample>();
			for(TridasSample s : i2.getSamples())
			{
				sampList.add(s);
			}

			i2.getSamples().clear();
			i2.getSamples().addAll(consolidateSamples(sampList));
			returnElements2.add(i2);
		}
		return returnElements2;
		
	}
	
	/**
	 * Takes a list of samples and merges those together than have the same title
	 * 
	 * @param elements
	 * @return
	 */
	public static ArrayList<TridasSample> consolidateSamples(ArrayList<TridasSample> samples)
	{
		ArrayList<ITridas> returnSamples = new ArrayList<ITridas>();
		
		// First consolidate Samples
		for(TridasSample s1 : samples)
		{
			if(getMatchInArrayByTitle(returnSamples, s1)==null)
			{
				// Samples not in return list, so add
				returnSamples.add(s1);
			}
			else
			{
				// Samples is in return list, so grab 
				TridasSample s2 = (TridasSample) getMatchInArrayByTitle(returnSamples, s1);
				
				// Remove existing from list
				returnSamples = removeEntriesThatMatchTitle(returnSamples, s1);
				
				// Append extra radii
				s2.getRadiuses().addAll(s1.getRadiuses());
				
				// Add back to list
				returnSamples.add(s2);
			}
		}
				
		
		// Recurse to merge child entries
		ArrayList<TridasSample> returnSamples2 = new ArrayList<TridasSample>();
		for(ITridas i : returnSamples)
		{
			TridasSample i2 = (TridasSample) i;
			ArrayList<TridasRadius> radList = new ArrayList<TridasRadius>();
			for(TridasRadius r : i2.getRadiuses())
			{
				radList.add(r);
			}

			i2.getRadiuses().clear();
			i2.getRadiuses().addAll(consolidateRadii(radList));
			returnSamples2.add(i2);
		}
		return returnSamples2;
		
	}
	
	/**
	 * Takes a list of radii and merges those together than have the same title
	 * 
	 * @param elements
	 * @return
	 */
	public static ArrayList<TridasRadius> consolidateRadii(ArrayList<TridasRadius> radii)
	{
		ArrayList<ITridas> returnRadii = new ArrayList<ITridas>();
		
		// First consolidate Radii
		for(TridasRadius r1 : radii)
		{
			if(getMatchInArrayByTitle(returnRadii, r1)==null)
			{
				// Radius not in return list, so add
				returnRadii.add(r1);
			}
			else
			{
				// Radius is in return list, so grab 
				TridasRadius r2 = (TridasRadius) getMatchInArrayByTitle(returnRadii, r1);
				
				// Remove existing from list
				returnRadii = removeEntriesThatMatchTitle(returnRadii, r1);
				
				// Append extra series
				r2.getMeasurementSeries().addAll(r1.getMeasurementSeries());
				
				// Add back to list
				returnRadii.add(r2);
			}
		}
				
		
		// Recurse to merge child entries
		ArrayList<TridasRadius> returnRadii2 = new ArrayList<TridasRadius>();
		for(ITridas i : returnRadii)
		{
			returnRadii2.add((TridasRadius) i);
		}
		return returnRadii2;
		
	}
	
	
	
	/**
	 * Search through the provided list looking for a match based on title.  If the 
	 * provided item has the default 'Unknown' title, then it is treated as unique.
	 * 
	 * If match is found, this entity is returned.  If not match is found then null 
	 * is returned
	 * 
	 * @param list
	 * @param checkitem
	 * @return
	 */
	private static ITridas getMatchInArrayByTitle(ArrayList<ITridas> list, ITridas checkitem)
	{
		// Intercept 'Unknown' entities and force treatment as different entities 
		if(checkitem instanceof TridasProject)
		{
			if(checkitem.getTitle().equals(I18n.getText("unnamed.project"))) return null;
		}
		else if(checkitem instanceof TridasObject)
		{
			if(checkitem.getTitle().equals(I18n.getText("unnamed.object"))) return null;
		}
		else if(checkitem instanceof TridasElement)
		{
			if(checkitem.getTitle().equals(I18n.getText("unnamed.element"))) return null;
		}
		else if(checkitem instanceof TridasSample)
		{
			if(checkitem.getTitle().equals(I18n.getText("unnamed.sample"))) return null;
		}			
		else if(checkitem instanceof TridasRadius)
		{
			if(checkitem.getTitle().equals(I18n.getText("unnamed.radius"))) return null;
		}	
		
		// Search through list checking against titles
		for(ITridas thisitem : list)
		{
			if(thisitem.getTitle().equals(checkitem.getTitle())) return thisitem;
		}
		
		// No match
		return null;
		
	}
	
	private static ArrayList<ITridas> removeEntriesThatMatchTitle(ArrayList<ITridas> list, ITridas checkitem)
	{
		ArrayList<ITridas> returnList = new ArrayList<ITridas>();
		
		for(ITridas thisitem : list)
		{
			if(!thisitem.getTitle().equals(checkitem.getTitle())) 
			{
				returnList.add(thisitem);
			}
		}
		
		return returnList;
	}

	
	/**
	 * Checks to see whether a TridasValues block contains only only values
	 * of a certain data type. If tests cannot be completed, (e.g. argValues 
	 * is empty) then it returns null.
	 * 
	 * @param argValues
	 * @return
	 */
	public static Boolean checkTridasValuesDataType(TridasValues argValues, TridasValueDataType dataType)
	{
		// Only test if argValues is not null
		if(argValues==null) return null;
		
		// Only test if argValues contains values
		if(argValues.isSetValues())
		{
			// Loop through all the values
			for (TridasValue val : argValues.getValues())
			{
				switch(dataType)
				{
				case DOUBLE:
				case POSITIVE_DOUBLE:
				case ANYNUMBER:
					// Parse to double if possible
					try{
						Double dbl = Double.parseDouble(val.getValue());
						if(dataType.equals(TridasValueDataType.POSITIVE_DOUBLE))
						{
							if(dbl.compareTo(Double.valueOf("0.0"))<0)
							{
								// Value is less than 0 
								return false;
							}
						}	
					} catch (NumberFormatException e)
					{
						// Parse to double failed 
						return false;
					}
					break;
				case INTEGER:
				case POSITIVE_INTEGER:
					// Parse to integer if possible
					try{
						Integer intval = Integer.parseInt(val.getValue());
						if(dataType.equals(TridasValueDataType.POSITIVE_INTEGER))
						{
							if(intval.compareTo(0)<0)
							{
								// Value is less than 0 
								return false;
							}
						}			
					} catch (NumberFormatException e)
					{
						// Parse to integer failed 
						return false;
					}
					break;
				case STRING:
					break;
				}
			}
		}

		return true;
	}
	
	/**
	 * Attempt to standardise a ring remark into a NormalTridasRemark.
	 * 
	 * @param str
	 * @return
	 */
	public static TridasRemark getRemarkFromString(String str)
	{
		TridasRemark remark = new TridasRemark();
		
		if(str==null)
		{
			return null;	
		}
		else if (str.trim().length()==0)
		{
			return null;
		}
		
		for(NormalTridasRemark trmk : NormalTridasRemark.values())
		{
			if(trmk.value().equalsIgnoreCase(str))
			{
				remark.setNormalTridas(trmk);
				return remark;
			}
		}
		
		remark.setValue(str);
		
		return remark;
	}
	
	/**
	 * Get the entity from this project that matches the specified identifier.  If parentEntityClass is specified
	 * then the parent entity of the class type entered is returned (e.g. perhaps the object of a sample).  If the
	 * identifier matches a derivedSeries, then this function is called recursively to try and locate the correct
	 * entity.  If no match is found, then null is returned.
	 * 
	 * @param p
	 * @param id
	 * @param parentEntityClass
	 * @return
	 */
	public static ITridas getEntityByIdentifier(TridasProject p, TridasIdentifier id, Class<? extends ITridas> parentEntityClass)
	{
		if(p==null || id==null) return null;
		
		ITridas returningEntity = null;
		/*
		// Would be nice to do this with XPATH.
		String domain = "";
		String idstr = "";
		String xpath = "identifier[@domain='"+domain+"' and .='"+idstr+"']/..";
		*/
				
		// First check the project!
		if (TridasUtils.doesEntityMatchIdentifier(p, id) || 
			(parentEntityClass==null || parentEntityClass.equals(TridasProject.class)))
		{
			return p;
		}
		
		
		ArrayList<TridasObject> objects = getObjectList(p);
		
		for (TridasObject o : objects)
		{
			if(parentEntityClass==null || parentEntityClass.equals(TridasObject.class)) returningEntity = o;
			if (TridasUtils.doesEntityMatchIdentifier(o, id)) return returningEntity;
			
			for(TridasElement e : o.getElements())
			{
				if(parentEntityClass==null || parentEntityClass.equals(TridasElement.class)) returningEntity = e;
				if (TridasUtils.doesEntityMatchIdentifier(e, id)) return returningEntity;
								
				for(TridasSample s : e.getSamples())
				{
					if(parentEntityClass==null || parentEntityClass.equals(TridasSample.class)) returningEntity = s;
					if (TridasUtils.doesEntityMatchIdentifier(s, id)) return returningEntity;
					
					for(TridasRadius r : s.getRadiuses())
					{
						if(parentEntityClass==null || parentEntityClass.equals(TridasRadius.class)) returningEntity = r;
						if (TridasUtils.doesEntityMatchIdentifier(r, id)) return returningEntity;
						
						for(TridasMeasurementSeries ser : r.getMeasurementSeries())
						{
							if(parentEntityClass==null || parentEntityClass.equals(TridasMeasurementSeries.class) || parentEntityClass.equals(ITridas.class)) returningEntity = ser;
							if (TridasUtils.doesEntityMatchIdentifier(ser, id)) return returningEntity;
						}
					}
				}
			}	
		}
		
		for (TridasDerivedSeries ds : p.getDerivedSeries())
		{
			if (TridasUtils.doesEntityMatchIdentifier(ds, id)) 
			{
				if(parentEntityClass==null || parentEntityClass.equals(TridasDerivedSeries.class) || parentEntityClass.equals(ITridas.class))
				{
					return ds;
				}
				else if (ds.isSetLinkSeries())
				{
					if(ds.getLinkSeries().isSetSeries())
					{
						if(ds.getLinkSeries().getSeries().get(0).isSetIdentifier())
						{
							return TridasUtils.getEntityByIdentifier(p, ds.getLinkSeries().getSeries().get(0).getIdentifier(), parentEntityClass);
						}
					}
					
				}
			}
		}
		
		return null;
	}
		
	
	/**
	 * Returns true if the TRiDaS entities identifier matches the one specified
	 * 
	 * @param entity
	 * @param id
	 * @return
	 */
	public static Boolean doesEntityMatchIdentifier(ITridas entity, TridasIdentifier id)
	{
		if(entity==null || id==null) return null;
		
		if(entity.isSetIdentifier())
		{
			if(entity.getIdentifier().getDomain().equals(id.getDomain()) && 
			   entity.getIdentifier().getValue().equals(id.getValue()))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	
	public static void debugTridasStructure(Object entity)
	{
		debugTridasStructure(entity, 0);
	}
	
	/**
	 * ASCII Art debug printout of an TRiDaS entity 
	 * 
	 * @param entity
	 * @param level
	 */
	public static void debugTridasStructure(Object entity, Integer level)
	{
		if(entity==null) return;
		
		if(entity instanceof TridasTridas)
		{
			System.out.println("----------------");
			System.out.println(getTabs(level)+"Tridas container ");
			for(TridasProject p : ((TridasTridas) entity).getProjects())
			{
				debugTridasStructure(p, level+1);
			}
		}
		else if (entity instanceof ITridas)
		{
			if (entity instanceof TridasProject)
			{
				
				System.out.println(getTabs(level)+"Project: "+((ITridas)entity).getTitle());
				for(TridasObject o : ((TridasProject) entity).getObjects())
				{
					debugTridasStructure(o, level+1);
				}
				for(TridasDerivedSeries ser : ((TridasProject) entity).getDerivedSeries())
				{
					debugTridasStructure(ser, level+1);
				}
				System.out.println("----------------");
			}
			else if(entity instanceof TridasObject)
			{
				System.out.println(getTabs(level)+"Object: "+((ITridas)entity).getTitle());
				for(TridasObject o2 : ((TridasObject) entity).getObjects())
				{
					debugTridasStructure(o2, level+1);
				}
				for(TridasElement e : ((TridasObject) entity).getElements())
				{
					debugTridasStructure(e, level+1);
				}
			}
			else if(entity instanceof TridasElement)
			{
				System.out.println(getTabs(level)+"Element: "+((ITridas)entity).getTitle());
				for(TridasSample s : ((TridasElement) entity).getSamples())
				{
					debugTridasStructure(s, level+1);
				}
			}
			else if(entity instanceof TridasSample)
			{
				System.out.println(getTabs(level)+"Sample: "+((ITridas)entity).getTitle());
				for(TridasRadius r : ((TridasSample) entity).getRadiuses())
				{
					debugTridasStructure(r, level+1);
				}
			}
			else if(entity instanceof TridasRadius)
			{
				System.out.println(getTabs(level)+"Radius: "+((ITridas)entity).getTitle());
				for(TridasMeasurementSeries ms : ((TridasRadius) entity).getMeasurementSeries())
				{
					debugTridasStructure(ms, level+1);
				}
			}
			else if(entity instanceof TridasMeasurementSeries)
			{
				System.out.println(getTabs(level)+"MSeries: "+((ITridas)entity).getTitle());
			}
			else if(entity instanceof TridasDerivedSeries)
			{
				System.out.println(getTabs(level)+"DSeries: "+((ITridas)entity).getTitle());
			}
		}
		else
		{
			log.warn("Unable to print stucture.  Unknown entity type");
		}
		
	}
	
	/**
	 * Get a friendly variable name for printing from TridasValues group
	 * 
	 * @param group
	 * @return
	 */
	public static String getFriendlyVariableString(TridasValues group)
	{
		if(!group.isSetVariable()) return I18n.getText("unknown.variable");
		
		
		TridasVariable var = group.getVariable();
		
		if(var.isSetNormalTridas())
		{
			return var.getNormalTridas().toString().toLowerCase();
		}
		else if (var.isSetNormal())
		{
			return var.getNormal().toString().toLowerCase();
		}
		else if (var.isSetValue())
		{
			return var.getValue().toLowerCase();
		}
				
		return I18n.getText("unknown.variable");
	}
	
	private static String getTabs(Integer level)
	{
		String str = "";
		for(Integer i=1; i<=level; i++)
		{
			str+="    ";
		}
		return str+"|->";
	}
	
	/** 
	 * Get the level of the Tridas class as an integer where
	 * 1=object through to 5=series.  If a class is given that is 
	 * not in the Tridas hierarchy then 0 is returned.
	 * 
	 * @param e1
	 * @return
	 */
	public static int getDepth(Class<? extends Object> e1)
	{
		if(e1==null) return 0;
		
		if(e1.equals(TridasObject.class) || e1.equals(TridasObjectEx.class))
		{
			return TreeDepth.OBJECT.getDepth();
		}
		else if(e1.equals(TridasElement.class) )
		{
			return TreeDepth.ELEMENT.getDepth();
		}
		else if(e1.equals(TridasSample.class) )
		{
			return TreeDepth.SAMPLE.getDepth();
		}
		else if(e1.equals(TridasRadius.class) )
		{
			return TreeDepth.RADIUS.getDepth();
		}
		else if(e1.equals(TridasMeasurementSeries.class) || e1.equals(TridasDerivedSeries.class))
		{
			return TreeDepth.SERIES.getDepth();
		}
		return 0;
	}
	
	
	public static enum TreeDepth {
		OBJECT(1),
		ELEMENT(2),
		SAMPLE(3),
		RADIUS(4),
		SERIES(5);
		
		private int depth;
		
		TreeDepth(int c) {
			depth = c;
		}
		
		public int getDepth() {
			return depth;
		}
		
		public static TreeDepth valueOf(int c) {
			for(TreeDepth depth : values()) {
				if(depth.getDepth() == c)
					return depth;
			}
			
			throw new IllegalArgumentException("Invalid type");
		}
	}
	
	public enum TridasValueDataType{
		DOUBLE,
		INTEGER,
		POSITIVE_DOUBLE,
		POSITIVE_INTEGER,
		ANYNUMBER,
		STRING;
	}
}

