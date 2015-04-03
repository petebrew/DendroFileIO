package org.tridas.io.formats.csvmatrix;

import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.formats.csvmatrix.TridasToMatrixDefaults.DefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.YearRange;
import org.tridas.schema.TridasValues;

public class CSVMatrixFile implements IDendroFile {
	private static final Logger log = LoggerFactory.getLogger(CSVMatrixFile.class);

	private TridasToMatrixDefaults defaults = new TridasToMatrixDefaults();
	private ArrayList<MatrixSeries> seriesList = new ArrayList<MatrixSeries>();
	private YearRange fileYearRange;
	
	public CSVMatrixFile()
	{
		
	}
	
	public void setDefaults(TridasToMatrixDefaults defaults)
	{
		this.defaults = defaults;
	}
	
	@Override
	public String[] saveToString() {
		
		String[] lines = new String[fileYearRange.span()+1];
		
		ArrayList<String[]> matrix = getMatrix();
		
		for(int i=0; i< lines.length; i++)
		{
			String line = "";
			for(String[] column : matrix)
			{
				if(column[i]==null)
				{
					line += ",";
				}
				else
				{
					line += column[i]+",";
				}
			}
			lines[i] = line.substring(0, line.length()-1);
		}
		
		
		return lines;
	}

	@Override
	public ITridasSeries[] getSeries() {
		ITridasSeries[] list = new ITridasSeries[seriesList.size()];
		
		for(int i=0; i<seriesList.size(); i++)
		{
			list[i] = seriesList.get(i).series;
		}
		
		return list;
	}

	@Override
	public String getExtension() {
		return "txt";
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		
		return defaults;
	}
	
