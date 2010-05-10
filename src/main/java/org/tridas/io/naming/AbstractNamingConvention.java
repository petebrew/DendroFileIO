/**
 * Created on Apr 22, 2010, 1:49:56 AM
 */
package org.tridas.io.naming;

import java.util.ArrayList;
import java.util.HashMap;

import org.grlea.log.SimpleLogger;
import org.tridas.io.DendroFile;
import org.tridas.io.I18n;
import org.tridas.io.util.StringUtils;
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
public abstract class AbstractNamingConvention implements INamingConvention{

	private static final SimpleLogger log = new SimpleLogger(AbstractNamingConvention.class);
	public static String DEFAULT_FILENAME = "unknown";
	
	private HashMap<String, ArrayList<DendroFile>> nameMap = new HashMap<String, ArrayList<DendroFile>>();
	private HashMap<DendroFile, String> fileMap = new HashMap<DendroFile, String>();
	/**
	 * @see org.tridas.io.naming.INamingConvention#registerFile(org.tridas.io.DendroFile, org.tridas.schema.TridasProject, org.tridas.schema.TridasObject, org.tridas.schema.TridasElement, org.tridas.schema.TridasSample, org.tridas.schema.TridasRadius, org.tridas.schema.TridasMeasurementSeries)
	 */
	@Override
	public synchronized void registerFile(DendroFile argFile, TridasProject argProject,
			TridasObject argObject, TridasElement argElement,
			TridasSample argSample, TridasRadius argRadius,
			TridasMeasurementSeries argSeries) {
		
		String filename = getDendroFilename(argFile, argProject, argObject, argElement, argSample, argRadius, argSeries);
		if(filename == null){
			log.error(I18n.getText("fileio.usingDefaultFilename", DEFAULT_FILENAME)); 
			filename = DEFAULT_FILENAME;
		}
		
		if(!nameMap.containsKey(filename)){
			nameMap.put(filename, new ArrayList<DendroFile>());
		}
		
		ArrayList<DendroFile> files = nameMap.get(filename);
		files.add(argFile);
		fileMap.put(argFile, filename);
	}
	
	protected abstract String getDendroFilename(DendroFile argFile, TridasProject argProject,
			TridasObject argObject, TridasElement argElement,
			TridasSample argSample, TridasRadius argRadius,
			TridasMeasurementSeries argSeries);

	
	public synchronized void clearRegisteredFiles(){
		nameMap.clear();
		fileMap.clear();
	}
	
	/**
	 * @see org.tridas.io.naming.INamingConvention#getFilename(org.tridas.io.DendroFile)
	 */
	@Override
	public synchronized String getFilename(DendroFile argFile) {
		String baseFilename = fileMap.get(argFile);
		ArrayList<DendroFile> files = nameMap.get(baseFilename);
		if(files == null || files.size() == 0){
			log.error(I18n.getText("fileio.fileNotRegistered")); 
		}
		
		if(files.size() == 1){
			return baseFilename;
		}
		
		int numDigits = (files.size()+"").length();
		
		int i;
		for(i=0; i<files.size(); i++){
			if(files.get(i) == argFile){
				break;
			}
		}
		if(i == files.size()){
			log.error(I18n.getText("fileio.fileNotFound", baseFilename)); 
		}
		i++; // so we start at 1
		return baseFilename+StringUtils.addLefthandZeros(i, numDigits);
	}

	protected static class DendoFileInfo{
		DendroFile file;
		String filename;
	}
	
	public abstract String getDescription();
	
	public abstract String getName();
}
