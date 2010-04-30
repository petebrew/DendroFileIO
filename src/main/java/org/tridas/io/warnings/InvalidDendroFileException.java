package org.tridas.io.warnings;

import org.tridas.io.I18n;

/**
 * This is thrown when a fatal error is found in the input dendro
 * data file.  The reason for the error is given along with the 
 * line number.
 * 
 * @author peterbrewer
 *
 */
public class InvalidDendroFileException extends Exception {

	private static final long serialVersionUID = 4354556879332983450L;
	private int linenumber;
	private String reason;
	
	/**
	 * Constructor for this exception
	 * 
	 * @param reason
	 * @param linenumber
	 */
	public InvalidDendroFileException(String reason, int linenumber){

		super(I18n.getText("fileio.fatalError")+": "+
				  reason + " "+
				  I18n.getText("fileio.errorAtLineNum", String.valueOf(linenumber))); 
		
		this.linenumber = linenumber;
		this.reason = reason;
	}
	
	/**
	 * Get the line number of the dendro file where this exception occurred
	 * 
	 * @return the linenumber
	 */
	public int getLinenumber() {
		return linenumber;
	}

	/**
	 * Get the reason for this exception
	 * 
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	
}
