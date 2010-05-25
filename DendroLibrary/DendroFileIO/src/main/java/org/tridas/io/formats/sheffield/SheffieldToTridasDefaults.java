package org.tridas.io.formats.sheffield;

import java.math.BigDecimal;
import java.util.ArrayList;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;

import org.apache.commons.lang.WordUtils;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldChronologyType;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldDataType;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldEdgeCode;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldPeriodCode;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldShapeCode;
import org.tridas.io.formats.sheffield.TridasToSheffieldDefaults.SheffieldVariableCode;
import org.tridas.io.util.CoordinatesUtils;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasShape;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasCoverage;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasDimensions;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasLastRingUnderBark;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasShape;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;
import org.tridas.schema.Year;

public class SheffieldToTridasDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {

	public static enum DefaultFields{
		OBJECT_NAME,
		RING_COUNT,
		START_YEAR,
		SERIES_TITLE,
		SERIES_COMMENT,
		UK_COORDS,			  // GENERICFIELD
		LATITUDE,
		LONGITUDE,
		PITH,
		PITH_DESCRIPTION,     // GENERICFIELD
		SHEFFIELD_DATA_TYPE,  // GENERICFIELD
		SAPWOOD_COUNT,
		SHEFFIELD_SHAPE_CODE,
		MAJOR_DIM,
		MINOR_DIM,
		UNMEAS_INNER_RINGS,
		UNMEAS_OUTER_RINGS,
		GROUP_PHASE,
		SHEFFIELD_PERIOD_CODE,
		TAXON_CODE,
		INTERPRETATION_NOTES,
		SHEFFIELD_VARIABLE_TYPE,
		SHEFFIELD_EDGE_CODE,
		SHEFFIELD_CHRONOLOGY_TYPE;
		
	}
	
