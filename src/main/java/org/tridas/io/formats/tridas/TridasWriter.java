package org.tridas.io.formats.tridas;

import java.util.ArrayList;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.util.TridasHierarchyHelper;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * Writer for the TRiDaS file format.  This is little more than a
 * wrapper around the JaXB marshaller
 * 
 * @see org.tridas.io.formats.tridas
 * @author peterbrewer
 */
public class TridasWriter extends AbstractDendroCollectionWriter {
	
	private INamingConvention naming = new HierarchicalNamingConvention();

	/**
	 * Constructor for the writer that creates TRiDaS XML files from
	 * TRiDaS java classes. 
	 */
	public TridasWriter(){
		super(TridasMetadataFieldSet.class);
	}
	
	@Override
	public void parseTridasProject(TridasProject p, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
		
		if(p==null){
			throw new IncompleteTridasDataException("Project is null!");
			
		}
		
		TridasFile file = new TridasFile(this);
		
		file.setProject(p);
		TridasProject project = p;
		TridasObject object = null;
		TridasElement element = null;
		TridasSample sample = null;
		TridasRadius radius = null;
		TridasMeasurementSeries series = null;
		
		ArrayList<TridasObject> objects = TridasHierarchyHelper.getObjectList(project);
		if(objects.size() == 1){
			object = objects.get(0);
			
			ArrayList<TridasElement> elements = TridasHierarchyHelper.getElementList(object);
			if(elements.size() == 1){
				element = elements.get(0);
				
				if(element.getSamples().size() == 1){
					sample = element.getSamples().get(0);
					
					if(sample.getRadiuses().size() == 1){
						radius = sample.getRadiuses().get(0);
						
						if(radius.getMeasurementSeries().size() == 1){
							series = radius.getMeasurementSeries().get(0);
						}
					}
				}
			}
		}
		
		naming.registerFile(file, project, object, element, sample, radius, series);
		this.addToFileList(file);
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
		return null;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("tridas.about.description");
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("tridas.about.fullName");
	}

	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("tridas.about.shortName");
	}
}
