package org.tridas.io.formats.csv;

import java.util.ArrayList;
import java.util.List;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroCollectionWriter;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.TridasValue;

/**
 * Basic Comma Separated Value file format.  Files are simple two column spreadsheets
 * with year in column 1 and value in column 2.
 * 
 * @todo add ring remarks column
 * @author peterbrewer
 *
 */
public class CSVFile implements IDendroFile {

	private TridasToCSVDefaults defaults;
	private ArrayList<Integer> data = new ArrayList<Integer>();
	private SafeIntYear startYear = new SafeIntYear(1001);
	
	public CSVFile(IMetadataFieldSet argDefaults){
		this.defaults = (TridasToCSVDefaults) argDefaults;
	}
	
	
	public void setSeries(ITridasSeries series) throws ConversionWarningException {
		
		// Set start year
		try{
			startYear = new SafeIntYear(series.getInterpretation().getFirstYear());
		} catch (NullPointerException e){}
		
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
		return "csv";
	}

	@Override
	public ITridasSeries[] getSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] saveToString() {

		StringBuilder string = new StringBuilder();
		

		SafeIntYear thisYear = startYear;
		
		string.append("Year,Value\n");
		
		for (Integer value : data)
		{
			string.append(thisYear.toString()+",");
			string.append(String.valueOf(value)+"\n");
			thisYear = thisYear.add(1);
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
