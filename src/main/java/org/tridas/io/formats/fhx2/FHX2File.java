package org.tridas.io.formats.fhx2;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.fhx2.FHX2Writer.FHX2Series;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.io.util.YearRange;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasRemark;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasRemark;
import org.tridas.schema.TridasValue;

public class FHX2File implements IDendroFile {

	private ArrayList<FHX2Series> seriesList = new ArrayList<FHX2Series>();
	private YearRange yrRange;
	private DatingSuffix calendar = DatingSuffix.AD;
	private static final Logger log = LoggerFactory.getLogger(FHX2File.class);
	private TridasToFHX2Defaults defaults = new TridasToFHX2Defaults();
	
	public void setSeriesList(ArrayList<FHX2Series> lst) {
				
		// Switch the BP dating if any series are in BP
		for (FHX2Series s : lst) {
			
			ITridasSeries ser = s.series;
			
			if (calendar == DatingSuffix.BP) {
				break;
			}
			try {
				if (ser.getInterpretation().getFirstYear().getSuffix() == DatingSuffix.BP) {
					calendar = DatingSuffix.BP;
				}
			} catch (NullPointerException e) {}
		}
		
		
		
		
		// Calculate the range for these series
		for (FHX2Series s : lst) {
			
			ITridasSeries ser = s.series;
			
			Integer ringcount = ser.getValues().get(0).getValues().size();
			SafeIntYear startYear = null;
			try {
				
				// Make sure we're using years with the right calendar
				//Year yearsWithCalendar = new SafeIntYear(ser.getInterpretation().getFirstYear()).toTridasYear(calendar);
				startYear = new SafeIntYear(ser.getInterpretation().getFirstYear());
				
			} catch (NullPointerException e) {
				startYear = new SafeIntYear();
			}
			
			YearRange thisrange = new YearRange(startYear, ringcount);
			
			if (yrRange == null) {
				yrRange = thisrange;
			}
			
			yrRange = yrRange.union(thisrange);
		}
		
		// Set the list
		seriesList = lst;
	}
	
	public void setDefaults(TridasToFHX2Defaults defaults)
	{
		this.defaults = defaults;
	}
	
	private ArrayList<String> getMetadata()
	{
		ArrayList<String> lines = new ArrayList<String>();
		
		lines.add("Name of site   : "+defaults.getSiteTitle());
		lines.add("Site code      : "+defaults.getSiteCode());
		lines.add("Collection date: "+defaults.getCollectionDate());
		lines.add("Collectors     : ");
		lines.add("Crossdaters    : ");
		lines.add("Number samples : "+seriesList.size()); 
		lines.add("Species name   : "+defaults.getTaxon());
		lines.add("Common name    : ");
		lines.add("Habitat type   : "); 
		lines.add("Country        : "+defaults.getCountry());
		lines.add("State          : "+defaults.getState());
		lines.add("County         : ");
		lines.add("Park/Monument  : ");
		lines.add("National Forest: ");
		lines.add("Ranger district: ");
		lines.add("Township       : "+defaults.getTown());
		lines.add("Range          : ");
		lines.add("Section        : ");
		lines.add("Quarter section: ");
		lines.add("UTM easting    : ");
		lines.add("UTM northing   : ");
		lines.add("Latitude       : "+defaults.getLatitude());
		lines.add("Longitude      : "+defaults.getLongitude());
		lines.add("Topographic map: ");
		lines.add("Lowest elev    : "+defaults.getMinimumAltitude());
		lines.add("Highest elev   : "+defaults.getMaximumAltitude());
		lines.add("Slope          : "+defaults.getSlope());
		lines.add("Aspect         : "+defaults.getAspect());
		lines.add("Area sampled   : ");
		lines.add("Substrate type : "+defaults.getSubstrate());
		lines.add("Begin comments BELOW this line: "+defaults.getComments());
		lines.add("End comments ABOVE this line. ");
		lines.add(" ");
		
		
		return lines;
	}
	
