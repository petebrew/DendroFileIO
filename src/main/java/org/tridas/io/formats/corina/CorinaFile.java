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
package org.tridas.io.formats.corina;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.corina.CorinaToTridasDefaults.DefaultFields;
import org.tridas.io.formats.corina.TridasToCorinaDefaults.CorinaDatingType;
import org.tridas.io.formats.corina.TridasToCorinaDefaults.CorinaSampleType;
import org.tridas.io.formats.corina.TridasToCorinaDefaults.CorinaTerminalRing;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.YearRange;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;


public class CorinaFile implements IDendroFile {

	private TridasToCorinaDefaults defaults;
	private TridasValues dataValues;
	private TridasValues wjValues;
	private ITridasSeries series;
	
	public CorinaFile(TridasToCorinaDefaults argDefaults) {
		defaults = argDefaults;
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String getExtension() {
		if(series instanceof TridasMeasurementSeries)
		{
			return "raw";
		}
		else
		{
			return "sum";
		}
	}
	
	public void setSeries(ITridasSeries argSeries) {
		
		series = argSeries;
	}
	
	public void setWJValues(TridasValues vals)
	{	
		wjValues = vals;
	}
	
	/**
	 * 
	 * @param vals
	 */
	public void setDataValues(TridasValues vals)
	{	
		dataValues = vals;
	}
	
	
	@Override
	public ITridasSeries[] getSeries() {
		return new ITridasSeries[]{series};
	}

	@Override
	public String[] saveToString() {
			
		ArrayList<String> file = new ArrayList<String>();
		String line = "";
	
		// Line 1 - NAME
		file.add(defaults.getStringDefaultValue(DefaultFields.NAME).getValue());
		
		// Line 2 - ID, NAME, DATING, UNMEAS_PRE, UNMEAS_POST
		line = "";
		if(defaults.getStringDefaultValue(DefaultFields.ID).getValue()!=null)
		{
			line += ";ID "+defaults.getStringDefaultValue(DefaultFields.ID).getValue();
		}
		if(defaults.getStringDefaultValue(DefaultFields.NAME).getValue()!=null)
		{
			line += ";NAME "+defaults.getStringDefaultValue(DefaultFields.NAME).getValue();
		}
		if(defaults.getDefaultValue(DefaultFields.DATING).getValue()!=null)
		{
			line += ";DATING "+ ((CorinaDatingType)defaults.getDefaultValue(DefaultFields.DATING).getValue()).toCode();
		}
		if(defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).getValue()!=null)
		{
			line += ";UNMEAS_PRE "+defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).getStringValue();
		}
		if(defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_POST).getValue()!=null)
		{
			line += ";UNMEAS_POST "+defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_POST).getStringValue();
		}
		file.add(line);
		
		// Line 3 - Filename
		if(defaults.getStringDefaultValue(DefaultFields.FILENAME).getValue()!=null)
		{
			file.add(";FILENAME "+defaults.getStringDefaultValue(DefaultFields.FILENAME).getValue());
		}

		// Line 4 - Species, Format, Pith
		line = "";
		if(defaults.getStringDefaultValue(DefaultFields.SPECIES).getValue()!=null)
		{
			line  = ";SPECIES "+defaults.getStringDefaultValue(DefaultFields.SPECIES).getValue();
		}
		if(defaults.getStringDefaultValue(DefaultFields.FORMAT).getValue()!=null)
		{
			line += ";FORMAT "+defaults.getStringDefaultValue(DefaultFields.FORMAT).getValue();
		}
		if(defaults.getStringDefaultValue(DefaultFields.PITH).getValue()!=null)
		{
			line += ";PITH "+defaults.getStringDefaultValue(DefaultFields.PITH).getValue();
		}
		file.add(line);

		// Line 5 - Terminal, Continuous, Quality
		line = "";
		if(((CorinaTerminalRing) defaults.getDefaultValue(DefaultFields.TERMINAL).getValue())!=null)
		{
			line  = ";TERMINAL "+((CorinaTerminalRing)defaults.getDefaultValue(DefaultFields.TERMINAL).getValue()).toCode();
		}
		if(defaults.getStringDefaultValue(DefaultFields.CONTINUOUS).getValue()!=null)
		{
			line += ";CONTINUOUS "+defaults.getStringDefaultValue(DefaultFields.CONTINUOUS).getValue();
		}
		if(defaults.getStringDefaultValue(DefaultFields.QUALITY).getValue()!=null)
		{
			line += ";QUALITY "+defaults.getStringDefaultValue(DefaultFields.QUALITY).getValue();
		}
		file.add(line);
		
		// Line 6 - Comments
		if(defaults.getStringDefaultValue(DefaultFields.COMMENTS).getValue()!=null)
		{
			file.add(";COMMENTS "+defaults.getStringDefaultValue(DefaultFields.COMMENTS).getValue());
		}
		
		// Line 7 - Type, IndexType, Reconciled, Sapwood
		line = "";
		if(((CorinaSampleType) defaults.getDefaultValue(DefaultFields.TYPE).getValue())!=null)
		{
			line  = ";TYPE "+((CorinaSampleType)defaults.getDefaultValue(DefaultFields.TYPE).getValue()).toCode();
		}
		if(defaults.getStringDefaultValue(DefaultFields.INDEX_TYPE).getValue()!=null)
		{
			line += ";INDEX_TYPE "+defaults.getStringDefaultValue(DefaultFields.INDEX_TYPE).getValue();
		}
		if(defaults.getStringDefaultValue(DefaultFields.RECONCILED).getValue()!=null)
		{
			line += ";RECONCILED "+defaults.getStringDefaultValue(DefaultFields.RECONCILED).getValue();
		}
		if(defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD).getValue()!=null)
		{
			line += ";SAPWOOD "+defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD).getStringValue();
		}
		file.add(line);

		
		// Data
		writeData(file);
		
		
		if(series instanceof TridasDerivedSeries)
		{
			TridasDerivedSeries dseries = ((TridasDerivedSeries) series);
			ArrayList<String> elementList = new ArrayList<String>();
			
			
			if(dseries.isSetLinkSeries())
			{
				for(SeriesLink sl : dseries.getLinkSeries().getSeries())
				{
					if(sl.isSetIdentifier())
					{
						elementList.add(sl.getIdentifier().getDomain()+":"+sl.getIdentifier().getValue());
					}
					/*else if (sl.isSetIdRef())
					{
						if(sl.getIdRef().getRef() instanceof ITridasSeries)
						{
							ITridasSeries linkedToSeries = ((ITridasSeries) sl.getIdRef().getRef());
							TridasIdentifier id = linkedToSeries.getIdentifier();
							if(id==null) 
							{
								elementList.add(I18n.getText("unamed.series"));
							}
							else
							{
								if(id.isSetDomain() && id.isSetValue())
								{
									elementList.add(id.getDomain()+":"+id.getValue());
								}
								else if (linkedToSeries.isSetTitle())
								{
									elementList.add(linkedToSeries.getTitle());
								}
								else
								{
									elementList.add(I18n.getText("unnamed.series"));
								}
							}
						}
					}*/
					else if (sl.isSetXLink())
					{
						elementList.add(sl.getXLink().getHref().toString());
					}
				}
			}
			
			if(elementList.size()>0)
			{
			
				file.add(";ELEMENTS");
				file.addAll(elementList);
				saveWeiserjahre(file);
			}
		}
		
		// Final line - username
		if(defaults.getStringDefaultValue(DefaultFields.USERNAME).getValue()!=null)
		{
			file.add("~ "+defaults.getStringDefaultValue(DefaultFields.USERNAME).getValue());
		}
		
		// Return array of lines
		return file.toArray(new String[0]);
		
	}
	
	// save the ;DATA section
	protected void writeData(ArrayList<String> file) 
	{
		file.add(";DATA         ");

		// clone the data, so i can add the infamous "9990".  (i can't
		// modify the data in-place, because that could cause all
		// sorts of problems, and it would be a huge mess to handle it
		// in a special case.)
		List<Object> data = new ArrayList<Object>();
		ArrayList<Integer> count = new ArrayList<Integer>();
		
		for(TridasValue v : dataValues.getValues())
		{
			data.add(v.getValue());
			if(v.isSetCount())
			{
				count.add(v.getCount());
			}
		}
		
		// Add end marker to data and count
		data.add(new Integer(9990));
		if(count.size()>0)
		{
			count.add(count.get(count.size() - 1));
		}

		// Get start year
		SafeIntYear startYear = new SafeIntYear(1001);
		if(series.isSetInterpretation())
		{
			if(series.getInterpretation().isSetFirstYear())
			{
				startYear = new SafeIntYear(series.getInterpretation().getFirstYear());
			}
		}
		
		// Get end year
		SafeIntYear endYear = startYear.add(data.size());
		YearRange range = new YearRange(startYear, endYear.add(-1));

		// row ends, for count info
		SafeIntYear rleft = null, rright;

		String line = "";
		// loop through years
		for (SafeIntYear y = range.getStart(); y.compareTo(range.getEnd()) <= 0; y = y
				.add(+1)) {
		
			// year
			if (range.startOfRow(y)) {
				line+=(StringUtils.leftPad(y.toString(), 5));
				rleft = y;
			}

			// pad the first row
			if (y.equals(range.getStart()) || y.isYearOne())
				for (int i = 0; i < y.column(); i++)
					line+="      ";

			// data: pad to 6 ("%-6d")
			line+=StringUtils.leftPad(data.get(y.diff(range.getStart()))
					.toString(), 6);
			// (FIXME: dies if data[i] = null -- so don't let nulls get here!)

			// newline
			if (range.endOfRow(y)) {
				file.add(line);
				line = "";
				rright = y;

				// last row is 4 cols to the left.  don't ask why.
				line+=rright.equals(range.getEnd()) ? "   " : "       "; // (3,7)

				// count (in brackets), for the line of data i just wrote
				if (rleft.equals(range.getStart()) || rleft.isYearOne())
					for (int i = 0; i < rleft.column(); i++)
						line+="      "; // (6)

				if (count.size()==0) {
					String c = StringUtils.leftPad("[1]", 6);
					for (SafeIntYear y1 = rleft; y1.compareTo(rright) <= 0; y1 = y1
							.add(+1)) {
						line+=c;
					}
				} else {
					for (SafeIntYear y1 = rleft; y1.compareTo(rright) <= 0; y1 = y1
							.add(+1)) {
						String c = count.get(y1.diff(range.getStart()))
								.toString();
						c = StringUtils.leftPad("[" + c + "]", 6);
						line+=c;
					}
				}

				file.add(line);
				line="";
			}
		}

		// blank line after s.data
		file.add("");
		line="";
	}
	
	private void saveWeiserjahre(ArrayList<String> file) 
	{
		// if no wj, do nothing
		if (wjValues==null)
			return;



		
		List<Object> data = new ArrayList<Object>();
		
		for(TridasValue v : wjValues.getValues())
		{
			if(v.getValue().matches("\\d/\\d"))
			{
				data.add(v.getValue());
			}
			else
			{
				return;
			}
		}
		
		
		file.add(";weiserjahre   ");
		
		// Get start year
		SafeIntYear startYear = new SafeIntYear(1001);
		if(series.isSetInterpretation())
		{
			if(series.getInterpretation().isSetFirstYear())
			{
				startYear = new SafeIntYear(series.getInterpretation().getFirstYear());
			}
		}
		
		// Get end year
		SafeIntYear endYear = startYear.add(data.size());
		YearRange range = new YearRange(startYear, endYear.add(-1));
		String line ="";
		for (SafeIntYear y = startYear; y.compareTo(endYear) < 0; y = y
				.add(1)) {

			// year: "%5d"
			if (y.equals(startYear))
				line +=StringUtils.leftPad(y.toString(), 5);

			// always use '/' in corina files			
			line+=StringUtils.leftPad(data.get(y.diff(range.getStart()))
					.toString(), 6);

			// newline
			if (y.column() == 9 || y.equals(endYear.add(-1)))
				file.add(line);
				line = "";
		}
		file.add(line);
		line="";
	}
	
}
