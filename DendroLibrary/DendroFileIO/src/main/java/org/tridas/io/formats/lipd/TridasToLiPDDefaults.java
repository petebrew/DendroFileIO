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
package org.tridas.io.formats.lipd;

import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.spatial.GMLPointSRSHandler;

public class TridasToLiPDDefaults extends AbstractMetadataFieldSet implements IMetadataFieldSet {
	
	public static enum DefaultFields {
		DATA_SET_NAME,
		INVESTIGATORS,
		DATA_DOI,
		VERSION,

		//PUB STUFF NOT USED
		
		LATITUDE,
		LONGITUDE,
		ELEVATION,
		SITE_NAME,
		
		PARAMETER,
		;
	}
	
	@Override
	public void initDefaultValues() {
		setDefaultValue(DefaultFields.DATA_SET_NAME, new StringDefaultValue(I18n.getText("unknown")));
		setDefaultValue(DefaultFields.INVESTIGATORS, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATA_DOI, new StringDefaultValue());
		setDefaultValue(DefaultFields.VERSION, new StringDefaultValue());

		
		setDefaultValue(DefaultFields.SITE_NAME, new StringDefaultValue(I18n.getText("unnamed.object")));
		setDefaultValue(DefaultFields.LATITUDE, new DoubleDefaultValue(null, -90.0, 90.0));
		setDefaultValue(DefaultFields.LONGITUDE, new DoubleDefaultValue(null, -180.0, 180.0));
		setDefaultValue(DefaultFields.ELEVATION, new DoubleDefaultValue(null, -418.0, 8850.0)); // Heights of Dead Sea and Everest! ;-)
		
		setDefaultValue(DefaultFields.PARAMETER, new StringDefaultValue("Ring width"));
		

	}
	

	public void populateFromTridasProject(TridasProject p) {

		if(p.isSetInvestigator())
		{
			getStringDefaultValue(DefaultFields.INVESTIGATORS).setValue(p.getInvestigator());
		}
		
	}
	
	public void populateFromTridasObject(TridasObject o) {

		// Set site name
		if(o.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SITE_NAME).setValue(o.getTitle());
		}
		
		// Set coordinates using the projection handler to make sure we're reading correctly
		if(o.isSetLocation())
		{
			if(o.getLocation().isSetLocationGeometry())
			{
				if(o.getLocation().getLocationGeometry().isSetPoint())
				{
					GMLPointSRSHandler tph = new GMLPointSRSHandler(o.getLocation().getLocationGeometry().getPoint());
					getDoubleDefaultValue(DefaultFields.LATITUDE).setValue(tph.getWGS84LatCoord());
					getDoubleDefaultValue(DefaultFields.LONGITUDE).setValue(tph.getWGS84LongCoord());
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasElement(TridasElement e) {

		if(e.isSetAltitude())
		{
			getDoubleDefaultValue(DefaultFields.ELEVATION).setValue(e.getAltitude());
		}

	}
		
	public void setCollectionName(String s)
	{
		getStringDefaultValue(DefaultFields.DATA_SET_NAME).setValue(s);

	}
}
