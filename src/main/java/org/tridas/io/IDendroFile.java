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
package org.tridas.io;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.defaults.IMetadataFieldSet;

/**
 * Interface for all Dendro File Formats.
 * 
 * @author daniel
 */
public interface IDendroFile {
	
	
	
	/**
	 * Saves the file to strings.
	 * 
	 * @return
	 */
	public String[] saveToString();
	
	/**
	 * Get the series list.
	 * 
	 * @return
	 */
	public ITridasSeries[] getSeries();
	
	/**
	 * Gets the extension to use for saving this file.
	 * 
	 * @return
	 */
	public String getExtension();
	
	/**
	 * Gets the defaults that this file uses to save the file
	 * 
	 * @return
	 */
	public IMetadataFieldSet getDefaults();
	

}