	public void initDefaultValues(){
		super.initDefaultValues();
		setDefaultValue(DefaultFields.OBJECT_NAME, new StringDefaultValue(I18n.getText("unnamed.object")));
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.START_YEAR, new GenericDefaultValue<SafeIntYear>());
		setDefaultValue(DefaultFields.SERIES_TITLE, new StringDefaultValue(I18n.getText("unnamed.series")));
		setDefaultValue(DefaultFields.SERIES_COMMENT, new StringDefaultValue());
		setDefaultValue(DefaultFields.UK_COORDS, new StringDefaultValue());
		setDefaultValue(DefaultFields.LATITUDE, new DoubleDefaultValue(-90.0, 90.0));
		setDefaultValue(DefaultFields.LONGITUDE, new DoubleDefaultValue(-180.0, 180.0));
		setDefaultValue(DefaultFields.PITH, new GenericDefaultValue<ComplexPresenceAbsence>());
		setDefaultValue(DefaultFields.PITH_DESCRIPTION, new StringDefaultValue());
		setDefaultValue(DefaultFields.SHEFFIELD_DATA_TYPE, new GenericDefaultValue<SheffieldDataType>());
		setDefaultValue(DefaultFields.SAPWOOD_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SHEFFIELD_SHAPE_CODE, new GenericDefaultValue<SheffieldShapeCode>());
		setDefaultValue(DefaultFields.MAJOR_DIM, new DoubleDefaultValue(0.0, Double.MAX_VALUE));
		setDefaultValue(DefaultFields.MINOR_DIM, new DoubleDefaultValue(0.0, Double.MAX_VALUE));
		setDefaultValue(DefaultFields.UNMEAS_INNER_RINGS, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_OUTER_RINGS, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.GROUP_PHASE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SHEFFIELD_PERIOD_CODE, new GenericDefaultValue<SheffieldPeriodCode>());
		setDefaultValue(DefaultFields.TAXON_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.INTERPRETATION_NOTES, new StringDefaultValue());
		setDefaultValue(DefaultFields.SHEFFIELD_VARIABLE_TYPE, new GenericDefaultValue<SheffieldVariableCode>());
		setDefaultValue(DefaultFields.SHEFFIELD_EDGE_CODE, new GenericDefaultValue<SheffieldEdgeCode>());
		setDefaultValue(DefaultFields.SHEFFIELD_CHRONOLOGY_TYPE, new GenericDefaultValue<SheffieldChronologyType>());
	}

	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasObject()
	 */
	@Override
	protected TridasObject getDefaultTridasObject() {
		TridasObject o = super.getDefaultTridasObject();
		
		o.setTitle(getStringDefaultValue(DefaultFields.OBJECT_NAME).getStringValue());
		
		// If Lat Long is available use it
		if(getDefaultValue(DefaultFields.LATITUDE).getValue()!=null && 
		   getDefaultValue(DefaultFields.LONGITUDE).getValue()!=null)
		{
			TridasLocationGeometry geometry = CoordinatesUtils.getLocationGeometry(
					getDoubleDefaultValue(DefaultFields.LATITUDE).getValue(),
					getDoubleDefaultValue(DefaultFields.LONGITUDE).getValue());
			TridasLocation location = new TridasLocation();
			location.setLocationGeometry(geometry);
			o.setLocation(location);
		}
		
		// Add UK Coords as a generic field
		if(getStringDefaultValue(DefaultFields.UK_COORDS).getValue()!=null)
		{
			TridasGenericField coords = new TridasGenericField();
			coords.setName("sheffield.UKCoords");
			coords.setType("xs:string");
			coords.setValue(getStringDefaultValue(DefaultFields.UK_COORDS).getValue());
			ArrayList<TridasGenericField> genericFields = new ArrayList<TridasGenericField>();
			genericFields.add(coords);
			o.setGenericFields(genericFields);
		}
		
		// Add group/phase as subobject
		if(getStringDefaultValue(DefaultFields.GROUP_PHASE).getValue()!=null)
		{
			TridasObject subobj = super.getDefaultTridasObject();
			subobj.setTitle(getStringDefaultValue(DefaultFields.GROUP_PHASE).getValue());
			ControlledVoc type = new ControlledVoc();
			type.setValue(I18n.getText("sheffield.groupOrPhase"));
			subobj.setType(type);
			ArrayList<TridasObject> objects = new ArrayList<TridasObject>();
			objects.add(subobj);
			o.setObjects(objects);
		}
		
		// Temporal coverage
		try{
			GenericDefaultValue<SheffieldPeriodCode> periodField = (GenericDefaultValue<SheffieldPeriodCode>) getDefaultValue(DefaultFields.SHEFFIELD_PERIOD_CODE); 		
			SheffieldPeriodCode value = periodField.getValue();
			String sheffieldPeriod = value.toString();
			TridasCoverage coverage = new TridasCoverage();
			coverage.setCoverageTemporal(WordUtils.capitalize(sheffieldPeriod.toLowerCase()));
			coverage.setCoverageTemporalFoundation(I18n.getText("unknown"));
			o.setCoverage(coverage);
		} catch (NullPointerException e1){}	
			
		return o;
	}

	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
		
		// Set Element shape
		SheffieldShapeCode sheffieldShape = null;
		TridasShape tridasShape = new TridasShape();		
		try{
			GenericDefaultValue<SheffieldShapeCode> shapeField = (GenericDefaultValue<SheffieldShapeCode>) getDefaultValue(DefaultFields.SHEFFIELD_SHAPE_CODE); 		
			sheffieldShape = shapeField.getValue();

			switch(sheffieldShape)
			{
			case WHOLE_ROUND_UNTRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.WHOLE___SECTION);
				break;
			case WHOLE_ROUND_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.SQUARED___BEAM___FROM___WHOLE___SECTION);
				break;
			case WHOLE_ROUND_IRREGULARLY_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.BEAM___STRAIGHTENED___ON___ONE___SIDE);
				break;
			case HALF_ROUND_UNTRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.HALF___SECTION);
				break;
			case HALF_ROUND_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.SQUARED___BEAM___FROM___HALF___SECTION);
				break;
			case HALF_ROUND_IRREGULARLY_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.SQUARED___BEAM___FROM___HALF___SECTION);
				break;
			case QUARTERED_UNTRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.QUARTER___SECTION);
				break;
			case QUARTERED_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.SQUARED___BEAM___FROM___QUARTER___SECTION);
				break;
			case QUARTERED_IRREGULARLY_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.SMALL___PART___OF___SECTION);
				break;
			case RADIAL_PLANK_UNTRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.PLANK___CUT___ON___ONE___SIDE);
				break;
			case RADIAL_PLANK_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.RADIAL___PLANK___THROUGH___PITH);
				break;
			case RADIAL_PLANK_IRREGULARLY_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.RADIAL___PLANK___UP___TO___PITH);
				break;
			case TANGENTIAL_PLANK_UNTRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.TANGENTIAL___PLANK___NOT___INCLUDING___PITH___WITH___BREADTH___LARGER___THAN___A___QUARTER___SECTION);
				break;
			case TANGENTIAL_PLANK_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.PLANK___NOT___INCLUDING___PITH___WITH___BREADTH___SMALLER___THAN___A___QUARTER___SECTION);
				break;
			case TANGENTIAL_PLANK_IRREGULARLY_TRIMMED:
				tridasShape.setNormalTridas(NormalTridasShape.PLANK___NOT___INCLUDING___PITH___WITH___BREADTH___SMALLER___THAN___A___QUARTER___SECTION);
				break;
			case UNKNOWN:
				tridasShape.setNormalTridas(NormalTridasShape.UNKNOWN);
				break;
			case CORE_UNCLASSIFIABLE:
				tridasShape.setNormalTridas(NormalTridasShape.UNKNOWN);
				break;
			default:
				break;
			}
			e.setShape(tridasShape);
			
		} catch (NullPointerException e1){}		
		
		// Set element dimensions
		if(  getDoubleDefaultValue(DefaultFields.MAJOR_DIM).getValue()!=null &&
			!getDoubleDefaultValue(DefaultFields.MAJOR_DIM).getValue().equals(0.0) &&
			 getDoubleDefaultValue(DefaultFields.MINOR_DIM).getValue()!=null &&
			!getDoubleDefaultValue(DefaultFields.MINOR_DIM).getValue().equals(0.0))
		{
			TridasDimensions dims = new TridasDimensions();
			dims.setHeight(BigDecimal.valueOf(getDoubleDefaultValue(DefaultFields.MAJOR_DIM).getValue()));
			dims.setWidth(BigDecimal.valueOf(getDoubleDefaultValue(DefaultFields.MINOR_DIM).getValue()));
			dims.setDepth(BigDecimal.valueOf(getDoubleDefaultValue(DefaultFields.MINOR_DIM).getValue()));
			TridasUnit units = new TridasUnit();
			units.setNormalTridas(NormalTridasUnit.MILLIMETRES);
			dims.setUnit(units);
			e.setDimensions(dims);
		}
		
		// Taxon
		if(getStringDefaultValue(DefaultFields.TAXON_CODE).getValue()!=null)
		{
			ControlledVoc taxon = ITRDBTaxonConverter.getControlledVocFromCode(getStringDefaultValue(DefaultFields.TAXON_CODE).getValue());
			e.setTaxon(taxon);
		}
			
		return e;
	}
	
	protected TridasSample getDefaultTridasSample()
	{
		TridasSample sample = super.getDefaultTridasSample();
		return sample;
	}
	
	protected TridasRadius getDefaultTridasRadius()
	{
		TridasRadius r = super.getDefaultTridasRadius();
		return r;
	}
		
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries ms = super.getDefaultTridasMeasurementSeries();
		TridasWoodCompleteness wc = super.getDefaultWoodCompleteness();
		ms.setTitle(getStringDefaultValue(DefaultFields.SERIES_TITLE).getStringValue());
		ms.setComments(getStringDefaultValue(DefaultFields.SERIES_COMMENT).getStringValue());
		
		// Start year info
		try{
			GenericDefaultValue<SafeIntYear> startYearField = (GenericDefaultValue<SafeIntYear>) getDefaultValue(DefaultFields.START_YEAR); 		
			ms.getInterpretation().setFirstYear(startYearField.getValue().toTridasYear(DatingSuffix.AD));
		} catch (NullPointerException e){}
		
		
		// Set pith info
		if(getDefaultValue(DefaultFields.PITH).getValue()!=null)
		{
			wc.getPith().setPresence(((ComplexPresenceAbsence)getDefaultValue(DefaultFields.PITH).getValue()));
			Integer sapwoodRings = ((Integer)getDefaultValue(DefaultFields.SAPWOOD_COUNT).getValue());
			wc.getSapwood().setNrOfSapwoodRings(sapwoodRings);
			
		}

		// Get any generic fields 
		if(getMeasurementSeriesGenericFields().size()>0)
		{
			ms.setGenericFields(getMeasurementSeriesGenericFields());
		}
		
		
		// Unmeasured inner rings
		if(getIntegerDefaultValue(DefaultFields.UNMEAS_INNER_RINGS).getValue()!=null)
		{
			wc.setNrOfUnmeasuredInnerRings(getIntegerDefaultValue(DefaultFields.UNMEAS_INNER_RINGS).getValue());
		}
		
		// Unmeasured outer rings
		if(getIntegerDefaultValue(DefaultFields.UNMEAS_OUTER_RINGS).getValue()!=null)
		{
			wc.setNrOfUnmeasuredOuterRings(getIntegerDefaultValue(DefaultFields.UNMEAS_OUTER_RINGS).getValue());
		}
		
		try{
			GenericDefaultValue<SheffieldEdgeCode> edgeCodeField = (GenericDefaultValue<SheffieldEdgeCode>) getDefaultValue(DefaultFields.SHEFFIELD_EDGE_CODE); 		
			TridasLastRingUnderBark lrub;
			switch(edgeCodeField.getValue())
			{
			case BARK:
				TridasBark bark = new TridasBark();
				bark.setPresence(PresenceAbsence.PRESENT);
				wc.setBark(bark);
				break;
			case WINTER:
				wc.getSapwood().setPresence(ComplexPresenceAbsence.COMPLETE);
				lrub = new TridasLastRingUnderBark();
				lrub.setPresence(PresenceAbsence.PRESENT);
				lrub.setContent(I18n.getText("seasons.winter"));
				wc.getSapwood().setLastRingUnderBark(lrub);
				break;
			case SUMMER:
				wc.getSapwood().setPresence(ComplexPresenceAbsence.COMPLETE);
				lrub = new TridasLastRingUnderBark();
				lrub.setPresence(PresenceAbsence.PRESENT);
				lrub.setContent(I18n.getText("seasons.summer"));
				wc.getSapwood().setLastRingUnderBark(lrub);
				break;
			case HS_BOUNDARY:
				wc.getSapwood().setPresence(ComplexPresenceAbsence.INCOMPLETE);
				lrub = new TridasLastRingUnderBark();
				lrub.setPresence(PresenceAbsence.ABSENT);
				lrub.setContent(I18n.getText(" "));
				wc.getSapwood().setLastRingUnderBark(lrub);
				break;
			case POSS_HS_BOUNDARY:
			case NO_SPECFIC_EDGE:
			case SAP_BARK_UNKNOWN:
			case CHARRED:
			case POSSIBLY_CHARRED:
			case POSS_BARK:
				// All unhandled as there are no corresponding fields in TRiDaS
				// Data is put in generic field 
			}
			
			
			
		} catch (NullPointerException e){}
			
		ms.setWoodCompleteness(wc);
		return ms;
	}
	
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasDerivedSeries()
	 */
	@Override
	protected TridasDerivedSeries getDefaultTridasDerivedSeries() {
		TridasDerivedSeries ds = super.getDefaultTridasDerivedSeries();

		ds.setTitle(getStringDefaultValue(DefaultFields.SERIES_TITLE).getStringValue());
		ds.setComments(getStringDefaultValue(DefaultFields.SERIES_COMMENT).getStringValue());
		
		// Start year info
		try{
			GenericDefaultValue<SafeIntYear> startYearField = (GenericDefaultValue<SafeIntYear>) getDefaultValue(DefaultFields.START_YEAR); 		
			ds.getInterpretation().setFirstYear(startYearField.getValue().toTridasYear(DatingSuffix.AD));
		} catch (NullPointerException e){}
		
		// Get any generic fields 
		if(getDerivedSeriesGenericFields().size()>0)
		{
			ds.setGenericFields(getDerivedSeriesGenericFields());
		}
		
		try{
			GenericDefaultValue<SheffieldChronologyType> chronologyTypeField = (GenericDefaultValue<SheffieldChronologyType>) getDefaultValue(DefaultFields.SHEFFIELD_CHRONOLOGY_TYPE); 		
			if(chronologyTypeField.getValue()!=null)
			{			
				ControlledVoc chronType = new ControlledVoc();
				chronType.setNormalStd("Sheffield D-Format");
				chronType.setNormalId(chronologyTypeField.getValue().toCode());
				chronType.setNormal(chronologyTypeField.getValue().toString());
				ds.setType(chronType);
			}			
		} catch (NullPointerException e){}
		
		return ds;	
	}
	
	@SuppressWarnings("unchecked")
	public TridasValues getTridasValuesWithDefaults(){
		TridasValues valuesGroup = new TridasValues();
		
		
		TridasUnit units = new TridasUnit();
		
		// Set units to 1/100th mm.  Is this always the case?
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		
		valuesGroup.setUnit(units);
		
		if(getDefaultValue(DefaultFields.SHEFFIELD_VARIABLE_TYPE).getValue()!=null)
		{
			TridasVariable variable = new TridasVariable();
			switch((SheffieldVariableCode)getDefaultValue(DefaultFields.SHEFFIELD_VARIABLE_TYPE).getValue())
			{
			case RING_WIDTHS:
				variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
				break;
			case EARLY_WOOD_WIDTHS:
				variable.setNormalTridas(NormalTridasVariable.EARLYWOOD_WIDTH);
				break;
			case LATE_WOOD_WIDTHS:
				variable.setNormalTridas(NormalTridasVariable.LATEWOOD_WIDTH);
				break;
			case EARLY_AND_LATE_WOOD_WIDTHS_REVERSED:
				variable.setNormalId("R");
				variable.setNormalStd("Sheffield D-Format");
				variable.setNormal("Early and late wood widths reversed");
				variable.setValue("Early and late wood widths reversed");
				break;
			case MINIMUM_DENSITY:
				variable.setNormalId("I");
				variable.setNormalStd("Sheffield D-Format");
				variable.setNormalStd("Minimum density");
				variable.setValue("Minimum density");
				break;
			case MAXIMUM_DENSITY:
				variable.setNormalTridas(NormalTridasVariable.MAXIMUM_DENSITY);
				break;
			case EARLY_AND_LATE_SEQUENTIALLY:
				// ERROR!!!! shouldn't have t his
				break;
			case MIXED:
				variable.setNormalId("M");
				variable.setNormalStd("Sheffield D-Format");
				variable.setNormalStd("Mixed");
				variable.setValue("Mixed");
				break;
			default :
				variable.setValue(I18n.getText("unknown"));
			}
			valuesGroup.setVariable(variable);
		}
		else
		{
			GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
			valuesGroup.setVariable(variable.getValue());
		}
		
		
		return valuesGroup;
	}
	
	private ArrayList<TridasGenericField> getMeasurementSeriesGenericFields(){
		
		ArrayList<TridasGenericField>genFields = new ArrayList<TridasGenericField>(); 
		
		if(getDefaultValue(DefaultFields.PITH_DESCRIPTION).getValue()!=null)
		{
			TridasGenericField gf = new ObjectFactory().createTridasGenericField();
			gf.setName("sheffield.pithCode");
			gf.setType("xs:string");
			gf.setValue(getDefaultValue(DefaultFields.PITH_DESCRIPTION).getValue().toString());
			genFields.add(gf);
		}
		
		if(getDefaultValue(DefaultFields.SHEFFIELD_DATA_TYPE).getValue()!=null)
		{
			TridasGenericField gf = new ObjectFactory().createTridasGenericField();
			gf.setName("sheffield.dataType");
			gf.setType("xs:string");
			gf.setValue(getDefaultValue(DefaultFields.SHEFFIELD_DATA_TYPE).getValue().toString());
			genFields.add(gf);
		}
		
		if(getDefaultValue(DefaultFields.INTERPRETATION_NOTES).getValue()!=null)
		{
			TridasGenericField gf = new ObjectFactory().createTridasGenericField();
			gf.setName("sheffield.interpretationAndAnatomyNotes");
			gf.setType("xs:string");
			gf.setValue(getDefaultValue(DefaultFields.INTERPRETATION_NOTES).getValue().toString());
			genFields.add(gf);
		}
		
		if(getDefaultValue(DefaultFields.SHEFFIELD_EDGE_CODE).getValue()!=null)
		{
			TridasGenericField gf = new ObjectFactory().createTridasGenericField();
			gf.setName("sheffield.edgeCode");
			gf.setType("xs:string");
			gf.setValue(getDefaultValue(DefaultFields.SHEFFIELD_EDGE_CODE).getValue().toString());
			genFields.add(gf);	
		}
		
		return genFields;
		
	}
	
	
	private ArrayList<TridasGenericField> getDerivedSeriesGenericFields(){
		
		ArrayList<TridasGenericField>genFields = new ArrayList<TridasGenericField>(); 
		
		
		if(getDefaultValue(DefaultFields.SHEFFIELD_DATA_TYPE).getValue()!=null)
		{
			TridasGenericField gf = new ObjectFactory().createTridasGenericField();
			gf.setName("sheffield.dataType");
			gf.setType("xs:string");
			gf.setValue(getDefaultValue(DefaultFields.SHEFFIELD_DATA_TYPE).getValue().toString());
			genFields.add(gf);
		}
		
		if(getDefaultValue(DefaultFields.INTERPRETATION_NOTES).getValue()!=null)
		{
			TridasGenericField gf = new ObjectFactory().createTridasGenericField();
			gf.setName("sheffield.interpretationAndAnatomyNotes");
			gf.setType("xs:string");
			gf.setValue(getDefaultValue(DefaultFields.INTERPRETATION_NOTES).getValue().toString());
			genFields.add(gf);
		}
		
		return genFields;
		
	}
	
}
