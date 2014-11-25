/*******************************************************************************
 * Copyright 2011 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.tridas.io.formats.past4;

import java.util.ArrayList;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.past4.TridasToPast4Defaults.DefaultFields;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasRemark;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class Past4Writer extends AbstractDendroCollectionWriter {

	
	private TridasToPast4Defaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	private String p4pProject;
	private ArrayList<String> p4pGroups = new ArrayList<String>(); 
	private ArrayList<String> p4pRecords = new ArrayList<String>();
	
	public Past4Writer() {
		super(TridasToPast4Defaults.class, new Past4Format());
	}
	


	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getNamingConvention()
	 */
	@Override
	public INamingConvention getNamingConvention() {
		return naming;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		naming = argConvension;
	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	private void parseTridasObject(TridasObject o, String parentIndex)
	{
		TridasToPast4Defaults objectDefaults = (TridasToPast4Defaults) defaults.clone();
		objectDefaults.populateFromTridasObject(o);
				
		p4pGroups.add(getGroupTag(objectDefaults, parentIndex));
		
		if(o.isSetElements())
		{
			for (TridasElement e : o.getElements()) 
			{
				TridasToPast4Defaults elementDefaults = (TridasToPast4Defaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);
				elementDefaults.populateFromTridasLocation(o, e);
				
				for (TridasSample s : e.getSamples()) 
				{
					TridasToPast4Defaults sampleDefaults = (TridasToPast4Defaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					
					for (TridasRadius r : s.getRadiuses()) 
					{
						TridasToPast4Defaults radiusDefaults = (TridasToPast4Defaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) 
						{
							TridasToPast4Defaults msDefaults = (TridasToPast4Defaults) radiusDefaults
									.clone();
							msDefaults.populateFromTridasMeasurementSeries(ms);
	
							TridasToPast4Defaults tvDefaults = (TridasToPast4Defaults) msDefaults.clone();
							for (int i = 0; i < ms.getValues().size(); i++) 
							{
								
								
								TridasValues tvsgroup = ms.getValues().get(i);
						
	
								// Create tags for record and add to list
								p4pRecords.add(getRecordTag(tvsgroup, tvDefaults, p4pGroups.size()-1));
								
							}
						}
					}
				}
			}
		}
		
		
		


		// Loop recursively through subobjects
		if(o.isSetObjects())
		{
			for (TridasObject subobj : o.getObjects())
			{
				parseTridasObject(subobj, p4pGroups.size()-1+"");
			}
		}
		
		
		
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException 
	{

		defaults = (TridasToPast4Defaults) argDefaults;
		defaults.populateFromTridasProject(argProject);

		if(argProject.isSetDerivedSeries())
		{
			// Now add any derived Series to another group
			p4pGroups.add(getGroupTag(defaults, "-1"));
			
			for (TridasDerivedSeries ds : argProject.getDerivedSeries())
			{
				defaults.populateFromTridasDerivedSeries(ds);
				
				for (TridasValues tvsgroup : ds.getValues())
				{
					p4pRecords.add(getRecordTag(tvsgroup, defaults, p4pGroups.size()-1));
				}
			}
		}
		
		// Loop through objects getting PAST4 Groups and Records 
		else if(argProject.isSetObjects())
		{
			for (TridasObject o : argProject.getObjects()) 
			{
				parseTridasObject(o, null);
			}
		}
		
		

		// Set the project info
		p4pProject =  getProjectTag(defaults);

		// Create a new Past4File and pass it the info		
		Past4File file = new Past4File(defaults);
		file.setPast4Project(p4pProject);
		file.setPast4Groups(p4pGroups);
		file.setPast4Records(p4pRecords);
		
		naming.registerFile(file, argProject, new TridasDerivedSeries());
		addToFileList(file);
		

		if(this.getFiles().length==0)
		{
			this.clearWarnings();
			throw new ImpossibleConversionException("File conversion failed.  This output format is unable to represent the data stored in the input file.");
		}
	}

	
	
	
	private String getProjectTag(TridasToPast4Defaults defaults)
	{
		String p;
		p =  "<PROJECT ";
		p += "Version=\""+defaults.getIntegerDefaultValue(DefaultFields.PROJ_VERSION).getStringValue()+"\" ";
		p += "Sample=\""+defaults.getIntegerDefaultValue(DefaultFields.PROJ_SAMPLE).getStringValue()+"\" ";
		p += "Reference=\""+defaults.getIntegerDefaultValue(DefaultFields.PROJ_REFERENCE).getStringValue()+"\" ";
		p += "Records=\""+p4pRecords.size()+"\" ";
		p += "PersID=\""+defaults.getStringDefaultValue(DefaultFields.PROJ_PERSID).getStringValue()+"\" ";
		p += "Name=\""+defaults.getStringDefaultValue(DefaultFields.PROJ_NAME).getStringValue()+"\" ";
		p += "Password=\""+defaults.getStringDefaultValue(DefaultFields.PROJ_PASSWORD).getStringValue()+"\" ";
		p += "Locked=\""+defaults.getPast4BooleanDefaultValue(DefaultFields.PROJ_LOCKED).getStringValue()+"\" ";
		p += "Groups=\""+p4pGroups.size()+"\" ";
		p += "ActiveGroup=\""+defaults.getIntegerDefaultValue(DefaultFields.PROJ_ACTIVE_GROUP).getStringValue()+"\" ";
		p += "CreationDate=\""+defaults.getStringDefaultValue(DefaultFields.PROJ_CREATION_DATE).getStringValue()+"\" ";
		p += "EditDate=\""+defaults.getStringDefaultValue(DefaultFields.PROJ_EDIT_DATE).getStringValue()+"\" >";
		p += "<![CDATA[";
		p += defaults.getStringDefaultValue(DefaultFields.PROJ_DESCRIPTION).getStringValue();
		p += "]]></PROJECT>";
		return p;
	}
	
	private String getGroupTag(TridasToPast4Defaults objectDefaults, String parentIndex )
	{
		if(parentIndex==null) parentIndex = "-1";
		
		String group;
		
		group  = "<GROUP ";
		group += "Name=\""+objectDefaults.getStringDefaultValue(DefaultFields.GRP_NAME).getStringValue()+"\" ";
		group += "Visible=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_VISIBLE).getStringValue()+"\" ";
		group += "Fixed=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_FIXED).getStringValue()+"\" ";
		group += "Locked=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_LOCKED).getStringValue()+"\" ";
		group += "Changed=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_CHANGED).getStringValue()+"\" ";
		group += "Expanded=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_EXPANDED).getStringValue()+"\" ";
		group += "UseColor=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_USE_COLOR).getStringValue()+"\" ";
		group += "HasMeanValue=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_HAS_MEAN_VALUE).getStringValue()+"\" ";
		group += "IsChrono=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_IS_CHRONO).getStringValue()+"\" ";
		group += "Checked=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_CHECKED).getStringValue()+"\" ";
		group += "Selected=\""+objectDefaults.getPast4BooleanDefaultValue(DefaultFields.GRP_SELECTED).getStringValue()+"\" ";
		group += "Color=\""+objectDefaults.getIntegerDefaultValue(DefaultFields.GRP_COLOR).getStringValue()+"\" ";
		group += "MVKeycode=\""+objectDefaults.getStringDefaultValue(DefaultFields.GRP_MV_KEYCODE).getStringValue()+"\" ";
		group += "Owner=\""+parentIndex+"\">";
		group += "<![CDATA[";
		group += objectDefaults.getStringDefaultValue(DefaultFields.GRP_DESCRIPTION).getStringValue();
		group += "]]></GROUP>";
		
		return group;
	}
	
	private String getRecordTag(TridasValues tvsgroup, TridasToPast4Defaults defaults, Integer parentIndex)
	{
		String record;
		boolean skipThisGroup = false;
		
		// Check we can handle this variable
		if(tvsgroup.isSetVariable())
		{
			if (!tvsgroup.getVariable().isSetNormalTridas())
			{
				this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
			}
			else
			{
				switch(tvsgroup.getVariable().getNormalTridas())
				{
				case RING_WIDTH:
				case EARLYWOOD_WIDTH:
				case LATEWOOD_WIDTH:
					// All handled ok
					
					break;
				default:
					// All other variables not representable
					this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().toString().toLowerCase().replace("_", " "))));
					skipThisGroup = true;
				}
			}
		}
		
		// Dodgy variable so skip
		if(skipThisGroup) return null;
		
		
		defaults.populateFromTridasValues(tvsgroup);
		
		
		
		record  = "<RECORD ";
		
		// Mandatory attributes
		record += "Keycode=\""+defaults.getStringDefaultValue(DefaultFields.KEYCODE).getValue()+"\" ";
		record += "Length=\""+defaults.getIntegerDefaultValue(DefaultFields.LENGTH).getValue()+"\" ";
		record += "Offset=\""+defaults.getIntegerDefaultValue(DefaultFields.OFFSET).getValue()+"\" ";
		record += "Owner=\""+parentIndex+"\" ";
		
		// Optional attributes
		if(defaults.getPast4BooleanDefaultValue(DefaultFields.PITH).getValue()!=null)
		{
			record += "Pith=\""+defaults.getPast4BooleanDefaultValue(DefaultFields.PITH).getStringValue()+"\" ";
		}
		if(defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD).getValue()!=null)
		{
			record += "Sapwood=\""+defaults.getIntegerDefaultValue(DefaultFields.SAPWOOD).getStringValue()+"\" ";
		}
		if(defaults.getStringDefaultValue(DefaultFields.LOCATION).getValue()!=null)
		{
			record += "Location=\""+defaults.getStringDefaultValue(DefaultFields.LOCATION).getStringValue()+"\" ";
		}
		if(defaults.getStringDefaultValue(DefaultFields.SPECIES).getValue()!=null)
		{
			record += "Species=\""+defaults.getStringDefaultValue(DefaultFields.SPECIES).getStringValue()+"\" ";
		}
		
		record += ">\n";
		record += "<HEADER><![CDATA[";
		
		if(tvsgroup.isSetUnit())
		{
			if(tvsgroup.getUnit().isSetNormalTridas())
			{
				record += "Unit="+tvsgroup.getUnit().getNormalTridas().value()+"\n";
			}
			else if (tvsgroup.getUnit().isSetNormal())
			{
				record += "Unit="+tvsgroup.getUnit().getNormal()+"\n";
			}
			else if (tvsgroup.getUnit().isSetValue())
			{
				record += "Unit="+tvsgroup.getUnit().getValue()+"\n";
			}
		}
		else if (tvsgroup.isSetUnitless())
		{
			record += "Unit=Unitless\n";
		}
		record += "]]></HEADER>\n";
		record += "<DATA><![CDATA[";
		
		
		
		for (TridasValue val: tvsgroup.getValues())
		{
			// Values are tab delimited as:
			// - Ring width
			// - Number of samples used for mean calc
			// - Number of samples ascending growth trend
			// - Latewood percentage (0-1) 
			// - Backup ring width
			// - Ring information
			record+=val.getValue()+"\t1\t1\t0\t"+val.getValue()+"\t";
			
			// Remarks
			for(TridasRemark remark : val.getRemarks())
			{
				if(remark.isSetNormalTridas())
				{
					record+= remark.getNormalTridas().toString().toLowerCase()+"; ";
				}
				else if (remark.isSetNormal())
				{
					record+= remark.getNormal()+"; ";
				}
				else if (remark.isSetValue())
				{
					record+= remark.getValue()+"; ";
				}
			}
			record+="\n";
		}
		
		record += "]]></DATA>\n";
		record += "</RECORD>";
		
		return record;
	}
}
