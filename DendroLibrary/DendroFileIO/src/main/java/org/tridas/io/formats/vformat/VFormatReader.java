package org.tridas.io.formats.vformat;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;

public class VFormatReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(VFormatReader.class);
	// defaults given by user
	private VFormatToTridasDefaults defaults = new VFormatToTridasDefaults();
	private TridasProject project = null;
	
	private ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();

	
	enum ParamMeasured{
		MEAN_DENSITY("D"),
		EARLYWOOD_WIDTH("F"),
		MAX_DENSITY("G"),
		RING_WIDTH("J"),
		MIN_DENSITY("K");
		
	    private String code;   
		ParamMeasured(String code) {
	        this.code = code;
	    }

		
	}
	
	enum VFormatLineType{
		HEADER_1,
		HEADER_2,
		DATA,
		INVALID;
	}
	
	public VFormatReader() {
		super(VFormatToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		// Sanity checks
		if(argFileString[0].length()!=80)
		{
			throw new InvalidDendroFileException(I18n.getText("vformat.headerWrongSize", 
					String.valueOf(argFileString[0].length())), 
					1);
		}
		
		if(!argFileString[0].substring(8, 9).equals("."))
		{
			throw new InvalidDendroFileException(I18n.getText("vformat.missingDot"), 1);			
		}
		
		if(!this.getLineType(argFileString[0]).equals(VFormatLineType.HEADER_1))
		{
			throw new InvalidDendroFileException(I18n.getText("vformat.headerLineWrong"), 1);
		}
		
		VFormatLineType lastLineType = VFormatLineType.DATA;
		ITridasSeries thisseries = null;
		int linenumber=0;
		TridasUnit units = new TridasUnit();
		TridasVariable var = new TridasVariable();
		for(String line : argFileString)
		{	
			linenumber++;
			switch(getLineType(line))
			{
			case HEADER_1:			
				// Last line should have been data otherwise something has gone wrong
				if (!lastLineType.equals(VFormatLineType.DATA)) throw new InvalidDendroFileException(I18n.getText("vformat.invalidLine"), linenumber);
			
				// If thisseries is not null then we should add it to our list
				// as we're just about to start another
				if(thisseries!=null) seriesList.add(thisseries);			
								
				// Check whether this is a supported version format
				// TODO 
				// Versions >=10 have additional lines for which I don't have the specs
				// or examples
				try{
					Integer fileVersionNumber = Integer.valueOf(line.substring(68,70));
					if(fileVersionNumber.compareTo(10)>=0)
					{
						throw new InvalidDendroFileException(I18n.getText("vformat.unsupportedFormat", 
								String.valueOf(fileVersionNumber)), 
								linenumber);
					}
				} catch (NumberFormatException e){}
				
				// Reset 
				units = new TridasUnit();
				ArrayList<TridasGenericField> genericFields = new ArrayList<TridasGenericField>();
				var = new TridasVariable();
				
				// Create new series depending on type and set fields that are specific
				// to that type.
				String typeCode = line.substring(9, 10);
				if(typeCode.equals("!"))
				{
					thisseries = defaults.getMeasurementSeriesWithDefaults();
					
					// Set Analyst
					((TridasMeasurementSeries)thisseries).setAnalyst(line.substring(58,60));
					
					// Set missing rings fields
					TridasWoodCompleteness wc = new TridasWoodCompleteness();
					try{
						Integer missingInnerRings = Integer.valueOf((line.substring(70,73)));	
						wc.setNrOfUnmeasuredInnerRings(missingInnerRings);					
					} catch (NumberFormatException e){}
					
					try{
						Integer missingOuterRings = Integer.valueOf((line.substring(75,78)));			
						wc.setNrOfUnmeasuredOuterRings(missingOuterRings);						
					} catch (NumberFormatException e){}
					
					// Add woodcompleteness to series
					((TridasMeasurementSeries)thisseries).setWoodCompleteness(wc);
					
					try{
						String stdErrInnerRings = line.substring(73,75);			
						if(stdErrInnerRings != null && !stdErrInnerRings.equals("."))
						{
							TridasGenericField gf = new TridasGenericField();
							gf.setName("vformat.stdErrMissingInnerRings");
							gf.setValue(stdErrInnerRings);
							gf.setType("String");
							genericFields.add(gf);
						}
					} catch (NumberFormatException e){}
					
					try{
						String stdErrOuterRings = line.substring(78,80).trim();			
						if(stdErrOuterRings!=null && !stdErrOuterRings.equals("."))
						{
							TridasGenericField gf = new TridasGenericField();
							gf.setName("vformat.stdErrMissingOuterRings");
							gf.setValue(stdErrOuterRings);
							gf.setType("String");
							genericFields.add(gf);
						}
					} catch (NumberFormatException e){}

					// Add genericFields list to series
					((TridasMeasurementSeries)thisseries).setGenericFields(genericFields);
				}
				else
				{
					thisseries = defaults.getDerivedSeriesWithDefaults();
				}
				
				// Set series identifier and title
				TridasIdentifier id = new TridasIdentifier();
				id.setValue(line.substring(0, 12));
				TridasMetadataFieldSet df = new TridasMetadataFieldSet();
				id.setDomain(defaults.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
				thisseries.setIdentifier(id);
				thisseries.setTitle(line.substring(0, 12));
								
				// Set variable type
				String paramMeasured = line.substring(11, 12);
				if(paramMeasured.equals("J"))
				{
					var.setNormalTridas(NormalTridasVariable.RING_WIDTH);
				}
				else if (paramMeasured.equals("F"))
				{
					var.setNormalTridas(NormalTridasVariable.EARLYWOOD_WIDTH);
				}
				else if (paramMeasured.equals("S"))
				{
					var.setNormalTridas(NormalTridasVariable.LATEWOOD_WIDTH);
				}				
				else if (paramMeasured.equals("D"))
				{
					var.setNormalTridas(NormalTridasVariable.RING_DENSITY);
				}
				else if (paramMeasured.equals("G"))
				{
					var.setNormalTridas(NormalTridasVariable.MAXIMUM_DENSITY);
				}
				else if (paramMeasured.equals("K"))
				{
					var.setValue("Minimum density");
				}
				else if (paramMeasured.equals("P"))
				{
					var.setValue("Portion of latewood (%)");
				}
				else
				{
					throw new InvalidDendroFileException(I18n.getText("fileio.unknownError"), linenumber);	
				}			
				
				
				// Set last year
				TridasInterpretation interp= new TridasInterpretation();
				try{
					Integer lastYear = Integer.valueOf(line.substring(24, 30));
					if (lastYear!=null) interp.setLastYear(new SafeIntYear(lastYear).toTridasYear(DatingSuffix.AD));
					thisseries.setInterpretation(interp);
				} catch (NumberFormatException e){}
					
				// Set created timestamp
				try{
					int day = Integer.valueOf(line.substring(50,52));
					int month = Integer.valueOf(line.substring(52,54));
					int year = Integer.valueOf(line.substring(54,58));
					thisseries.setCreatedTimestamp(DateUtils.getDateTime(day, month, year));
				} catch (NumberFormatException e){ }
				
				// Set analysts/author
				if(thisseries instanceof TridasMeasurementSeries)
				{
					
				}
				else if (thisseries instanceof TridasDerivedSeries)
				{
					((TridasDerivedSeries)thisseries).setAuthor(line.substring(58,60));
				}
	
				// Set comments/description
				thisseries.setComments(line.substring(30,50).trim());
						
				// Set units variable for use in <values> tag
				String unitsCode = line.substring(12,15).trim();
				if(!unitsCode.equals("mm"))
				{
					units.setValue(unitsCode);
				}
				else
				{
					units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
				}
							
				// Set this line type as the last linetype before continuing
				lastLineType=VFormatLineType.HEADER_1;
				break;
			
			case HEADER_2:
				// Last line should have been header1 otherwise something has gone wrong
				if (!lastLineType.equals(VFormatLineType.HEADER_1)) throw new InvalidDendroFileException(I18n.getText("vformat.invalidLine"), linenumber);
			
				try{
					ArrayList<TridasGenericField> gflist = (ArrayList<TridasGenericField>) thisseries.getGenericFields();
					TridasGenericField gf = new TridasGenericField();
					gf.setName("vformat.freeTextHeaderLine");
					gf.setValue(line.trim());
					gf.setType("String");
					gflist.add(gf);
				} catch (NullPointerException e){}	
				
				// Set this line type as the last linetype before continuing
				lastLineType=VFormatLineType.HEADER_2;
				break;
				
			case DATA:
				// Last line should have been header2 or data otherwise something has gone wrong
				if (lastLineType.equals(VFormatLineType.HEADER_1)) throw new InvalidDendroFileException(I18n.getText("vformat.invalidLine"), linenumber);
			
				// Get his decades values
				ArrayList<TridasValue> thisDecadesValues = new ArrayList<TridasValue>();						
				for (int i=1; i<line.length(); i=i+8)
				{
					/*String validity = line.substring(i-1,i).trim();
					String importance = line.substring(i, i+1).trim();
					String remark = line.substring(i+1, i+2).trim();*/
					
					TridasValue theValue = new TridasValue();
					theValue.setValue(line.substring(i+2, i+7).trim());
					thisDecadesValues.add(theValue);		
				}
				
				
				if(!lastLineType.equals(VFormatLineType.DATA))
				{
					// Must be the first batch of data so set up <values> container
					TridasValues valuesGroup = new TridasValues();
					valuesGroup.setValues(thisDecadesValues);
					valuesGroup.setUnit(units);
					valuesGroup.setVariable(var);
					ArrayList<TridasValues> valuesGroupList = new ArrayList<TridasValues>();
					valuesGroupList.add(valuesGroup);	
					thisseries.setValues(valuesGroupList);
				}
				else
				{
					// Adding to existing ring values
					thisseries.getValues().get(0).getValues().addAll(thisDecadesValues);
				}
				
				
				// Set this line type as the last linetype before continuing
				lastLineType=VFormatLineType.DATA;
				break;
			
			default:
				throw new InvalidDendroFileException(I18n.getText("vformat.invalidLine"), linenumber);
			}
		}

	}
	
	/**
	 * Attempt to work out what sort of line this is
	 * 
	 * @param line
	 * @return
	 */
	private VFormatLineType getLineType(String line)
	{
		String regex = null;
		Pattern p1;
		Matcher m1;
		
		// If line is empty or is not the right length return invalid now
		if (line==null) return VFormatLineType.INVALID;
		if(line.length()>80) return VFormatLineType.INVALID;
		
		// Data line
		regex = "^([ !\"#$%&\']{3}[ \\d.]{4}[\\d]{1}){1,10}";
	    p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    m1 = p1.matcher(line);
		if(m1.find()) return VFormatLineType.DATA;
		
		// Header line 1
		regex = "^[\\S\\s]{8}.[!%#][FIMOPQRSTWXZ][DFGJKPS][\\S\\s]{12}[\\d\\s]{6}[\\S\\s]{20}[\\d\\s]{8}[\\S\\s]{10}[\\d}]{2}[\\d\\s.]{10}";
	    p1 = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    m1 = p1.matcher(line);
		if(m1.find()) return VFormatLineType.HEADER_1;
		
		else return VFormatLineType.HEADER_2;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getFileExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TridasProject getProject() {
		
		try{
		project = defaults.getProjectWithDefaults(true); 
		TridasObject o = project.getObjects().get(0);
		TridasElement e = o.getElements().get(0);
		TridasSample s = e.getSamples().get(0);
		
		if(seriesList.size()>0)
		{
			ArrayList<TridasMeasurementSeries> mSeriesList;
			
			for (ITridasSeries series : seriesList)
			{
				if(series instanceof TridasMeasurementSeries)
				{
					TridasRadius r = s.getRadiuses().get(0);
					mSeriesList = (ArrayList<TridasMeasurementSeries>) r.getMeasurementSeries();
					mSeriesList.add((TridasMeasurementSeries) series);
				}
				else if (series instanceof TridasDerivedSeries)
				{
					// TODO
				}
			}
			
			

		}
		

		
		} catch (NullPointerException e){
			
		} catch (IndexOutOfBoundsException e2){
			
		}
		
		
		return project;
	}

}
