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

import org.tridas.io.IDendroFile;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

/**
 * @author daniel
 */
public interface INamingConvention {
	
	public void registerFile(IDendroFile argFile, TridasProject argProject);
	
	public void registerFile(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries);
	
	public void registerFile(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries, TridasValues argValues);
	
	public void registerFile(IDendroFile argFile, TridasProject argProject, TridasDerivedSeries argSeries);
	
	
	public void registerFile(IDendroFile argFile, NamingConventionGrouper group);
	
	public String getFilename(IDendroFile argFile);
	
	public String getDescription();
	
	public String getName();
	
	/**
	 * Sets the filename manually.
	 * @param argFile
	 * @param argFilename
	 */
	public void setFilename(IDendroFile argFile, String argFilename);
}
