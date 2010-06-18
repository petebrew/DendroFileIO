package org.tridas.io.formats.sheffield;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.AbstractMetadataFieldSet;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SheffieldStringDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.formats.heidelberg.HeidelbergToTridasDefaults.DefaultFields;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.NormalTridasShape;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasWoodCompleteness;

public class TridasToSheffieldDefaults extends AbstractMetadataFieldSet implements IMetadataFieldSet {
	
	public static enum DefaultFields {
		SITE_NAME, RING_COUNT, DATE_TYPE, START_DATE, DATA_TYPE, SAPWOOD_COUNT, TIMBER_COUNT, EDGE_CODE, CHRONOLOGY_TYPE, COMMENT, UK_COORDS, LAT_LONG, PITH_CODE, SHAPE_CODE, MAJOR_DIM, MINOR_DIM, INNER_RING_CODE, OUTER_RING_CODE, PHASE, SHORT_TITLE, PERIOD, TAXON, INTERPRETATION_COMMENT, VARIABLE_TYPE;
	}
	
	@Override
	public void initDefaultValues() {
		setDefaultValue(DefaultFields.SITE_NAME, new SheffieldStringDefaultValue(I18n.getText("unnamed.object"), 1, 64));
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue(1, 2147483647));
		setDefaultValue(DefaultFields.DATE_TYPE, new GenericDefaultValue<SheffieldDateType>(SheffieldDateType.ABSOLUTE));
		setDefaultValue(DefaultFields.START_DATE, new IntegerDefaultValue(1));
		setDefaultValue(DefaultFields.DATA_TYPE, new GenericDefaultValue<SheffieldDataType>(SheffieldDataType.ANNUAL_RAW_RING_WIDTH));
		setDefaultValue(DefaultFields.SAPWOOD_COUNT, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.TIMBER_COUNT, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.EDGE_CODE, new GenericDefaultValue<SheffieldEdgeCode>(SheffieldEdgeCode.NO_SPECFIC_EDGE));
		setDefaultValue(DefaultFields.CHRONOLOGY_TYPE, new GenericDefaultValue<SheffieldChronologyType>(SheffieldChronologyType.UNKNOWN_MEAN));
		setDefaultValue(DefaultFields.COMMENT, new SheffieldStringDefaultValue("?", 1, 64));
		setDefaultValue(DefaultFields.UK_COORDS, new StringDefaultValue("?", 1, 14));
		setDefaultValue(DefaultFields.LAT_LONG, new StringDefaultValue("?", 1, 64));
		setDefaultValue(DefaultFields.PITH_CODE, new GenericDefaultValue<SheffieldPithCode>(SheffieldPithCode.UNKNOWN));
		setDefaultValue(DefaultFields.SHAPE_CODE, new GenericDefaultValue<SheffieldShapeCode>(SheffieldShapeCode.UNKNOWN));
		setDefaultValue(DefaultFields.MAJOR_DIM, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.MINOR_DIM, new IntegerDefaultValue(0));
		setDefaultValue(DefaultFields.INNER_RING_CODE, new StringDefaultValue("N", 1, 5));
		setDefaultValue(DefaultFields.OUTER_RING_CODE, new StringDefaultValue("N", 1, 5));
		setDefaultValue(DefaultFields.PHASE, new SheffieldStringDefaultValue(I18n.getText("unknown"), 1, 14));
		setDefaultValue(DefaultFields.SHORT_TITLE, new SheffieldStringDefaultValue(I18n.getText("unknown"), 1, 8));
		setDefaultValue(DefaultFields.PERIOD, new GenericDefaultValue<SheffieldPeriodCode>(SheffieldPeriodCode.UNKNOWN));
		setDefaultValue(DefaultFields.TAXON, new StringDefaultValue("UNKN", 4, 4));
		setDefaultValue(DefaultFields.INTERPRETATION_COMMENT, new SheffieldStringDefaultValue("?", 1, 64));
		setDefaultValue(DefaultFields.VARIABLE_TYPE, new GenericDefaultValue<SheffieldVariableCode>(SheffieldVariableCode.RING_WIDTHS));
	}
	

	public void populateFromTridasProject(TridasProject p) {


	}
	
	public void populateFromTridasObject(TridasObject o) {

		// Object.title = Site Name
		if(o.isSetTitle())
		{
			getSheffieldStringDefaultValue(DefaultFields.SITE_NAME).setValue(o.getTitle());
		}

	}
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasElement(TridasElement e) {

		// Element.taxon = Taxon
		if(e.isSetTaxon())
		{
			if(e.getTaxon().isSetNormal())
			{
				// Try from normalised field first
				getStringDefaultValue(DefaultFields.TAXON).setValue(ITRDBTaxonConverter.getNormalisedCode(e.getTaxon().getNormalId()));
			}
			else
			{
				// If not, from the value instead
				getStringDefaultValue(DefaultFields.TAXON).setValue(ITRDBTaxonConverter.getNormalisedCode(ITRDBTaxonConverter.getCodeFromName(e.getTaxon().getValue())));
			}
		}
		
		// Element.shape = cross-section code
		if(e.isSetShape())
		{
			if(e.getShape().isSetNormalTridas())
			{
				GenericDefaultValue<SheffieldShapeCode> shapeField = (GenericDefaultValue<SheffieldShapeCode>)getDefaultValue(DefaultFields.SHAPE_CODE);
				switch(e.getShape().getNormalTridas())
				{
				case BEAM___STRAIGHTENED___ON___ONE___SIDE:
					shapeField.setValue(SheffieldShapeCode.WHOLE_ROUND_IRREGULARLY_TRIMMED);
					break;
				case HALF___SECTION:
					shapeField.setValue(SheffieldShapeCode.HALF_ROUND_UNTRIMMED);
					break;
				case PART___OF___UNDETERMINED___SECTION:
					shapeField.setValue(SheffieldShapeCode.UNKNOWN);
					break;
				case PLANK___CUT___ON___ONE___SIDE:
					shapeField.setValue(SheffieldShapeCode.RADIAL_PLANK_TRIMMED);
					break;
				case PLANK___NOT___INCLUDING___PITH___WITH___BREADTH___SMALLER___THAN___A___QUARTER___SECTION:
					shapeField.setValue(SheffieldShapeCode.TANGENTIAL_PLANK_TRIMMED);
					break;
				case QUARTER___SECTION:
					shapeField.setValue(SheffieldShapeCode.QUARTERED_UNTRIMMED);
					break;
				case RADIAL___PLANK___THROUGH___PITH:
					shapeField.setValue(SheffieldShapeCode.RADIAL_PLANK_TRIMMED);
					break;
				case RADIAL___PLANK___UP___TO___PITH:
					shapeField.setValue(SheffieldShapeCode.RADIAL_PLANK_IRREGULARLY_TRIMMED);
					break;
				case SMALL___PART___OF___SECTION:
					shapeField.setValue(SheffieldShapeCode.QUARTERED_IRREGULARLY_TRIMMED);
					break;
				case SQUARED___BEAM___FROM___HALF___SECTION:
					shapeField.setValue(SheffieldShapeCode.HALF_ROUND_TRIMMED);
					break;
				case SQUARED___BEAM___FROM___QUARTER___SECTION:
					shapeField.setValue(SheffieldShapeCode.QUARTERED_TRIMMED);
					break;
				case SQUARED___BEAM___FROM___WHOLE___SECTION:
					shapeField.setValue(SheffieldShapeCode.WHOLE_ROUND_TRIMMED);
					break;
				case TANGENTIAL___PLANK___NOT___INCLUDING___PITH___WITH___BREADTH___LARGER___THAN___A___QUARTER___SECTION:
					shapeField.setValue(SheffieldShapeCode.TANGENTIAL_PLANK_IRREGULARLY_TRIMMED);
					break;
				case THIRD___SECTION:
					shapeField.setValue(SheffieldShapeCode.QUARTERED_UNTRIMMED);
					break;
				case WHOLE___SECTION:
					shapeField.setValue(SheffieldShapeCode.WHOLE_ROUND_UNTRIMMED);
					break;
				case UNKNOWN:
				case WEDGE___WHERE___RADIUS___EQUALS___THE___CIRCUMFERENCE:
				case WEDGE___WHERE___RADIUS___IS___BIGGER___THAN___THE___CIRCUMFERENCE:
				case WEDGE___WHERE___RADIUS___IS___SMALLER___THAN___CIRCUMFERENCE:
					shapeField.setValue(SheffieldShapeCode.UNKNOWN);
					break;
				}
			}
		}
		
		// Major and minor dimensions
		if (e.isSetDimensions())
		{			
			BigDecimal majordim = new BigDecimal(0);
			BigDecimal minordim = new BigDecimal(999999);
			if(e.getDimensions().isSetHeight())
			{
				if (e.getDimensions().getHeight().compareTo(majordim)>0)
				{
					majordim = e.getDimensions().getHeight();
				}
				if (e.getDimensions().getHeight().compareTo(minordim)<0)
				{
					minordim = e.getDimensions().getHeight();
				}
			}
			if(e.getDimensions().isSetWidth())
			{
				if (e.getDimensions().getWidth().compareTo(majordim)>0)
				{
					majordim = e.getDimensions().getWidth();
				}
				if (e.getDimensions().getWidth().compareTo(minordim)<0)
				{
					minordim = e.getDimensions().getWidth();
				}
			}
			if(e.getDimensions().isSetDepth())
			{
				if (e.getDimensions().getDepth().compareTo(majordim)>0)
				{
					majordim = e.getDimensions().getDepth();
				}
				if (e.getDimensions().getDepth().compareTo(minordim)<0)
				{
					minordim = e.getDimensions().getDepth();
				}
			}
			if(e.getDimensions().isSetDiameter())
			{
				if (e.getDimensions().getDiameter().compareTo(majordim)>0)
				{
					majordim = e.getDimensions().getDiameter();
				}
				if (e.getDimensions().getDiameter().compareTo(minordim)<0)
				{
					minordim = e.getDimensions().getDiameter();
				}
			}
			
			// Dimensions have been set
			if(majordim.compareTo(minordim)>0)
			{
				NormalTridasUnit units = null;
				if(e.getDimensions().getUnit().isSetNormalTridas())
				{
					// Set using applicable units
					units = e.getDimensions().getUnit().getNormalTridas();
					Integer majordimval = Math.round(UnitUtils.convertBigDecimal(units, NormalTridasUnit.MILLIMETRES,majordim).floatValue());
					Integer minordimval = Math.round(UnitUtils.convertBigDecimal(units, NormalTridasUnit.MILLIMETRES,minordim).floatValue());
					if(majordimval!=null) getIntegerDefaultValue(DefaultFields.MAJOR_DIM).setValue(majordimval);
					if(minordimval!=null) getIntegerDefaultValue(DefaultFields.MINOR_DIM).setValue(minordimval);
				}
				else
				{
					// Units not standardised so can't safely set fields
				}
			}
		}

	}
	
	public void populateFromTridasSample(TridasSample s) {


	}
	
	public void populateFromTridasRadius(TridasRadius r) {


	}
	
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasMeasurementSeries(TridasMeasurementSeries ms) {
		GenericDefaultValue<SheffieldDataType> dataTypeField = (GenericDefaultValue<SheffieldDataType>)getDefaultValue(DefaultFields.DATA_TYPE);
		dataTypeField.setValue(SheffieldDataType.ANNUAL_RAW_RING_WIDTH);
		
		// Author and comment
		String comment ="";
		if(ms.isSetDendrochronologist())
		{
			comment+= ms.getDendrochronologist();
		}
		if(ms.isSetComments())
		{
			comment+= " " +ms.getComments();
		}
		getSheffieldStringDefaultValue(DefaultFields.COMMENT).setValue(comment);
	}
	
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasDerivedSeries(TridasDerivedSeries ds) {

		GenericDefaultValue<SheffieldDataType> dataTypeField = (GenericDefaultValue<SheffieldDataType>)getDefaultValue(DefaultFields.DATA_TYPE);
		dataTypeField.setValue(SheffieldDataType.CHRON_MEAN);
		
		// Author and comment
		String comment ="";
		if(ds.isSetAuthor())
		{
			comment+= ds.getAuthor();
		}
		if(ds.isSetComments())
		{
			comment+= " " +ds.getComments();
		}
		getSheffieldStringDefaultValue(DefaultFields.COMMENT).setValue(comment);
	}
	
	private void populateFromTridasSeries(ITridasSeries ser)
	{
		// series.title = Short title
		if (ser.isSetTitle())
		{
			getStringDefaultValue(DefaultFields.SHORT_TITLE).setValue(ser.getTitle());

		}
		
		// woodcompleteness.interpretation.firstyear = start date
		if(ser.isSetInterpretation())
		{
			if(ser.getInterpretation().isSetFirstYear())
			{
				SafeIntYear firstYear = new SafeIntYear(ser.getInterpretation().getFirstYear());
				getIntegerDefaultValue(DefaultFields.START_DATE).setValue(firstYear.toAstronomicalInteger()+10000);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void populateFromTridasValues(TridasValues argValues) {

		// Data type (variable) = values.variable.normaltridas
		GenericDefaultValue<SheffieldVariableCode> variableField = (GenericDefaultValue<SheffieldVariableCode>)getDefaultValue(DefaultFields.VARIABLE_TYPE);
		if(argValues.isSetVariable())
		{		
			if(argValues.getVariable().isSetNormalTridas())
			{
				switch(argValues.getVariable().getNormalTridas())
				{
				case RING_WIDTH:
					variableField.setValue(SheffieldVariableCode.RING_WIDTHS);
					break;
				case EARLYWOOD_WIDTH:
					variableField.setValue(SheffieldVariableCode.EARLY_WOOD_WIDTHS);
					break;	
				case MAXIMUM_DENSITY:
					variableField.setValue(SheffieldVariableCode.MAXIMUM_DENSITY);
					break;
				
				case RING_DENSITY:
				case LATEWOOD_WIDTH:
				case LATEWOOD_PERCENT:
				case EARLYWOOD_DENSITY:
				case LATEWOOD_DENSITY:
				default:
					variableField.setValue(SheffieldVariableCode.MIXED);
					break;
				}
			}
			else
			{
				variableField.setValue(SheffieldVariableCode.RING_WIDTHS);
			}
		}
		else
		{
			variableField.setValue(SheffieldVariableCode.RING_WIDTHS);
		}
				
		// Count of values = number of rings
		getIntegerDefaultValue(DefaultFields.RING_COUNT).setValue(argValues.getValues().size());
		
		
	}
	
	@SuppressWarnings("unchecked")
	public void populateFromWoodCompleteness(TridasMeasurementSeries ms, TridasRadius r)
	{
		TridasWoodCompleteness wc = null;
		TridasSapwood sapwood = null;
		TridasBark bark = null;
		
		// Get the wood completeness from the series if possible, if not then try the radius
		if (ms.isSetWoodCompleteness())
		{
			wc = ms.getWoodCompleteness();
		}
		else if (r.isSetWoodCompleteness())
		{
			wc = r.getWoodCompleteness();
		}
		
		// Woodcompleteness not there so return without doing anything
		if(wc==null) {return ;}
		
		if(wc.isSetSapwood())
		{
			// wc.sapwood.nrofsapwoodrings = sapwood count
			if(wc.getSapwood().isSetNrOfSapwoodRings())
			{
				getIntegerDefaultValue(DefaultFields.SAPWOOD_COUNT).setValue(wc.getSapwood().getNrOfSapwoodRings());
			}
		}
		
		// Edge code calculated from many fields
		GenericDefaultValue<SheffieldEdgeCode> edgeCodeField = (GenericDefaultValue<SheffieldEdgeCode>)getDefaultValue(DefaultFields.EDGE_CODE);
		if(wc.isSetBark())
		{
			if(wc.getBark().equals(PresenceAbsence.PRESENT))
			{
				edgeCodeField.setValue(SheffieldEdgeCode.BARK);
			}
			else if (wc.isSetSapwood())
			{
				if( (wc.getSapwood().getPresence().equals(ComplexPresenceAbsence.COMPLETE)) || 
						(wc.getSapwood().getPresence().equals(ComplexPresenceAbsence.INCOMPLETE)))
					{
						edgeCodeField.setValue(SheffieldEdgeCode.NO_SPECFIC_EDGE);
					}
			}
		}
		else if (wc.isSetSapwood())
		{
			if( (wc.getSapwood().getPresence().equals(ComplexPresenceAbsence.COMPLETE)) || 
					(wc.getSapwood().getPresence().equals(ComplexPresenceAbsence.INCOMPLETE)))
				{
					edgeCodeField.setValue(SheffieldEdgeCode.NO_SPECFIC_EDGE);
				}
		}
		
		// Pith code
		GenericDefaultValue<SheffieldPithCode> pithCodeField = (GenericDefaultValue<SheffieldPithCode>)getDefaultValue(DefaultFields.PITH_CODE);
		if(wc.isSetPith())
		{
			TridasPith pith = wc.getPith();
			if (pith.getPresence().equals(ComplexPresenceAbsence.COMPLETE))
			{
				pithCodeField.setValue(SheffieldPithCode.CENTRE);
			}
			else if (pith.getPresence().equals(ComplexPresenceAbsence.INCOMPLETE))
			{
				pithCodeField.setValue(SheffieldPithCode.WITHIN_FIVE_YEARS);
			}
			else
			{
				pithCodeField.setValue(SheffieldPithCode.UNKNOWN);
			}
		}
		
		// Unmeasured inner rings
		if(wc.isSetNrOfUnmeasuredInnerRings())
		{
			getStringDefaultValue(DefaultFields.INNER_RING_CODE).setValue("?"+wc.getNrOfUnmeasuredInnerRings());
		}
		
		// Unmeasured outer rings
		if(wc.isSetNrOfUnmeasuredOuterRings())
		{
			getStringDefaultValue(DefaultFields.OUTER_RING_CODE).setValue("U"+wc.getNrOfUnmeasuredOuterRings());
		}
		
	}
	
	/**
	 * Populate location fields from TridasObject and TridasElement entities.  If 
	 * location info is in the TridasElement use this as it is more detailed, 
	 * otherwise use the data from the TridasObject.
	 * 
	 * @param o
	 * @param e
	 */
	public void populateFromTridasLocation(TridasObject o, TridasElement e)
	{
		// Grab location from element, or object
		TridasLocation location = null;
		if(e.isSetLocation())
		{
			location = e.getLocation();
		}
		else if (o.isSetLocation())
		{
			location = o.getLocation();
		}	
		if (location==null) {return; }
		
		// Do Coordinate fields
		if(location.isSetLocationGeometry())
		{	
			try{
				List<Double> points = null;
				points = location.getLocationGeometry().getPoint().getPos().getValues();
				if(points.size()!=2) { return;}
				getStringDefaultValue(DefaultFields.LAT_LONG).setValue(points.get(0).toString()+";"+points.get(1).toString());						
			} catch (Exception ex){	}
		}
		
	}
	
	
	public enum SheffieldDateType {
		RELATIVE("R"), ABSOLUTE("A");
		
		private String code;
		
		SheffieldDateType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static SheffieldDateType fromCode(String code) {
			for (SheffieldDateType val : SheffieldDateType.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum SheffieldDataType {
		ANNUAL_RAW_RING_WIDTH("R"), // A, B, E, F, P, S, U, are all equal to R
		TIMBER_MEAN_WITH_SIGNATURES("W"), CHRON_MEAN_WITH_SIGNATURES("X"), TIMBER_MEAN("T"), CHRON_MEAN("C"), UNWEIGHTED_MASTER(
				"M");
		
		private String code;
		
		SheffieldDataType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static SheffieldDataType fromCode(String code) {
			for (SheffieldDataType val : SheffieldDataType.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
				else if ((code.equalsIgnoreCase("A")) || (code.equalsIgnoreCase("B")) || (code.equalsIgnoreCase("E"))
						|| (code.equalsIgnoreCase("F")) || (code.equalsIgnoreCase("P")) || (code.equalsIgnoreCase("S"))
						|| (code.equalsIgnoreCase("U"))) {
					// All synoynms of ANNUAL_RAW_RING_WIDTH
					return SheffieldDataType.ANNUAL_RAW_RING_WIDTH;
				}
				
			}
			return null;
		}
		
	}
	
	public enum SheffieldEdgeCode {
		BARK("Y"), POSS_BARK("!"), WINTER("W"), SUMMER("S"), HS_BOUNDARY("B"), POSS_HS_BOUNDARY("?"), NO_SPECFIC_EDGE(
				"N"), SAP_BARK_UNKNOWN("U"), CHARRED("C"), POSSIBLY_CHARRED("P");
		
		private String code;
		
		SheffieldEdgeCode(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static SheffieldEdgeCode fromCode(String code) {
			for (SheffieldEdgeCode val : SheffieldEdgeCode.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
		
	}
	
	public enum SheffieldChronologyType {
		RAW("R"), FIVE_YEAR_MEAN("5"), INDEXED("I"), UNKNOWN_MEAN("U");
		
		private String code;
		
		SheffieldChronologyType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase());
		}
		
		public final String toCode() {
			return code;
		}
		
		public static SheffieldChronologyType fromCode(String code) {
			for (SheffieldChronologyType val : SheffieldChronologyType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum SheffieldPithCode {
		CENTRE("C"), WITHIN_FIVE_YEARS("V"), FIVE_TO_TEN_YEARS("F"), GREATER_THAN_TEN("G"), UNKNOWN("?");
		
		private String code;
		
		SheffieldPithCode(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static SheffieldPithCode fromCode(String code) {
			for (SheffieldPithCode val : SheffieldPithCode.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum SheffieldShapeCode {
		WHOLE_ROUND_UNTRIMMED("A1"), WHOLE_ROUND_TRIMMED("A2"), WHOLE_ROUND_IRREGULARLY_TRIMMED("AX"), HALF_ROUND_UNTRIMMED(
				"B1"), HALF_ROUND_TRIMMED("B2"), HALF_ROUND_IRREGULARLY_TRIMMED("BX"), QUARTERED_UNTRIMMED("C1"), QUARTERED_TRIMMED(
				"C2"), QUARTERED_IRREGULARLY_TRIMMED("CX"), RADIAL_PLANK_UNTRIMMED("D1"), RADIAL_PLANK_TRIMMED("D2"), RADIAL_PLANK_IRREGULARLY_TRIMMED(
				"DX"), TANGENTIAL_PLANK_UNTRIMMED("E1"), TANGENTIAL_PLANK_TRIMMED("E2"), TANGENTIAL_PLANK_IRREGULARLY_TRIMMED(
				"EX"), UNKNOWN("?"), CORE_UNCLASSIFIABLE("X");
		
		private String code;
		
		SheffieldShapeCode(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static SheffieldShapeCode fromCode(String code) {
			for (SheffieldShapeCode val : SheffieldShapeCode.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum SheffieldPeriodCode {
		MODERN("C"), POST_MEDIEVAL("P"), MEDIEVAL("M"), SAXON("S"), ROMAN("R"), PRE_ROMAN("A"), DUPLICATE("2"), MULTI_PERIOD(
				"B"), UNKNOWN("?");
		
		private String code;
		
		SheffieldPeriodCode(String c) {
			code = c;
		}
		
		public final String toCode() {
			return code;
		}
		
		@Override
		public final String toString() {
			return name();
		}
		
		public static SheffieldPeriodCode fromCode(String code) {
			for (SheffieldPeriodCode val : SheffieldPeriodCode.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum SheffieldVariableCode {
		RING_WIDTHS("D"), EARLY_WOOD_WIDTHS("E"), LATE_WOOD_WIDTHS("L"), EARLY_AND_LATE_WOOD_WIDTHS_REVERSED("R"), MINIMUM_DENSITY(
				"I"), MAXIMUM_DENSITY("A"), EARLY_AND_LATE_SEQUENTIALLY("S"), // ????
		MIXED("M");
		
		private String code;
		
		SheffieldVariableCode(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
		
		public static SheffieldVariableCode fromCode(String code) {
			for (SheffieldVariableCode val : SheffieldVariableCode.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
}