	public void addSeries(TridasToMatrixDefaults def, ITridasSeries series, TridasValues values)
	{
		MatrixSeries matrixseries = new MatrixSeries(def, series, values);
		seriesList.add(matrixseries);
		
		if(fileYearRange==null)
		{
			fileYearRange = matrixseries.range;
		}
		else
		{
			fileYearRange = fileYearRange.union(matrixseries.range);
		}
	}
	
	
	public String[] fieldnames = new String[]{"Field", 
			"Object title", 
			"Object identifier",
			"Object created timestamp",
			"Object updated timestamp",
			"Object comments",
			"Object type",  
			"Object description",
			"Object location latitude",
			"Object location longitude",
			"Object location type",
			"Object location precision",
			"Object location comment",
			"Object location city/town",
			"Object location state",
			"Object location country",
			
			"Element title",
			"Element identifier",
			"Element created timestamp",
			"Element updated timestamp",
			"Element comments",
			"Element type",
			"Element description",
			"Element taxon",
			"Element shape",
			"Element dimensions unit",
			"Element dimensions height",
			"Element dimensions width",
			"Element dimensions depth",
			"Element dimensions diameter",
			"Element authenticity",
			"Element location lat",
			"Element location lon",
			"Element locaiton type",
			"Element location precision",
			"Element location comment",
			"Element location citytown",
			"Element location state",
			"Element location country",
			"Element processing",
			"Element marks",
			"Element elevation",
			"Element slope angle",
			"Element slope azimuth",
			"Element soil depth",
			"Element soil description",
			"Element bedrock",
		};
	public ArrayList<String[]> getMetadataMatrix()
	{
		ArrayList<String[]> matrix = new ArrayList<String[]>();
		
		// First column - field names
		matrix.add(fieldnames);
		
		
		
		// Create a HashSet of variables so we can see if they need to be included in column headers
		HashSet<String> variables = new HashSet<String>();
		for(MatrixSeries s : seriesList)
		{
			if(s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue()!=null && s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue().length()>0)
			{
				variables.add(s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue());
			}
		}
		
		// Now the values columns
		String[] valuecolumns = new String[fieldnames.length];
		for(MatrixSeries s : seriesList)
		{
			valuecolumns = new String[fieldnames.length];
			
			// Header
			String header = s.def.getStringDefaultValue(DefaultFields.SERIES_TITLE).getStringValue();
			
			// Add variable type to column heading if there are different variables in file
			if(variables.size()>1 && s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue()!=null && s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue().length()>0)
			{
				header+=" ("+s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue()+")";
			}
			valuecolumns[0] = header.replace(",", "_");
			
			
			// Field values
			valuecolumns[1]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_TITLE).getStringValue();
			valuecolumns[2]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_IDENTIFIER).getStringValue();
			valuecolumns[3]  = s.def.getDateTimeDefaultValue(DefaultFields.OBJECT_CREATED_TIMESTAMP).getStringValue();
			valuecolumns[4]  = s.def.getDateTimeDefaultValue(DefaultFields.OBJECT_UPDATED_TIMESTAMP).getStringValue();				
			valuecolumns[5]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_COMMENTS).getStringValue();				
			valuecolumns[6]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_TYPE).getStringValue();
			valuecolumns[7]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_DESCRIPTION).getStringValue();
			valuecolumns[8]  = s.def.getDoubleDefaultValue(DefaultFields.OBJECT_LOCATION_LAT).getStringValue();
			valuecolumns[9]  = s.def.getDoubleDefaultValue(DefaultFields.OBJECT_LOCATION_LON).getStringValue();
			valuecolumns[10] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_TYPE).getStringValue();
			valuecolumns[11] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_PRECISION).getStringValue();
			valuecolumns[12] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_COMMENT).getStringValue();
			valuecolumns[13] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_CITYTOWN).getStringValue();
			valuecolumns[14] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_STATE).getStringValue();
			valuecolumns[15] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_COUNTRY).getStringValue();
			
			valuecolumns[16] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_TITLE).getStringValue();
			valuecolumns[17] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_IDENTIFIER).getStringValue();
			valuecolumns[18] = s.def.getDateTimeDefaultValue(DefaultFields.ELEMENT_CREATED_TIMESTAMP).getStringValue();
			valuecolumns[19] = s.def.getDateTimeDefaultValue(DefaultFields.ELEMENT_UPDATED_TIMESTAMP).getStringValue();
			valuecolumns[20] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_COMMENTS).getStringValue();
			valuecolumns[21] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_TYPE).getStringValue();
			valuecolumns[22] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_DESCRIPTION).getStringValue();
			valuecolumns[23] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_TAXON).getStringValue();
			valuecolumns[24] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_SHAPE).getStringValue();
			valuecolumns[25] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_UNIT).getStringValue();
			valuecolumns[26] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_HEIGHT).getStringValue();
			valuecolumns[27] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_WIDTH).getStringValue();
			valuecolumns[28] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DEPTH).getStringValue();
			valuecolumns[29] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DIAMETER).getStringValue();
			valuecolumns[30] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_AUTHENTICITY).getStringValue();
			valuecolumns[31] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_LOCATION_LAT).getStringValue();
			valuecolumns[32] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_LOCATION_LON).getStringValue();
			valuecolumns[33] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCAITON_TYPE).getStringValue();
			valuecolumns[34] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_PRECISION).getStringValue();
			valuecolumns[35] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_COMMENT).getStringValue();
			valuecolumns[36] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_CITYTOWN).getStringValue();
			valuecolumns[37] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_STATE).getStringValue();
			valuecolumns[38] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_COUNTRY).getStringValue();
			valuecolumns[39] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_PROCESSING).getStringValue();
			valuecolumns[40] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_MARKS).getStringValue();
			valuecolumns[41] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_ELEVATION).getStringValue();
			valuecolumns[42] = s.def.getIntegerDefaultValue(DefaultFields.ELEMENT_SLOPE_ANGLE).getStringValue();
			valuecolumns[43] = s.def.getIntegerDefaultValue(DefaultFields.ELEMENT_SLOPE_AZIMUTH).getStringValue();
			valuecolumns[44] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_SOIL_DEPTH).getStringValue();
			valuecolumns[45] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_SOIL_DESCRIPTION).getStringValue();
			valuecolumns[46] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_BEDROCK).getStringValue();
			
			matrix.add(valuecolumns);
		}
		
		
		return matrix;
	}
	
	public ArrayList<String[]> getMatrix()
	{
		ArrayList<String[]> matrix = new ArrayList<String[]>();
		
		// First column - year headers
		String[] yearcolumn = new String[fileYearRange.span()+1];
		yearcolumn[0] = "Years ";
		if(defaults.getStringDefaultValue(DefaultFields.DATING_TYPE).getValue()!=null)
		{
			yearcolumn[0] = yearcolumn[0] + "("+defaults.getStringDefaultValue(DefaultFields.DATING_TYPE).getValue()+")";
		}
		int ind = 1;
		
		SafeIntYear start = fileYearRange.getStart();
		SafeIntYear end = fileYearRange.getEnd();
		
		for(SafeIntYear currYear =start; currYear.compareTo(end)<=0; currYear = currYear.add(1))
		{
			yearcolumn[ind] = currYear.toString();
			ind++;
		}
		matrix.add(yearcolumn);
		
		// Create a HashSet of variables so we can see if they need to be included in column headers
		HashSet<String> variables = new HashSet<String>();
		for(MatrixSeries s : seriesList)
		{
			if(s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue()!=null && s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue().length()>0)
			{
				variables.add(s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue());
			}
		}
		
		// Now the data columns
		String[] datacolumn = new String[fileYearRange.span()+1];
		for(MatrixSeries s : seriesList)
		{
			datacolumn = new String[fileYearRange.span()+1];
			
			// Header
			String header = s.def.getStringDefaultValue(DefaultFields.SERIES_TITLE).getStringValue();
			
			// Add variable type to column heading if there are different variables in file
			if(variables.size()>1 && s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue()!=null && s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue().length()>0)
			{
				header+=" ("+s.def.getStringDefaultValue(DefaultFields.VARIABLE).getValue()+")";
			}
			datacolumn[0] = header.replace(",", "_");
			
			
			// Data values
			if(s.values.getValues().size()>0)
			{	
				int yearIndex = fileYearRange.getStart().diff(s.range.getStart())+1;
				if(yearIndex<0) yearIndex = 0- yearIndex+2;
							
				for(int i=0; i<s.values.getValues().size(); i++)
				{
					datacolumn[yearIndex] = s.values.getValues().get(i).getValue().replace(",", "_");
					yearIndex++;
				}
			}
			
			matrix.add(datacolumn);
		}
		
		
		return matrix;
	}

	
	public class MatrixSeries
	{
		public TridasToMatrixDefaults def;
		public ITridasSeries series;
		public TridasValues values;
		public YearRange range = new YearRange();
		
		public MatrixSeries(TridasToMatrixDefaults def, ITridasSeries series, TridasValues values)
		{
			this.def = def;
			this.series = series;
			this.values = values;
			
			SafeIntYear startYear = null;
			Integer years = null;
			try{
				startYear = new SafeIntYear(series.getInterpretation().getFirstYear());
			} catch (NullPointerException e)
			{
				startYear = new SafeIntYear(1);
			}
			
			try{
				years = values.getValues().size();
			} catch (NullPointerException e)
			{
				years =0;
			}
			
			range = new YearRange(startYear, years);
		}
		
		
	}
}
