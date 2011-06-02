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
package org.tridas.io.naming;

import java.util.List;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

public class KeycodeNamingConvention extends AbstractNamingConvention {
	
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries) {
		
		return getDendroFilename(argSeries);
	}
	
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasDerivedSeries argSeries) {
		
		return getDendroFilename(argSeries);
		
	}
	
	private String getDendroFilename(ITridasSeries argSeries) {
		String name = "";
		
		
		if (argSeries != null) {
			
			if(argSeries.isSetGenericFields())
			{
				List<TridasGenericField> gfs = argSeries.getGenericFields();
				
				for(TridasGenericField gf : gfs)
				{
					if(gf.getName().equals("keycode"))
					{
						return gf.getValue();
					}
				}
				
			}
			
			name += argSeries.getTitle();
		}
		else {
			return I18n.getText("fileio.defaultFilenameBase");
		}
		
		return name;
	}
	
	@Override
	public String getDescription() {
		return I18n.getText("namingconvention.keycode.description");
	}
	
	@Override
	public String getName() {
		return I18n.getText("namingconvention.keycode");
	}
	
}
