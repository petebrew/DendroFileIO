package org.tridas.io.exceptions;

import org.tridas.io.I18n;

/**
 * This is thrown when a fatal error is found in the input dendro
 * data file. The reason for the error is given along with the
 * line number.
 * 
 * @author peterbrewer
 */
public class InvalidDendroFileException extends Exception {
	
	private static final long serialVersionUID = 4354556879332983450L;
	private Integer pointerNumber;
	private String reason;
	private PointerType pointerType = PointerType.LINE;
	
	public enum PointerType {
		LINE, BYTE;
		
		@Override
		public String toString() {
			if (this == PointerType.LINE) {
				return I18n.getText("general.line");
			}
			else if (this == PointerType.BYTE) {
				return I18n.getText("general.byte");
			}
			return null;
			
		}
	}
	
	/**
	 * Constructor for this exception when there is no specific
	 * position in the file where the error occurred e.g. no data
	 * in file.
	 * 
	 * @param reason
	 */
	public InvalidDendroFileException(String reason) {
		this.reason = reason;
		pointerNumber = null;
	}
	
	/**
	 * Constructor for this exception when providing line number of
	 * erroneous input file
	 * 
	 * @param reason
	 * @param linenumber
	 */
	public InvalidDendroFileException(String reason, int linenumber) {
		pointerNumber = linenumber;
		this.reason = reason;
	}
	
	/**
	 * Construct an exception. Requires a reason, a counter (either line or byte)
	 * to say whereabouts in the input file the error occurred, and a PointerType
	 * to say whether the counter is a line or byte count.
	 * 
	 * @param reason
	 * @param pointernumber
	 * @param type
	 */
	public InvalidDendroFileException(String reason, int pointernumber, PointerType type) {
		pointerType = type;
		pointerNumber = pointernumber;
		this.reason = reason;
	}
	
	/**
	 * Get the line or byte number of the dendro file where this exception occurred
	 * 
	 * @return the pointernumber
	 */
	public Integer getPointerNumber() {
		return pointerNumber;
	}
	
	/**
	 * Get the reason for this exception
	 * 
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}
	
	/**
	 * @return the pointerType
	 */
	public PointerType getPointerType() {
		return pointerType;
	}
	
	@Override
	public String getLocalizedMessage() {
		if (pointerNumber != null) {
			return I18n.getText("fileio.fatalError") + ": " + reason + ".  "
					+ I18n.getText("fileio.errorAt", pointerNumber + "", pointerType.toString().toLowerCase());
		}
		else {
			return I18n.getText("fileio.fatalError") + ": " + reason + ".  ";
		}
	}
}
