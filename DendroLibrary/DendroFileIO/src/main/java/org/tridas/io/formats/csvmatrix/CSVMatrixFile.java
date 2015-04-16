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
			
			valuecolumns[1]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_TITLE).getStringValue();
			valuecolumns[2]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_IDENTIFIER).getStringValue();
			valuecolumns[3]  = s.def.getDateTimeDefaultValue(DefaultFields.PROJECT_CREATED_TIMESTAMP).getStringValue();
			valuecolumns[4]  = s.def.getDateTimeDefaultValue(DefaultFields.PROJECT_UPDATED_TIMESTAMP).getStringValue();				
			valuecolumns[5]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_COMMENTS).getStringValue();				
			valuecolumns[6]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_TYPE).getStringValue();
			valuecolumns[7]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_DESCRIPTION).getStringValue();
			valuecolumns[8]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_CATEGORY).getStringValue();
			valuecolumns[9]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_INVESTIGATOR).getStringValue();
			valuecolumns[10]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_PERIOD).getStringValue();
			valuecolumns[11]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_REQUEST_DATE).getStringValue();
			valuecolumns[12]  = s.def.getStringDefaultValue(DefaultFields.PROJECT_COMMISSIONER).getStringValue();
			valuecolumns[13]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_TITLE).getStringValue();
			valuecolumns[14]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_IDENTIFIER).getStringValue();
			valuecolumns[15]  = s.def.getDateTimeDefaultValue(DefaultFields.OBJECT_CREATED_TIMESTAMP).getStringValue();
			valuecolumns[16]  = s.def.getDateTimeDefaultValue(DefaultFields.OBJECT_UPDATED_TIMESTAMP).getStringValue();				
			valuecolumns[17]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_COMMENTS).getStringValue();				
			valuecolumns[18]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_TYPE).getStringValue();
			valuecolumns[19]  = s.def.getStringDefaultValue(DefaultFields.OBJECT_DESCRIPTION).getStringValue();
			valuecolumns[20]  = s.def.getDoubleDefaultValue(DefaultFields.OBJECT_LOCATION_LAT).getStringValue();
			valuecolumns[21]  = s.def.getDoubleDefaultValue(DefaultFields.OBJECT_LOCATION_LON).getStringValue();
			valuecolumns[22] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_TYPE).getStringValue();
			valuecolumns[23] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_PRECISION).getStringValue();
			valuecolumns[24] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_COMMENT).getStringValue();
			valuecolumns[25] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_CITYTOWN).getStringValue();
			valuecolumns[26] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_STATE).getStringValue();
			valuecolumns[27] = s.def.getStringDefaultValue(DefaultFields.OBJECT_LOCATION_COUNTRY).getStringValue();
			valuecolumns[28] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_TITLE).getStringValue();
			valuecolumns[29] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_IDENTIFIER).getStringValue();
			valuecolumns[30] = s.def.getDateTimeDefaultValue(DefaultFields.ELEMENT_CREATED_TIMESTAMP).getStringValue();
			valuecolumns[31] = s.def.getDateTimeDefaultValue(DefaultFields.ELEMENT_UPDATED_TIMESTAMP).getStringValue();
			valuecolumns[32] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_COMMENTS).getStringValue();
			valuecolumns[33] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_TYPE).getStringValue();
			valuecolumns[34] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_DESCRIPTION).getStringValue();
			valuecolumns[35] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_TAXON).getStringValue();
			valuecolumns[36] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_SHAPE).getStringValue();
			valuecolumns[37] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_UNIT).getStringValue();
			valuecolumns[38] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_HEIGHT).getStringValue();
			valuecolumns[39] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_WIDTH).getStringValue();
			valuecolumns[40] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DEPTH).getStringValue();
			valuecolumns[41] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_DIMENSIONS_DIAMETER).getStringValue();
			valuecolumns[42] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_AUTHENTICITY).getStringValue();
			valuecolumns[43] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_LOCATION_LAT).getStringValue();
			valuecolumns[44] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_LOCATION_LON).getStringValue();
			valuecolumns[45] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCAITON_TYPE).getStringValue();
			valuecolumns[46] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_PRECISION).getStringValue();
			valuecolumns[47] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_COMMENT).getStringValue();
			valuecolumns[48] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_CITYTOWN).getStringValue();
			valuecolumns[49] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_STATE).getStringValue();
			valuecolumns[50] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_LOCATION_COUNTRY).getStringValue();
			valuecolumns[51] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_PROCESSING).getStringValue();
			valuecolumns[52] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_MARKS).getStringValue();
			valuecolumns[53] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_ELEVATION).getStringValue();
			valuecolumns[54] = s.def.getIntegerDefaultValue(DefaultFields.ELEMENT_SLOPE_ANGLE).getStringValue();
			valuecolumns[55] = s.def.getIntegerDefaultValue(DefaultFields.ELEMENT_SLOPE_AZIMUTH).getStringValue();
			valuecolumns[56] = s.def.getDoubleDefaultValue(DefaultFields.ELEMENT_SOIL_DEPTH).getStringValue();
			valuecolumns[57] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_SOIL_DESCRIPTION).getStringValue();
			valuecolumns[58] = s.def.getStringDefaultValue(DefaultFields.ELEMENT_BEDROCK).getStringValue();
			valuecolumns[59] = s.def.getStringDefaultValue(DefaultFields.SAMPLE_TITLE).getStringValue();
			valuecolumns[60] = s.def.getStringDefaultValue(DefaultFields.SAMPLE_IDENTIFIER).getStringValue();
			valuecolumns[61] = s.def.getDateTimeDefaultValue(DefaultFields.SAMPLE_CREATED_TIMESTAMP).getStringValue();
			valuecolumns[62] = s.def.getDateTimeDefaultValue(DefaultFields.SAMPLE_UPDATED_TIMESTAMP).getStringValue();
			valuecolumns[63] = s.def.getStringDefaultValue(DefaultFields.SAMPLE_COMMENTS).getStringValue();
			valuecolumns[64] = s.def.getStringDefaultValue(DefaultFields.SAMPLE_TYPE).getStringValue();
			valuecolumns[65] = s.def.getStringDefaultValue(DefaultFields.SAMPLE_DESCRIPTION).getStringValue();
			valuecolumns[66] = s.def.getStringDefaultValue(DefaultFields.SAMPLE_SAMPLING_DATE).getStringValue();
			valuecolumns[67] = s.def.getStringDefaultValue(DefaultFields.SAMPLE_POSITION).getStringValue();
			valuecolumns[68] = s.def.getStringDefaultValue(DefaultFields.SAMPLE_STATE).getStringValue();
			valuecolumns[69] = s.def.getBooleanDefaultValue(DefaultFields.SAMPLE_KNOTS).getStringValue();
			valuecolumns[70] = s.def.getStringDefaultValue(DefaultFields.RADIUS_TITLE).getStringValue();
			valuecolumns[71] = s.def.getStringDefaultValue(DefaultFields.RADIUS_IDENTIFIER).getStringValue();
			valuecolumns[72] = s.def.getDateTimeDefaultValue(DefaultFields.RADIUS_CREATED_TIMESTAMP).getStringValue();
			valuecolumns[73] = s.def.getDateTimeDefaultValue(DefaultFields.RADIUS_UPDATED_TIMESTAMP).getStringValue();
			valuecolumns[74] = s.def.getStringDefaultValue(DefaultFields.RADIUS_COMMENTS).getStringValue();
			valuecolumns[75] = s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_RING_COUNT).getStringValue();
			valuecolumns[76] = s.def.getDoubleDefaultValue(DefaultFields.RADIUS_WC_AVERAGE_RING_WIDTH).getStringValue();
			valuecolumns[77] = s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_NR_UNMEASURED_INNER_RINGS).getStringValue();
			valuecolumns[78] = s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_NR_UNMEASURED_OUTER_RINGS).getStringValue();
			valuecolumns[79] = s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_PITH).getStringValue();
			valuecolumns[80] = s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_PRESENCE).getStringValue();
			valuecolumns[81] = s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH).getStringValue();
			valuecolumns[82] = s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_HEARTWOOD_MISSING_RINGS_TO_PITH_FOUNDATION).getStringValue();
			valuecolumns[83] = s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_PRESENCE).getStringValue();
			valuecolumns[84] = s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_NR_SAPWOOD_RINGS).getStringValue();
			valuecolumns[85] = s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_LAST_RING_UNDER_BARK_PRESENCE).getStringValue();
			valuecolumns[86] = s.def.getIntegerDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK).getStringValue();
			valuecolumns[87] = s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_SAPWOOD_MISSING_SAPWOOD_RINGS_TO_BARK_FOUNDATION).getStringValue();
			valuecolumns[88] = s.def.getStringDefaultValue(DefaultFields.RADIUS_WC_BARK).getStringValue();
			valuecolumns[89] = s.def.getDoubleDefaultValue(DefaultFields.RADIUS_AZIMUTH).getStringValue();
			valuecolumns[90] = s.def.getStringDefaultValue(DefaultFields.SERIES_TITLE).getStringValue();
			valuecolumns[91] = s.def.getStringDefaultValue(DefaultFields.SERIES_IDENTIFIER).getStringValue();
			valuecolumns[92] = s.def.getDateTimeDefaultValue(DefaultFields.SERIES_CREATED_TIMESTAMP).getStringValue();
			valuecolumns[93] = s.def.getDateTimeDefaultValue(DefaultFields.SERIES_UPDATED_TIMESTAMP).getStringValue();
			valuecolumns[94] = s.def.getStringDefaultValue(DefaultFields.SERIES_COMMENTS).getStringValue();
			valuecolumns[95] = s.def.getStringDefaultValue(DefaultFields.SERIES_MEASURING_DATE).getStringValue();
			valuecolumns[96] = s.def.getStringDefaultValue(DefaultFields.SERIES_ANALYST).getStringValue();
			valuecolumns[97] = s.def.getStringDefaultValue(DefaultFields.SERIES_DENDROCHRONOLOGIST).getStringValue();
			valuecolumns[98] = s.def.getStringDefaultValue(DefaultFields.SERIES_MEASURING_METHOD).getStringValue();


			
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
