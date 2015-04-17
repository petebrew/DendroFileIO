package org.tridas.io.formats.csvmatrix;

import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
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
			
			"Project title", 
			"Project identifier",
			"Project created timestamp",
			"Project updated timestamp",
			"Project comments",
			"Project type",  
			"Project description",
			"Project category",
			"Project investigator",
			"Project period",
			"Project request date",
			"Project commissioner",
			
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
			"Element location latitude",
			"Element location longitude",
			"Element location type",
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
			"Sample title",
			"Sample identifier",
			"Sample created timestamp",
			"Sample updated timestamp",
			"Sample comments",
			"Sample type",
			"Sample description",
			"Sample sampling date",
			"Sample position",
			"Sample state",
			"Sample knots",
			"Radius title",
			"Radius identifier",
			"Radius created timestamp",
			"Radius updated timestamp",
			"Radius comments",
			"Radius ring count",
			"Radius average ring width",
			"Radius nr unmeasured inner rings",
			"Radius nr unmeasured outer rings",
			"Radius pith",
			"Radius heartwood presence",
			"Radius heartwood missing rings to pith",
			"Radius heartwood missing rings to pith foundation",
			"Radius sapwood presence",
			"Radius sapwood nr sapwood rings",
			"Radius sapwood last ring under bark presence",
			"Radius sapwood last ring under bark content",
			"Radius sapwood missing sapwood rings to bark",
			"Radius sapwood missing sapwood rings to bark foundation",
			"Radius bark",
			"Radius azimuth",
			"Series title",
			"Series identifier",
			"Series created timestamp",
			"Series updated timestamp",
			"Series comments",
			"Series measuring date",
			"Series analyst",
			"Series dendrochronologist",
			"Series measuring method",
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
			int index=1;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.PROJECT_IDENTIFIER).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.PROJECT_CREATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.PROJECT_UPDATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.PROJECT_COMMENTS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.PROJECT_TYPE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.PROJECT_DESCRIPTION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.PROJECT_CATEGORY).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.PROJECT_INVESTIGATOR).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.PROJECT_PERIOD).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.PROJECT_REQUEST_DATE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.PROJECT_COMMISSIONER).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_TITLE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_IDENTIFIER).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.OBJECT_CREATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.OBJECT_UPDATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_COMMENTS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_TYPE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_DESCRIPTION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.OBJECT_LOCATION_LAT).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.OBJECT_LOCATION_LON).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_TYPE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_PRECISION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_COMMENT).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_CITYTOWN).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_STATE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_COUNTRY).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_TITLE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_IDENTIFIER).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.ELEMENT_CREATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.ELEMENT_UPDATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_COMMENTS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_TYPE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_DESCRIPTION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_TAXON).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_SHAPE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_UNIT).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_HEIGHT).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_WIDTH).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DEPTH).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DIAMETER).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_AUTHENTICITY).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_LOCATION_LAT).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_LOCATION_LON).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCAITON_TYPE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_PRECISION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_COMMENT).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_CITYTOWN).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_STATE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_COUNTRY).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_PROCESSING).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_MARKS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_ELEVATION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getIntegerDefaultValue(DefaultFields.ELEMENT_SLOPE_ANGLE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getIntegerDefaultValue(DefaultFields.ELEMENT_SLOPE_AZIMUTH).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_SOIL_DEPTH).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_SOIL_DESCRIPTION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.ELEMENT_BEDROCK).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SAMPLE_TITLE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SAMPLE_IDENTIFIER).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.SAMPLE_CREATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.SAMPLE_UPDATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SAMPLE_COMMENTS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SAMPLE_TYPE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SAMPLE_DESCRIPTION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SAMPLE_SAMPLING_DATE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SAMPLE_POSITION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SAMPLE_STATE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getBooleanDefaultValue(DefaultFields.SAMPLE_KNOTS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_TITLE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_IDENTIFIER).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.RADIUS_CREATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.RADIUS_UPDATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_COMMENTS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_RING_COUNT).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.RADIUS_WC_AVERAGE_RING_WIDTH).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_NR_UNMEASURED_INNER_RINGS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_NR_UNMEASURED_OUTER_RINGS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_PITH).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_PRESENCE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH_FOUNDATION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_PRESENCE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_NR_SAPWOOD_RINGS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_LAST_RING_UNDER_BARK_PRESENCE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK_FOUNDATION).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_BARK).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDoubleDefaultValue(DefaultFields.RADIUS_AZIMUTH).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SERIES_TITLE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SERIES_IDENTIFIER).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.SERIES_CREATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getDateTimeDefaultValue(DefaultFields.SERIES_UPDATED_TIMESTAMP).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SERIES_COMMENTS).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SERIES_MEASURING_DATE).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SERIES_ANALYST).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SERIES_DENDROCHRONOLOGIST).getStringValue(), index);  index++;
			addValueToColumn(valuecolumns, s.def.getStringDefaultValue(DefaultFields.SERIES_MEASURING_METHOD).getStringValue(), index);  index++;


			
			matrix.add(valuecolumns);
		}
		
		
		return matrix;
	}
	
	
	private static void addValueToColumn(String[] matrix, String value, int index)
	{
		
		if(value==null)
		{
			matrix[index] = "NULL";
		}
		else
		{
			matrix[index] = value;
		}
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
