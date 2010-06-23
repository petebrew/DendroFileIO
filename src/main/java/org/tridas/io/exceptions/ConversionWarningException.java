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
 * A problem was found when converting between dendro formats
 * 
 * @author peterbrewer
 */
public class ConversionWarningException extends IOException {
	
	private static final long serialVersionUID = 1L;
	
	private ConversionWarning warning;
	
	/**
	 * Raise a conversion exception
	 * 
	 * @param cw
	 */
	public ConversionWarningException(ConversionWarning cw) {
		
		warning = cw;
		
		// For now, just dump debug info
		// toSystemOut();
	}
	
	/**
	 * Get the warning
	 * 
	 * @return
	 */
	public ConversionWarning getWarning() {
		return warning;
	}
	
	/**
	 * Write the warning to System.out
	 */
	private void toSystemOut() {
		System.out.println("**Conversion Warning**\n" + "Type    : " + warning.getWarningType() + "\n" + "Message : "
				+ warning.getMessage() + "\n" + "Field   : " + warning.getField() + "\n");
	}
}
