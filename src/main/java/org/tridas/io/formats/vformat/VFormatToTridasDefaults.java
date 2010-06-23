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
package org.tridas.io.formats.vformat;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasSample;

public class VFormatToTridasDefaults extends TridasMetadataFieldSet {
	
	public TridasObject getObjectWithDefaults(String objectid) {
		TridasObject o = getObjectWithDefaults();
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(objectid);
		id.setDomain(getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		o.setIdentifier(id);
		return o;
	}
	
	public TridasElement getElementWithDefaults(String elementid) {
		TridasElement e = getElementWithDefaults();
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(elementid);
		id.setDomain(getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		e.setIdentifier(id);
		return e;
	}
	
	public TridasSample getSampleWithDefaults(String sampleid) {
		TridasSample s = getSampleWithDefaults();
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(sampleid);
		id.setDomain(getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		s.setIdentifier(id);
		return s;
	}
}
