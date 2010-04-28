/**
 * Created on Apr 22, 2010, 1:45:27 AM
 */
package org.tridas.io.naming;

import org.tridas.io.DendroFile;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * @author daniel
 *
 */
public interface INamingConvention {

	public void registerFile(DendroFile argFile,
							 TridasProject argProject,
							 TridasObject argObject,
							 TridasElement argElement,
							 TridasSample argSample,
							 TridasRadius argRadius,
							 TridasMeasurementSeries argSeries);
	
	public String getFilename(DendroFile argFile);
	
	public String getDescription();
	
	public String getName();
}
