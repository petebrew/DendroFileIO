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
