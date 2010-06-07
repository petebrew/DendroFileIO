package org.tridas.io.defaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.tridas.io.I18n;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.NormalTridasMeasuringMethod;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.SeriesLinks;
import org.tridas.schema.TridasAddress;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasCategory;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasLaboratory;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasMeasuringMethod;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;
import org.tridas.schema.TridasLaboratory.Name;

/**
 * Mandatory fields as specified by the Tridas schema along with
 * their default values
 * 
 * @author peterbrewer
 */
public class TridasMetadataFieldSet extends AbstractTridasMetadataFieldSet {
	
	public enum TridasMandatoryField {
		PROJECT_TITLE, PROJECT_TYPES, PROJECT_LABORATORIES, PROJECT_CATEGORY, PROJECT_INVESTIGATOR, PROJECT_PERIOD, OBJECT_TITLE, OBJECT_TYPE, ELEMENT_TITLE, ELEMENT_TAXON, SAMPLE_TITLE, SAMPLE_TYPE, RADIUS_TITLE, MEASUREMENTSERIES_TITLE, MEASUREMENTSERIES_MEASURINGMETHOD, MEASUREMENTSERIES_VARIABLE, DERIVEDSERIES_TITLE, DERIVEDSERIES_TYPE, DERIVEDSERIES_IDENTIFIER, IDENTIFIER_DOMAIN;
	}
	
	@Override
	protected void initDefaultValues() {
		setDefaultValue(TridasMandatoryField.PROJECT_TITLE, new StringDefaultValue(I18n.getText("unnamed.project")));
		
		ArrayList<ControlledVoc> projectTypes = new ArrayList<ControlledVoc>();
		ControlledVoc projType = new ObjectFactory().createControlledVoc();
		projType.setValue(I18n.getText("unknown"));
		projectTypes.add(projType);
		setDefaultValue(TridasMandatoryField.PROJECT_TYPES, new GenericDefaultValue<ArrayList<ControlledVoc>>(
				projectTypes));
		
		ArrayList<TridasLaboratory> projectLaboratories = new ArrayList<TridasLaboratory>();
		TridasLaboratory lab = new TridasLaboratory();
		Name labname = new Name();
		labname.setValue(I18n.getText("unnamed.lab"));
		lab.setName(labname);
		lab.setAddress(new TridasAddress());
		projectLaboratories.add(lab);
		setDefaultValue(TridasMandatoryField.PROJECT_LABORATORIES,
				new GenericDefaultValue<ArrayList<TridasLaboratory>>(projectLaboratories));
		
		TridasCategory projectCategory = new ObjectFactory().createTridasCategory();
		projectCategory.setValue(I18n.getText("unknown"));
		setDefaultValue(TridasMandatoryField.PROJECT_CATEGORY, new GenericDefaultValue<TridasCategory>(projectCategory));
		
		setDefaultValue(TridasMandatoryField.PROJECT_INVESTIGATOR, new StringDefaultValue(I18n.getText("unknown")));
		setDefaultValue(TridasMandatoryField.PROJECT_PERIOD, new StringDefaultValue(I18n.getText("unknown")));
		setDefaultValue(TridasMandatoryField.OBJECT_TITLE, new StringDefaultValue(I18n.getText("unnamed.object")));
		
		ControlledVoc objectType = new ControlledVoc();
		objectType.setValue(I18n.getText("unknown"));
		setDefaultValue(TridasMandatoryField.OBJECT_TYPE, new GenericDefaultValue<ControlledVoc>(objectType));
		setDefaultValue(TridasMandatoryField.ELEMENT_TITLE, new StringDefaultValue(I18n.getText("unnamed.element")));
		
		ControlledVoc elementTaxon = new ControlledVoc();
		elementTaxon.setValue("Plantae");
		setDefaultValue(TridasMandatoryField.ELEMENT_TAXON, new GenericDefaultValue<ControlledVoc>(elementTaxon));
		setDefaultValue(TridasMandatoryField.SAMPLE_TITLE, new StringDefaultValue(I18n.getText("unnamed.sample")));
		
		ControlledVoc sampleType = new ControlledVoc();
		sampleType.setValue(I18n.getText("unknown"));
		setDefaultValue(TridasMandatoryField.SAMPLE_TYPE, new GenericDefaultValue<ControlledVoc>(sampleType));
		setDefaultValue(TridasMandatoryField.RADIUS_TITLE, new StringDefaultValue(I18n.getText("unnamed.radius")));
		setDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_TITLE, new StringDefaultValue(I18n
				.getText("unnamed.series")));
		
