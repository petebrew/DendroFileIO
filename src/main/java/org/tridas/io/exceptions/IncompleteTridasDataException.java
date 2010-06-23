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

/**
 * This exception is thrown when a Tridas entity is not complete. This
 * typically happens when an entity with no child entities is passed e.g.
 * a TridasObject with no TridasElements.
 * 
 * @author peterbrewer
 */
public class IncompleteTridasDataException extends IOException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Basic non-descriptive missing data exception
	 */
	public IncompleteTridasDataException() {
		super("Missing data from TRiDaS classes");
	}
	
	/**
	 * Constructor for an incomplete data exception with
	 * descriptive message
	 * 
	 * @param s
	 */
	public IncompleteTridasDataException(String s) {
		// For now, just dump debug info
		System.out.println("Incomplete TRiDaS Data: " + s);
	}
}
