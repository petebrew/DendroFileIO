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
package org.tridas.io.warningsandexceptions;

import java.io.IOException;

/**
 * This exception is thrown when a Tridas entity is fundamentally not representable
 * in a legacy data format, for instance when it cannot hold data prior to a 
 * specified date. This should only be used when no legacy data file can be 
 * contstructed for the Tridas entity, <I>not</I> to indicate a that a metadata
 * field cannot be represented.  In this case use 
 * @see org.tridas.io.warningsandexceptions.ConversionWarningException
 * 
 * @author peterbrewer
 */
public class UnrepresentableTridasDataException extends IOException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Basic non-descriptive unrepresentable data exception
	 */
	public UnrepresentableTridasDataException() {
		super("TRiDaS class not representable in legacy data format");
	}
	
	/**
	 * Constructor for an unrepresentable data exception with
	 * descriptive message
	 * 
	 * @param s
	 */
	public UnrepresentableTridasDataException(String s) {
		// For now, just dump debug info
		System.out.println("Unrepresentable TRiDaS Data: " + s);
	}
}