	@Override
	public String[] saveToString() {	
		OdfSpreadsheetDocument outputDocument;
		ArrayList<String> lines = new ArrayList<String>();
		
		try {
			// Compile spreadsheet using ODFToolkit
			outputDocument = OdfSpreadsheetDocument.newSpreadsheetDocument();
			OdfTable table; 
			table = outputDocument.getTableList().get(0);
			
			// Write header
			table.getCellByPosition(0, 0).setStringValue("FHX2");
			table.getCellByPosition(2, 0).setStringValue("FORMAT");
			table.getCellByPosition(0, 1).setStringValue(yrRange.getStart().toString());  // Start year
			table.getCellByPosition(2, 1).setStringValue(seriesList.size()+"");     // Sample size
			int labelsize = 0;
			for (FHX2Series s : seriesList) {

				if(getLabelForSeries(s).length()>labelsize) labelsize = getLabelForSeries(s).length();
			}
			table.getCellByPosition(4, 1).setStringValue(labelsize+"");
					
			// Write years column
			SafeIntYear yr = yrRange.getStart();
			Integer rowNumber = labelsize+2;
			String yearval;
			while (yr.compareTo(yrRange.getEnd()) <= 0) {
				yearval = "{"+yr.toAstronomicalInteger()+"}";
				
				table.getCellByPosition(seriesList.size()+1, rowNumber).setStringValue(yearval);
				// Increment to next year and row number
				yr = yr.add(1);
				rowNumber++;
			}
			
			// Write data columns
			int col =0;
			for (FHX2Series s : seriesList) {
				
				if(!s.series.isSetValues())
				{
					continue;
				}
				writeDataColumn(table, col, s, labelsize);
				col++;
			}	
			
			// Convert ODF Spreadsheet into CSV doc
			for(int row=0; row < table.getRowCount(); row++)
			{
				String line = "";
				for(int column=0; column < table.getColumnCount(); column++)
				{
					String cell = table.getCellByPosition(column, row).getStringValue();
					
					if(cell.isEmpty())
					{
						cell = " ";
					}
					line+=cell;
				}
				//line = line.substring(0, line.length()-2) ;
				lines.add(line);
			}
			ArrayList<String> metadata = getMetadata();
			
			metadata.addAll(lines);
			
			return metadata.toArray(new String[0]);
			
		} catch (Exception e) {

			log.warn("Failed to write to file");
			e.printStackTrace();
			return null;
		}

	}


	private String getLabelForSeries(FHX2Series series)
	{
		// Creates year label
		String l;
		String keycode = null;
		if(series.series.isSetGenericFields())
		{
			for(TridasGenericField gf : series.series.getGenericFields())
			{
				if(gf.getName().toLowerCase().equals("keycode"))
				{
					keycode = gf.getValue();
				}
			}
		}
		if(keycode!=null)
		{
			l = keycode;
		}
		else
		{
		    l = series.series.getTitle();
		}
		
		
		return l;
	}
	
	/**
	 * Write the ring widths for the provided series in the specified column
	 * 
	 * @param table
	 * @param col
	 * @param series
	 */
	private void writeDataColumn(OdfTable table, Integer col, FHX2Series series, int labelSize)  {
		
		// Write label cells
		String label = StringUtils.rightPad(getLabelForSeries(series), labelSize);
		for (int i = 0; i < label.length(); i++){
		    String c = label.substring(i, i+1);        
		    table.getCellByPosition(col, i+2).setStringValue(c);
		}
		
		int row=labelSize+2;
		Boolean isRecording = false;
		Boolean reachedData = false;
		for(SafeIntYear currYear = yrRange.getStart(); currYear.compareTo(yrRange.getEnd())<=0; currYear = currYear
				.add(1))
		{
			
			SafeIntYear firstYearInSeries = series.getFirstYear();
			SafeIntYear lastYearInSeries = series.getLastYear();
			
			
			if((currYear.compareTo(firstYearInSeries)<0) || (currYear.compareTo(lastYearInSeries.add(-1))>0))
			{
				// Before first year or after last year
				table.getCellByPosition(col, row).setStringValue(".");  
			}
			else if (currYear.compareTo(lastYearInSeries.add(-1))==0)
			{
				// Last year of data
				if(series.isBarkKnown())
				{
					table.getCellByPosition(col, row).setStringValue("]");
				}
				else
				{
					table.getCellByPosition(col, row).setStringValue("}");
				}
				reachedData=true;
			}
			else if (reachedData==false)
			{
				// First year of data
				if(series.isPithKnown())
				{
					table.getCellByPosition(col, row).setStringValue("[");
				}
				else
				{
					table.getCellByPosition(col, row).setStringValue("{");
				}
				reachedData=true;
			}
			else
			{
				// Either a recording year or an event
				
				TridasValue val = series.getValueForYear(currYear);
				
				if(val==null)
				{
					log.warn("Failed to get value from TridasValues by year");
				}
				
				String  mark = "|";
				if(val.isSetRemarks())
				{
					for (TridasRemark remark : val.getRemarks())
					{
						if(remark.isSetNormalStd() && remark.getNormalStd().equals("FHX2"))
						{
							if(remark.isSetNormalId())
							{
								if(remark.getNormalId().equals("U") 
									|| remark.getNormalId().equals("u")
									|| remark.getNormalId().equals("A")
									|| remark.getNormalId().equals("a")
									|| remark.getNormalId().equals("L")
									|| remark.getNormalId().equals("l")
									|| remark.getNormalId().equals("M")
									|| remark.getNormalId().equals("m")
									|| remark.getNormalId().equals("E")
									|| remark.getNormalId().equals("e")
									|| remark.getNormalId().equals("D")
									|| remark.getNormalId().equals("d"))
								{
									mark = remark.getNormalId();
								}
							}
						}
					}
				}
				
				// No event to mark as recording year
				table.getCellByPosition(col, row).setStringValue(mark);
				
			}
			
			row++;
		}
				
	}

	

	@Override
	public String getExtension() {
		return "fhx";
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		return seriesList.toArray(new ITridasSeries[0]);

	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

}
