package org.tridas.io.formats.belfastapple;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroCollectionWriter;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.belfastapple.TridasToBelfastAppleDefaults.BelfastAppleField;
import org.tridas.io.formats.trims.TridasToTrimsDefaults.TrimsField;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults.TucsonField;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.Date;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasValue;
import org.tridas.schema.Year;

public class BelfastAppleFile implements IDendroFile {
	
	private final IDendroCollectionWriter writer;
	private TridasToBelfastAppleDefaults defaults;
	private ArrayList<Integer> data = new ArrayList<Integer>();
	
	public BelfastAppleFile(IMetadataFieldSet argDefaults, IDendroCollectionWriter argWriter){
		this.defaults = (TridasToBelfastAppleDefaults) argDefaults;
		writer = argWriter;
	}
	
	/**
	 * Set the object title
	 * 
	 * @param title
	 * @throws ConversionWarningException
	 */
	public void setObjectTitle(String title) throws ConversionWarningException{	
		if(title!=null)
		{
			defaults.getStringDefaultValue(BelfastAppleField.OBJECT_TITLE).setValue(title);
		}
	}
	
	
	/**
	 * Set the sample title
	 * 
	 * @param title
	 * @throws ConversionWarningException
	 */
	public void setSampleTitle(String title) throws ConversionWarningException{	
		if(title!=null)
		{		
			defaults.getStringDefaultValue(BelfastAppleField.SAMPLE_TITLE).setValue(title);
		}
	}
	
	
	public void setSeries(ITridasSeries series) throws ConversionWarningException {
		
		// Extract ring widths from series
		List<TridasValue> valueList ;
		try{
			valueList = series.getValues().get(0).getValues();
		} catch (NullPointerException e){
			throw new ConversionWarningException(new ConversionWarning(
					WarningType.NULL_VALUE, 
					I18n.getText("fileio.noData")));
		}	
		try{
			for (TridasValue v : valueList)
			{
				Integer val = Integer.valueOf(v.getValue());
				data.add(val);
			}
		} catch (NumberFormatException e){
			throw new ConversionWarningException(new ConversionWarning(
					WarningType.INVALID, 
					I18n.getText("fileio.invalidDataValue")));
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
	public IDendroCollectionWriter getWriter() {
		return writer;
	}

	@Override
	public String[] saveToString() {

		StringBuilder string = new StringBuilder();
		
		string.append(defaults.getDefaultValue(BelfastAppleField.OBJECT_TITLE).getValue()+"\n");
		string.append(defaults.getDefaultValue(BelfastAppleField.SAMPLE_TITLE).getValue()+"\n");

			
		for (Integer value : data)
		{
			string.append(String.valueOf(value)+"\n");
		}
		
		
		return string.toString().split("\n");
		
	}

}
