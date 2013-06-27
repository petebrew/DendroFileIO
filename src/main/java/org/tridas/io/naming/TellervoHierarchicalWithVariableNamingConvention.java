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
import org.tridas.schema.TridasValues;

/**
 * @author daniel
 */
public class TellervoHierarchicalWithVariableNamingConvention extends AbstractNamingConvention {
	
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries) {
		
		
		return getDendroFilename(argFile, argProject, argObject, argElement, argSample, argRadius, argSeries, null);
	}
	
	/**
	 * @see org.tridas.io.naming.AbstractNamingConvention#getDendroFilename(org.tridas.io.IDendroFile,
	 *      org.tridas.schema.TridasProject, org.tridas.schema.TridasObject,
	 *      org.tridas.schema.TridasElement, org.tridas.schema.TridasSample,
	 *      org.tridas.schema.TridasRadius, org.tridas.schema.TridasMeasurementSeries)
	 */
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries, TridasValues argValues) {
		
		String name = "";
		
		if (argObject != null) {
    		name = argObject.getTitle();
    		
    		if(argObject.isSetGenericFields())
    		{
	    		for(TridasGenericField gf : argObject.getGenericFields())
	    		{
	    			if (gf.getName().equals("tellervo.objectLabCode")){
	    				name = gf.getValue().toString();
	    			}
	    		}
    		}
		}
		else {
			return name;
		}
		
		if (argElement != null) {
			name += "-" + argElement.getTitle();
		}
		else {
			return name;
		}
		
		if (argSample != null) {
			name += "-" + argSample.getTitle();
		}
		else {
			return name;
		}
		
		if (argRadius != null) {
			name += "-" + argRadius.getTitle();
		}
		else {
			return name;
		}
		
		if (argSeries != null) {
			name += "-" + argSeries.getTitle();		
		}
		else {
			return name;
		}
		
		if(argValues!=null && argValues.isSetVariable())
		{
			if(argValues.getVariable().isSetNormalTridas())
			{
				name += "-"+ argValues.getVariable().getNormalTridas().value();
			}
			else if (argValues.getVariable().isSetNormal())
			{
				name += "-"+ argValues.getVariable().getNormal();
			}
			else if (argValues.getVariable().isSetValue())
			{
				name += "-"+ argValues.getVariable().getValue();
			}
		}
		

		return name;
	}
	
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasDerivedSeries argSeries) {
		String name = "";
		
		if (argProject != null) {
			name += argProject.getTitle();
		}
		else {
			return name;
		}
		
		if (argSeries != null) {
			name += "-" + argSeries.getTitle();
		}
		else {
			return name;
		}
		
		return name;
	}
	
	@Override
	public String getDescription() {
		return I18n.getText("namingconvention.tellervohierarchical.description");
	}
	
	@Override
	public String getName() {
		return I18n.getText("namingconvention.tellervohierarchical");
	}
	
}
