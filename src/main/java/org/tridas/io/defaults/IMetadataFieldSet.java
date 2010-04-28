/**
 * Created at: 9:22:50 PM, Apr 19, 2010
 */
package org.tridas.io.defaults;

import java.util.ArrayList;

import org.tridas.io.warnings.ConversionWarning;

/**
 * so the library works while i work on this upgrade
 * @author Daniel
 *
 */
public interface IMetadataFieldSet {
	
	/**
	 * Add a warning from validation
	 * @param argKey
	 * @param argWarning
	 */
	public void addIgnoredWarning(Enum<?> argKey, String argText);
	
	/**
	 * Add a warning from validation
	 * @param argKey
	 * @param argText
	 */
	public void addTruncatedWarning(Enum<?> argKey, String argText);
	
	/**
	 * Gets the conversion warnings from validations
	 * @return
	 */
	public ArrayList<ConversionWarning> getConversionWarnings();
	
	/**
	 * Get the default value object for the given Enum
	 * @param argValueType
	 * @return
	 */
	public AbstractDefaultValue<? extends Object> getDefaultValue(Enum<?> argValueType);
}
