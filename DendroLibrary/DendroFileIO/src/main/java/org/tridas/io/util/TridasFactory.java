package org.tridas.io.util;

import java.util.ArrayList;

import org.tridas.io.I18n;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasAddress;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasLaboratory;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasWoodCompleteness;
import org.tridas.util.TridasObjectEx;

public class TridasFactory {

	
	public static TridasWoodCompleteness getWoodCompleteness()
	{
		TridasWoodCompleteness wc = new TridasWoodCompleteness();
		
		TridasPith pith = new TridasPith();
		TridasHeartwood hw = new TridasHeartwood();
		TridasSapwood sw = new TridasSapwood();
		TridasBark bark = new TridasBark();
		
		pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
		bark.setPresence(PresenceAbsence.UNKNOWN);
		hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
		sw.setPresence(ComplexPresenceAbsence.UNKNOWN);
		
		wc.setPith(pith);
		wc.setHeartwood(hw);
		wc.setSapwood(sw);
		wc.setBark(bark);
		
		
		
		return wc;
		
	}
	
	public static TridasProject getNewTridasProject()
	{
		TridasProject entity = new TridasProject();
		
		entity.setTitle(I18n.getText("unnamed.project"));
		
		ControlledVoc type = new ControlledVoc();
		type.setValue(I18n.getText("unknown"));
		ArrayList<ControlledVoc> types = new ArrayList<ControlledVoc>();
		types.add(type);
		entity.setTypes(types);
		
		TridasLaboratory lab = new TridasLaboratory();
		TridasLaboratory.Name name = new TridasLaboratory.Name(); 
		name.setValue(I18n.getText("unknown"));
		name.setAcronym(I18n.getText("unknown"));
		lab.setName(name);
		TridasAddress addr = new TridasAddress();
		addr.setCountry(I18n.getText("unknown"));
		lab.setAddress(addr);
		ArrayList<TridasLaboratory> laboratories = new ArrayList<TridasLaboratory>();
		laboratories.add(lab);
		entity.setLaboratories(laboratories);
		
		ControlledVoc category = new ControlledVoc();		
		category.setValue(I18n.getText("unknown"));
		entity.setCategory(category);
		
		entity.setInvestigator(I18n.getText("unknown"));
		entity.setPeriod(I18n.getText("unknown"));
	

		return entity;
		
		
	}
	
	public static TridasObjectEx getNewTridasObject()
	{
		TridasObjectEx object = new TridasObjectEx();
		
		object.setTitle(I18n.getText("unnamed.object"));
		
		ControlledVoc type = new ControlledVoc();
		type.setValue(I18n.getText("unknown"));
		object.setType(type);

		return object;
		
		
	}
	
	public static TridasElement getNewTridasElement()
	{
		TridasElement entity = new TridasElement();
		
		entity.setTitle(I18n.getText("unnamed.element"));

		ControlledVoc taxon = new ControlledVoc();
		taxon.setValue(I18n.getText("unknown"));
		entity.setTaxon(taxon);
	

		return entity;
		
	}
	
	public static TridasSample getNewTridasSample()
	{
		TridasSample entity = new TridasSample();
		
		entity.setTitle(I18n.getText("unnamed.element"));

		ControlledVoc type = new ControlledVoc();
		type.setValue(I18n.getText("unknown"));
		entity.setType(type);
	

		return entity;
		
	}
	
	public static TridasRadius getNewTridasRadius()
	{
		TridasRadius entity = new TridasRadius();
		
		entity.setTitle(I18n.getText("unnamed.radius"));


		return entity;
		
	}
	
}
