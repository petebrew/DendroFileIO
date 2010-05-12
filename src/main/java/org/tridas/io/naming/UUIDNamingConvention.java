package org.tridas.io.naming;

import java.util.UUID;

import org.tridas.io.IDendroFile;
import org.tridas.io.I18n;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * Produces a universally unique file names 
 * 
 * @author peterbrewer
 *
 */
public class UUIDNamingConvention extends AbstractNamingConvention {
	
	@Override
	protected String getDendroFilename(IDendroFile argFile,
			TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample,
			TridasRadius argRadius, TridasMeasurementSeries argSeries) {

		return UUID.randomUUID().toString();
	}
	
	@Override
	public String getDescription() {
		return I18n.getText("namingconvention.uuid.description");
	}
	
	@Override
	public String getName(){
		return I18n.getText("namingconvention.uuid");
	}

}
