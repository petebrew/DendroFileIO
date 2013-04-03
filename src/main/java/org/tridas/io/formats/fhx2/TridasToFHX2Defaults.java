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
package org.tridas.io.formats.fhx2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.text.SimpleDateFormat;

import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasSample;
import org.tridas.spatial.GMLPointSRSHandler;

import com.ibm.icu.util.GregorianCalendar;

import edu.emory.mathcs.backport.java.util.Collections;

public class TridasToFHX2Defaults extends AbstractMetadataFieldSet {
	public ArrayList<String> siteTitle;
	public ArrayList<String> siteCode;
	public ArrayList<String> country;
	public ArrayList<String> state;
	public ArrayList<String> town;
	public ArrayList<Double> latitude;
	public ArrayList<Double> longitude;
	public ArrayList<String> comments;
	public ArrayList<String> taxon;
	public ArrayList<Integer> slopeAngle;
	public ArrayList<Integer> slopeAzimuth;
	public ArrayList<String> soil;
	public ArrayList<String> geology;
	public ArrayList<Date> collectingDate;

	
	@Override
	protected void initDefaultValues() {
	// TODO Auto-generated method stub
		siteTitle = new ArrayList<String>();
		siteCode = new ArrayList<String>();
		country = new ArrayList<String>();
		state = new ArrayList<String>();
		town = new ArrayList<String>();
		latitude = new ArrayList<Double>();
		longitude = new ArrayList<Double>();
		comments = new ArrayList<String>();
		taxon = new ArrayList<String>();
		slopeAngle = new ArrayList<Integer>();
		slopeAzimuth = new ArrayList<Integer>();
		soil = new ArrayList<String>();
		geology = new ArrayList<String>();
		collectingDate = new ArrayList<Date>();
	}
	
	public String getLatitude()
	{
		return summeriseDoubleArray(latitude);
	}
	
	public String getLongitude()
	{
		return summeriseDoubleArray(longitude);
	}
	
	public String getSiteTitle()
	{
		return summeriseStringArray(siteTitle, "sites");
	}
	
	public String getSiteCode()
	{
		return summeriseStringArray(siteCode, "sites");
	}
	
	public String getCountry()
	{
		return summeriseStringArray(country, "countries");
	}
	
	public String getState()
	{
		return summeriseStringArray(state, "states");
	}
	
	public String getTown()
	{
		return summeriseStringArray(town, "towns");
	}
	
	public String getComments()
	{
		String lines = "";
		for(String line : comments)
		{
			lines+= "\n"+line.trim();
		}
		return lines;
	}
	
	public String getTaxon()
	{
		return summeriseStringArray(taxon, "taxa");
	}
	
	public String getSlope()
	{
		return summeriseIntegerArray(slopeAngle);
	}
	
	public String getAspect()
	{
		return summeriseIntegerArray(slopeAzimuth);
	}
	
	public String getSubstrate()
	{
		String soilstr = summeriseStringArray(soil, "types");
		String geolstr = summeriseStringArray(geology, "types");
		String str ="";
		if(soilstr!=null && soilstr.length()>0)
		{
			str +="Soil: "+soilstr +".  ";
		}
		if(geolstr!=null && geolstr.length()>0)
		{
			str +="Bedrock: "+geolstr;
		}
		
		return str.replaceAll("\\n", "  ");
		
	}
	
	public String getCollectionDate()
	{
		return summeriseDateArray(collectingDate);
	}
	
	public void populateFromTridasSample(TridasSample s)
	{
		if(s==null) return;
		
		if(s.isSetSamplingDate())
		{
			collectingDate.add(s.getSamplingDate().getValue().toGregorianCalendar().getTime());
		}
		
	}
	
	public void populateFromTridasElement(TridasElement e)
	{
		if(e==null) return;
		
		if(e.isSetTaxon())
		{
			if(e.getTaxon().isSetNormal())
			{
				taxon.add(e.getTaxon().getNormal());
			}
			else if (e.getTaxon().isSetValue())
			{
				taxon.add(e.getTaxon().getValue());
			}
		}
		
		if(e.isSetSlope())
		{
			if(e.getSlope().isSetAngle())
			{
				slopeAngle.add(e.getSlope().getAngle());
			}
			if(e.getSlope().isSetAzimuth())
			{
				slopeAzimuth.add(e.getSlope().getAzimuth());
			}
		}
		
		if(e.isSetSoil())
		{
			if(e.getSoil().isSetDescription())
			{
				soil.add(e.getSoil().getDescription());
			}			
		}
		
		if(e.isSetBedrock())
		{
			if(e.getBedrock().isSetDescription())
			{
				geology.add(e.getBedrock().getDescription());
			}
		}
	}
	
