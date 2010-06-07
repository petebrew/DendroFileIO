package org.tridas.io.naming;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

public class SeriesNamingConvention extends AbstractNamingConvention {
	
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries) {
		
		return getDendroFilename(argSeries);
	}
	
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasDerivedSeries argSeries) {
		
		return getDendroFilename(argSeries);
		
	}
	
	private String getDendroFilename(ITridasSeries argSeries) {
		String name = "";
		
		if (argSeries != null) {
			name += argSeries.getTitle();
		}
		else {
			return I18n.getText("fileio.defaultFilenameBase");
		}
		
		return name;
	}
	
	@Override
	public String getDescription() {
		return I18n.getText("namingconvention.series.description");
	}
	
	@Override
	public String getName() {
		return I18n.getText("namingconvention.series");
	}
	
}
