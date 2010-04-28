package org.tridas.io.warnings;

import org.tridas.io.I18n;

public class IncorrectDefaultFieldsException extends Exception {
	private static final long serialVersionUID = -8642265757250345131L;

	public IncorrectDefaultFieldsException(Class<?> argCorrectClass){
		super(I18n.getText("tridas.incorrectIDefaultFields")); 
	}
}