	public void populateFromTridasObject(TridasObject o)
	{
		if(o==null) return;
		
		
		if(o.isSetTitle())
		{
			siteTitle.add(o.getTitle());
		}
		
		if(o.isSetGenericFields())
		{
    		for(TridasGenericField gf : o.getGenericFields())
    		{
    			if (gf.getName().equals("tellervo.objectLabCode")){
    				siteCode.add(gf.getValue().toString());
    			}
    		}
		}
		
		if(o.isSetLocation())
		{
			if(o.getLocation().isSetAddress())
			{
				if(o.getLocation().getAddress().isSetCountry())
				{
					country.add(o.getLocation().getAddress().getCountry());
				}
				
				if(o.getLocation().getAddress().isSetStateProvinceRegion())
				{
					state.add(o.getLocation().getAddress().getStateProvinceRegion());
				}
				
				if(o.getLocation().getAddress().isSetCityOrTown())
				{
					town.add(o.getLocation().getAddress().getCityOrTown());
				}
			}
			
			if(o.getLocation().isSetLocationGeometry())
			{
				if(o.getLocation().getLocationGeometry().isSetPoint())
				{
					GMLPointSRSHandler gmlhandler = new GMLPointSRSHandler(o.getLocation().getLocationGeometry().getPoint());
					
					latitude.add(gmlhandler.getWGS84LatCoord());
					longitude.add(gmlhandler.getWGS84LongCoord());
					
				}
			}
		}
		
		if(o.isSetComments())
		{
			comments.add(o.getComments());
		}
		
		
	}
	
	
	private String summeriseDateArray(ArrayList<Date> ar)
	{
		ArrayList<Date> arr = (ArrayList<Date>) ar.clone();
		
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		
		if(arr.size()==0) return "";
		if(arr.size()==1) return dateFormat.format(arr.get(0));
		
		HashSet hs = new HashSet();
		hs.addAll(arr);
		if(hs.size()==0) return "";
		if(hs.size()==1) return dateFormat.format(arr.get(0));
		
		Collections.sort(arr);
		
		return "between "+dateFormat.format(arr.get(0)) + " and "+ dateFormat.format(arr.get(arr.size()-1));
		
	}
	
	private String summeriseIntegerArray(ArrayList<Integer> ar)
	{
		ArrayList<Integer> arr = (ArrayList<Integer>) ar.clone();
		
		if(arr.size()==0) return "";
		if(arr.size()==1) return arr.get(0).toString();
		
		HashSet hs = new HashSet();
		hs.addAll(arr);
		if(hs.size()==0) return "";
		if(hs.size()==1) return arr.get(0).toString();
		
		Collections.sort(arr);
		
		return "between "+arr.get(0) + " and "+ arr.get(arr.size()-1);
		
	}
	
	
	private String summeriseDoubleArray(ArrayList<Double> ar)
	{
		ArrayList<Double> arr = (ArrayList<Double>) ar.clone();
		
		if(arr.size()==0) return "";
		if(arr.size()==1) return arr.get(0).toString();
		
		HashSet hs = new HashSet();
		hs.addAll(arr);
		if(hs.size()==0) return "";
		if(hs.size()==1) return arr.get(0).toString();
		
		Collections.sort(arr);
		
		return "between "+arr.get(0) + " and "+ arr.get(arr.size()-1);
		
	}
	
	
	private String summeriseStringArray(ArrayList<String> ar, String field)
	{
		ArrayList<String> arr = (ArrayList<String>) ar.clone();
		
		if(arr.size()==0) return "";
		if(arr.size()==1) return arr.get(0);
		
		HashSet hs = new HashSet();
		hs.addAll(arr);
		if(hs.size()==0) return "";
		if(hs.size()==1) return arr.get(0);
		
		String str = hs.size()+" "+field+": ";
		
		Iterator iter = hs.iterator();
		while (iter.hasNext()) {
		  String val = (String) iter.next();
		  if(val!=null && val.length()>0) str +=val+"; ";
		}
		
		try{
			str = (str.trim().substring(0, str.length()-2))+"";
		} catch (Exception e)
		{
		}
		return str;
		
	}
}
