package org.tridas.io.formats.trims;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroCollectionWriter;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.trims.TridasToTrimsDefaults.TrimsField;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasValue;

public class TrimsFile implements IDendroFile {
	
	private final IDendroCollectionWriter writer;
	private TridasToTrimsDefaults defaults;
	private ArrayList<Integer> data = new ArrayList<Integer>();
	
	public TrimsFile(IMetadataFieldSet argDefaults, IDendroCollectionWriter argWriter){
		this.defaults = (TridasToTrimsDefaults) argDefaults;
		writer = argWriter;
	}
	
	
	/**
	 * Set the name of the author of this dataset
	 * 
	 * @param name
	 * @throws ConversionWarningException
	 */
	private void setAuthor(String name) throws ConversionWarningException{
		if(name == null){
			return;
		}
		
	     char ch;       // One of the characters in str.
	     char prevCh;   // The character that comes before ch in the string.
	     int i;         // A position in str, from 0 to str.length()-1.
	     prevCh = '.';  // Prime the loop with any non-letter character.
	     String initials = "";
	     for ( i = 0;  i < name.length();  i++ ) {
	        ch = name.charAt(i);
	        if ( Character.isLetter(ch)  &&  ! Character.isLetter(prevCh) )
	           initials += Character.toLowerCase(ch) ;
	        prevCh = ch;
	     }

		defaults.getStringDefaultValue(TrimsField.AUTHOR).setValue(initials);
	}
	
	
	/**
	 * Set the date this series was measured
	 * 
	 * @param date
	 * @throws ConversionWarningException
	 */
	private void setMeasuringDate(XMLGregorianCalendar date) throws ConversionWarningException{

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = dateFormat.format(date);
		
		defaults.getStringDefaultValue(TrimsField.MEASURING_DATE).setValue(dateStr);
	}
	
	
	/**
	 * Set the first year of this sequence
	 * 
	 * @param yr
	 * @throws ConversionWarningException
	 */
	private void setStartYear(SafeIntYear yr) throws ConversionWarningException{
		Integer year = null;
		try{
		year = Integer.valueOf(yr.toString());
		} catch (NumberFormatException e)
		{
			return;
		}
		
		defaults.getIntegerDefaultValue(TrimsField.START_YEAR).setValue(year);
		
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
		
		// Set start year
		try{
			SafeIntYear yr = new SafeIntYear(series.getInterpretation().getFirstYear());
			setStartYear(yr);
		} catch (NullPointerException e){}
		
		// Set date
		try{
			XMLGregorianCalendar date;
			if(series instanceof TridasMeasurementSeries)
			{
				date  = ((TridasMeasurementSeries)series).getMeasuringDate().getValue();
			}
			else
			{
				date = ((TridasDerivedSeries)series).getDerivationDate().getValue();
			}
			setMeasuringDate(date);
		} catch (NullPointerException e){}
		
		// Set Author
		try{
			String author;
			if(series instanceof TridasMeasurementSeries)
			{
				author  = ((TridasMeasurementSeries)series).getAnalyst();
			}
			else
			{
				author = ((TridasDerivedSeries)series).getAuthor();
			}
			setAuthor(author);
		} catch (NullPointerException e){}		
		
	}
	
	
	
	@Override
	public String getExtension() {
		return "rw";
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
		
		string.append(defaults.getDefaultValue(TrimsField.AUTHOR).getValue()+"\n");
		string.append(defaults.getDefaultValue(TrimsField.MEASURING_DATE).getValue()+"\n");
		string.append(defaults.getDefaultValue(TrimsField.START_YEAR).getValue()+"\n");
			
		for (Integer value : data)
		{
			string.append(" "+String.valueOf(value)+"\n");
		}
		
		
		return string.toString().split("\n");
		
	}

}
