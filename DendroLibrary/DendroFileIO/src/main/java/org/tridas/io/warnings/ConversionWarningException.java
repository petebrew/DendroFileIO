package org.tridas.io.warnings;

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
		//toSystemOut();
	}
	
	/**
	 * Get the warning
	 * 
	 * @return
	 */
	public ConversionWarning getWarning()
	{
		return warning;
	}
	
	/**
	 * Write the warning to System.out
	 */
	private void toSystemOut()
	{
		System.out.println("**Conversion Warning**\n"
				+ "Type    : " + warning.getWarningType() +"\n"
				+ "Message : " + warning.getMessage() + "\n"
				+ "Field   : " + warning.getField() + "\n");
	}
}
