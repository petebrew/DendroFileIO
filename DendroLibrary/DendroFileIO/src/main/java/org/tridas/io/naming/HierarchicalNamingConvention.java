/**
 * Created on Apr 22, 2010, 2:42:24 AM
 */
package org.tridas.io.naming;

import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * @author daniel
 */
public class HierarchicalNamingConvention extends AbstractNamingConvention {
	
	/**
	 * @see org.tridas.io.naming.AbstractNamingConvention#getDendroFilename(org.tridas.io.IDendroFile,
	 *      org.tridas.schema.TridasProject, org.tridas.schema.TridasObject,
	 *      org.tridas.schema.TridasElement, org.tridas.schema.TridasSample,
	 *      org.tridas.schema.TridasRadius, org.tridas.schema.TridasMeasurementSeries)
	 */
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries) {
		
		String name = "";
		
		if (argProject != null) {
			name += argProject.getTitle();
		}
		else {
			return name;
		}
		
		if (argObject != null) {
			name += "-" + argObject.getTitle();
		}
		else {
			return name;
		}
		
		if (argElement != null) {
			name += "-" + argElement.getTitle();
		}
		else {
			return name;
		}
		
		if (argSample != null) {
			name += "-" + argSample.getTitle();
		}
		else {
			return name;
		}
		
		if (argRadius != null) {
			name += "-" + argRadius.getTitle();
		}
		else {
			return name;
		}
		
		if (argSeries != null) {
			name += "-" + argSeries.getTitle();
		}
		else {
			return name;
		}
		
		return name;
	}
	
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasDerivedSeries argSeries) {
		String name = "";
		
		if (argProject != null) {
			name += argProject.getTitle();
		}
		else {
			return name;
		}
		
		if (argSeries != null) {
			name += "-" + argSeries.getTitle();
		}
		else {
			return name;
		}
		
		return name;
	}
	
	@Override
	public String getDescription() {
		return I18n.getText("namingconvention.hierarchical.description");
	}
	
	@Override
	public String getName() {
		return I18n.getText("namingconvention.hierarchical");
	}
	
}
