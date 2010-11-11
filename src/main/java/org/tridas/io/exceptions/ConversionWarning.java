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

import org.tridas.io.I18n;

/**
 * A conversion warning is used to record when an issue has been detected
 * during the conversion of a file from or to the Tridas classes.
 * 
 * @author peterbrewer
 */
public class ConversionWarning {
	
	private WarningType type;
	private String message;
	private String field;
	
	/**
	 * Raise a warning about an aspect of the data conversion
	 * 
	 * @param warningType
	 *            The type of warning
	 * @param warningMessage
	 *            The warning message
	 */
	public ConversionWarning(WarningType warningType, String warningMessage) {
		type = warningType;
		message = warningMessage;
	}
	
	/**
	 * Raise a warning about an aspect of the data conversion
	 * 
	 * @param warningType
	 *            The type of warning
	 * @param warningMessage
	 *            The warning message
	 * @param problemField
	 *            The field where the problem comes from
	 */
	public ConversionWarning(WarningType warningType, String warningMessage, String problemField) {
		field = problemField;
		type = warningType;
		message = warningMessage;
	}
	
	/**
	 * Get the type of warning
	 * 
	 * @return
	 */
	public WarningType getWarningType() {
		return type;
	}
	
	/**
	 * Get the message for the warning
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Get the field that this warning applies to
	 * 
	 * @return
	 */
	public String getField() {
		return field;
	}
	
	@Override
	public String toString() {
		return type + " - " + message;
	}
	
	/**
	 * Get the warning message including the field involved
	 * if possible
	 * 
	 * @return
	 */
	public String toStringWithField() {
		return type + (field != null ? " (" + field +") ": " ") + " - " + message;
	}
	
	/**
	 * Warning types that can be raised
	 * 
	 * @author peterbrewer
	 */
	public enum WarningType {
		
		/**
		 * Ignored during conversion, typically because value did not
		 * strictly meet specifications
		 */
		IGNORED,
		/**
		 * Value had to be truncated to fit the output specifications
		 */
		TRUNCATED,
		/**
		 * Value was ambiguously translated therefore some change of
		 * meaning may have been made.
		 */
		AMBIGUOUS,
		/**
		 * Value was invalid
		 */
		INVALID,
		/**
		 * Value was set to default
		 */
		DEFAULT,
		/**
		 * Value was null
		 */
		NULL_VALUE,
		/**
		 * Value could not be represented in the output format so has
		 * been ignored.
		 */
		UNREPRESENTABLE,
		/**
		 * A work-around has been employed to handle a conversion problem
		 */
		WORK_AROUND,
		/**
		 * Value does not strictly adhere to the specification but has been
		 * used nonetheless
		 */
		NOT_STRICT,
		/**
		 * File was not read or writen to.
		 */
		FILE_IGNORED;
		
		/**
		 * Get the warning type as an internationalised string
		 */
		@Override
		public String toString() {
			switch (this) {
				case IGNORED :
					return I18n.getText("warningType.ignored");
				case TRUNCATED :
					return I18n.getText("warningType.truncated");
				case AMBIGUOUS :
					return I18n.getText("warningType.ambiguous");
				case NOT_STRICT :
					return I18n.getText("warningType.notStrict");
				case INVALID :
					return I18n.getText("warningType.invalidValue");
				case NULL_VALUE :
					return I18n.getText("warningType.nullValue");
				case UNREPRESENTABLE :
					return I18n.getText("warningType.unrepresentable");
				case WORK_AROUND :
					return I18n.getText("warningType.workAround");
				case DEFAULT :
					return I18n.getText("warningType.defaultValue");
				case FILE_IGNORED :
					return I18n.getText("warningType.fileIgnored");
				default :
					return "";
			}
		}
		
	}
	
}
