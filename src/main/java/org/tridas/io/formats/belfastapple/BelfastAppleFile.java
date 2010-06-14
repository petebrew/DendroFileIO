package org.tridas.io.formats.belfastapple;

import java.util.ArrayList;
import java.util.List;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.belfastapple.TridasToBelfastAppleDefaults.BelfastAppleField;
import org.tridas.io.warningsandexceptions.ConversionWarning;
import org.tridas.io.warningsandexceptions.ConversionWarningException;
import org.tridas.io.warningsandexceptions.ConversionWarning.WarningType;
import org.tridas.schema.TridasValue;

public class BelfastAppleFile implements IDendroFile {
	
	private TridasToBelfastAppleDefaults defaults;
	private ArrayList<Integer> data = new ArrayList<Integer>();
	
	public BelfastAppleFile(IMetadataFieldSet argDefaults, AbstractDendroCollectionWriter argWriter) {
		defaults = (TridasToBelfastAppleDefaults) argDefaults;
	}
	
	/**
	 * Set the object title
	 * 
	 * @param title
	 * @throws ConversionWarningException
	 */
	public void setObjectTitle(String title) throws ConversionWarningException {
		if (title != null) {
			defaults.getStringDefaultValue(BelfastAppleField.OBJECT_TITLE).setValue(title);
		}
	}
	
	/**
	 * Set the sample title
	 * 
	 * @param title
	 * @throws ConversionWarningException
	 */
	public void setSampleTitle(String title) throws ConversionWarningException {
		if (title != null) {
			defaults.getStringDefaultValue(BelfastAppleField.SAMPLE_TITLE).setValue(title);
		}
	}
	
	public void setSeries(ITridasSeries series) throws ConversionWarningException {
		
		// Extract ring widths from series
		List<TridasValue> valueList;
		try {
			valueList = series.getValues().get(0).getValues();
		} catch (NullPointerException e) {
			throw new ConversionWarningException(new ConversionWarning(WarningType.NULL_VALUE, I18n
					.getText("fileio.noData")));
		}
		try {
			for (TridasValue v : valueList) {
				Integer val = Integer.valueOf(v.getValue());
				data.add(val);
			}
		} catch (NumberFormatException e) {
			throw new ConversionWarningException(new ConversionWarning(WarningType.INVALID, I18n
					.getText("fileio.invalidDataValue")));
		}
	}
	
	@Override
	public String getExtension() {
		return "txt";
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String[] saveToString() {
		
		StringBuilder string = new StringBuilder();
		
		string.append(defaults.getDefaultValue(BelfastAppleField.OBJECT_TITLE).getValue() + "\n");
		string.append(defaults.getDefaultValue(BelfastAppleField.SAMPLE_TITLE).getValue() + "\n");
		
		for (Integer value : data) {
			string.append(String.valueOf(value) + "\n");
		}
		
		return string.toString().split("\n");
		
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
}
