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

import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class TridasUtils {
	
	public TridasUtils() {

	}
	
	@SuppressWarnings("unchecked")
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
	

	public enum TridasValueDataType{
		DOUBLE,
		INTEGER,
		POSITIVE_DOUBLE,
		POSITIVE_INTEGER,
		ANYNUMBER,
		STRING;
	}
}

