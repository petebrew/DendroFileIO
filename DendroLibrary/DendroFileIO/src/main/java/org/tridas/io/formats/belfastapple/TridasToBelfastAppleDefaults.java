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
package org.tridas.io.formats.belfastapple;

import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;

/**
 * @author peterbrewer
 */
public class TridasToBelfastAppleDefaults extends AbstractMetadataFieldSet {
	
	public enum BelfastAppleField {
		OBJECT_TITLE, SAMPLE_TITLE, RING_COUNT;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		setDefaultValue(BelfastAppleField.OBJECT_TITLE, new StringDefaultValue(I18n.getText("unnamed.object")));
		setDefaultValue(BelfastAppleField.SAMPLE_TITLE, new StringDefaultValue(I18n.getText("unnamed.sample")));
		setDefaultValue(BelfastAppleField.RING_COUNT, new IntegerDefaultValue(0));
	}
	
}