		TridasMeasuringMethod measurementSeriesMeasuringMethod = new TridasMeasuringMethod();
		measurementSeriesMeasuringMethod.setNormalTridas(NormalTridasMeasuringMethod.MEASURING___PLATFORM);
		setDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_MEASURINGMETHOD,
				new GenericDefaultValue<TridasMeasuringMethod>(measurementSeriesMeasuringMethod));
		
		TridasVariable measurementSeriesVariable = new TridasVariable();
		measurementSeriesVariable.setValue(I18n.getText("unknown"));
		setDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE, new GenericDefaultValue<TridasVariable>(
				measurementSeriesVariable));
		setDefaultValue(TridasMandatoryField.DERIVEDSERIES_TITLE,
				new StringDefaultValue(I18n.getText("unnamed.series")));
		
		ControlledVoc derivedSeriesType = new ControlledVoc();
		derivedSeriesType.setValue(I18n.getText("unknown"));
		setDefaultValue(TridasMandatoryField.DERIVEDSERIES_TYPE, new GenericDefaultValue<ControlledVoc>(
				derivedSeriesType));
		setDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN, new StringDefaultValue(I18n.getText("domain.value")));
		setDefaultValue(TridasMandatoryField.DERIVEDSERIES_IDENTIFIER, new StringDefaultValue(UUID.randomUUID()
				.toString()));
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected TridasProject getDefaultTridasProject() {
		TridasProject project = new TridasProject();
		
		project.setTypes((List<ControlledVoc>) getDefaultValue(TridasMandatoryField.PROJECT_TYPES).getValue());
		project.setLaboratories((List<TridasLaboratory>) getDefaultValue(TridasMandatoryField.PROJECT_LABORATORIES)
				.getValue());
		project.setTitle(getDefaultValue(TridasMandatoryField.PROJECT_TITLE).getStringValue());
		project.setInvestigator(getDefaultValue(TridasMandatoryField.PROJECT_INVESTIGATOR).getStringValue());
		project.setCategory((TridasCategory) getDefaultValue(TridasMandatoryField.PROJECT_CATEGORY).getValue());
		project.setPeriod(getDefaultValue(TridasMandatoryField.PROJECT_PERIOD).getStringValue());
		return project;
	}
	
	@Override
	protected TridasObject getDefaultTridasObject() {
		TridasObject o = new ObjectFactory().createTridasObject();
		
		// Object identifier
		// TridasIdentifier identifier = new ObjectFactory().createTridasIdentifier();
		// o.setIdentifier(identifier);
		
		// Object type
		ControlledVoc type = (ControlledVoc) getDefaultValue(TridasMandatoryField.OBJECT_TYPE).getValue();
		o.setType(type);
		
		// Object title
		o.setTitle(getDefaultValue(TridasMandatoryField.OBJECT_TITLE).getStringValue());
		
		return o;
	}
	
	@Override
	protected TridasElement getDefaultTridasElement() {
		ObjectFactory factory = new ObjectFactory();
		TridasElement e = factory.createTridasElement();
		
		e.setTitle(getDefaultValue(TridasMandatoryField.ELEMENT_TITLE).getStringValue());
		e.setTaxon((ControlledVoc) getDefaultValue(TridasMandatoryField.ELEMENT_TAXON).getValue());
		
		return e;
	}
	
	@Override
	protected TridasSample getDefaultTridasSample() {
		ObjectFactory factory = new ObjectFactory();
		TridasSample s = factory.createTridasSample();
		
		s.setTitle(getDefaultValue(TridasMandatoryField.SAMPLE_TITLE).getStringValue());
		s.setType((ControlledVoc) getDefaultValue(TridasMandatoryField.SAMPLE_TYPE).getValue());
		
		return s;
	}
	
	@Override
	protected TridasRadius getDefaultTridasRadius() {
		TridasRadius r = new ObjectFactory().createTridasRadius();
		
		r.setTitle(getDefaultValue(TridasMandatoryField.RADIUS_TITLE).getStringValue());
		return r;
	}
	
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		
		TridasMeasurementSeries ms = new ObjectFactory().createTridasMeasurementSeries();
		
		ms.setTitle(getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_TITLE).getStringValue());
		ms.setMeasuringMethod((TridasMeasuringMethod) getDefaultValue(
				TridasMandatoryField.MEASUREMENTSERIES_MEASURINGMETHOD).getValue());
		ms.setInterpretation(new ObjectFactory().createTridasInterpretation());
		return ms;
	}
	
	@Override
	protected TridasDerivedSeries getDefaultTridasDerivedSeries() {
		
		TridasDerivedSeries ds = new ObjectFactory().createTridasDerivedSeries();
		TridasIdentifier id = new ObjectFactory().createTridasIdentifier();
		id.setDomain(getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		id.setValue(getDefaultValue(TridasMandatoryField.DERIVEDSERIES_IDENTIFIER).getStringValue());
		ds.setTitle(getDefaultValue(TridasMandatoryField.DERIVEDSERIES_TITLE).getStringValue());
		ds.setType((ControlledVoc) getDefaultValue(TridasMandatoryField.DERIVEDSERIES_TYPE).getValue());
		ds.setLinkSeries(new SeriesLinks());
		ds.setIdentifier(id);
		return ds;
	}
	
	protected TridasWoodCompleteness getDefaultWoodCompleteness() {
		
		TridasWoodCompleteness wc = new ObjectFactory().createTridasWoodCompleteness();
		
		TridasPith pith = new ObjectFactory().createTridasPith();
		TridasHeartwood heartwd = new ObjectFactory().createTridasHeartwood();
		TridasSapwood sapwd = new ObjectFactory().createTridasSapwood();
		TridasBark bark = new ObjectFactory().createTridasBark();
		
		pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
		heartwd.setPresence(ComplexPresenceAbsence.UNKNOWN);
		sapwd.setPresence(ComplexPresenceAbsence.UNKNOWN);
		bark.setPresence(PresenceAbsence.ABSENT);
		
		wc.setPith(pith);
		wc.setHeartwood(heartwd);
		wc.setSapwood(sapwd);
		wc.setBark(bark);
		return wc;
	}
}
