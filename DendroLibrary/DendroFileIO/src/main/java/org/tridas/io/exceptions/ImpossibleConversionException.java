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
package org.tridas.io.exceptions;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This exception is thrown when a conversion is not possible. 
 * 
 * This typically happens when the provided TRiDaS entity is incomplete e.g. when an entity with no child entities is passed such as
 * a TridasObject with no TridasElements.
 * 
 * It's also used when an output format is incapable of storing the data being passed to it e.g. a format that only handles ring widths
 * is passed a ring density data.
 * 
 * @author peterbrewer
 */
public class ImpossibleConversionException extends IOException {
	
	private static final Logger log = LoggerFactory.getLogger(ImpossibleConversionException.class);
	private static final long serialVersionUID = 1L;
	
	/**
	 * Basic non-descriptive missing data exception
	 */
	public ImpossibleConversionException() {
		super("Unable to convert to this output format");
	}
	
	/**
	 * Constructor for an incomplete data exception with
	 * descriptive message
	 * 
	 * @param s
	 */
	public ImpossibleConversionException(String s) {
		// For now, just dump debug info
		super(s);
		log.error(s);
	}
}
