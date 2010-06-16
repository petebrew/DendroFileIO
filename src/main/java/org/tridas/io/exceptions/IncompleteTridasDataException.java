/**
 * pboon: need to have some new header
 */

//
// This file is part of Corina.
//
// Corina is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// Corina is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Corina; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// Copyright 2001 Ken Harris <kbh7@cornell.edu>
//
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
