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

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * Used when an appropriate resource bundle cannot be found -
 * in the future perhaps I18N can be changed from loading the
 * resource bundle in a static initializer to an explicit init
 * method called during startup which upon failure can display
 * an error to the user.
 * 
 * @author Aaron Hamid arh14 at cornell.edu
 */
public class DefaultResourceBundle extends ResourceBundle {
	@Override
	protected Object handleGetObject(String key) {
		return key;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getKeys() {
		return EMPTY_ENUMERATION;
	}
	
	@SuppressWarnings("unchecked")
	private static final Enumeration EMPTY_ENUMERATION = new Enumeration() {
		public boolean hasMoreElements() {
			return false;
		}
		
		public Object nextElement() {
			throw new NoSuchElementException();
		}
	};
}