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
package org.tridas.io.defaults;

import java.util.ArrayList;
import java.util.Set;

import org.tridas.io.exceptions.ConversionWarning;

/**
 * TODO
 * so the library works while i work on this upgrade
 * 
 * @author Daniel
 */
public interface IMetadataFieldSet extends Cloneable{
	
	/**
	 * Add a warning from validation
	 * 
	 * @param argKey
	 * @param argWarning
	 */
	public void addIgnoredWarning(Enum<?> argKey, String argText);
	
	/**
	 * Add a warning from validation
	 * 
	 * @param argKey
	 * @param argText
	 */
	public void addTruncatedWarning(Enum<?> argKey, String argText);
	
	/**
	 * Gets the conversion warnings from validations
	 * 
	 * @return
	 */
	public ArrayList<ConversionWarning> getWarnings();
	
	public void addConversionWarning(ConversionWarning argWarning);
	
	/**
	 * Get the default value object for the given Enum
	 * 
	 * @param argValueType
	 * @return
	 */
	public AbstractDefaultValue<? extends Object> getDefaultValue(Enum<?> argValueType);
	
	
	public Set<Enum<?>> keySet();
	
	/**
	 * Clones values to new {@link IMetadataFieldSet}.
	 * 
	 * @return
	 */
	public Object clone();
}
