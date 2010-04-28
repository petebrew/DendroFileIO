package org.tridas.io.formats.heidelberg;

import java.util.ArrayList;
import java.util.HashMap;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarning.WarningType;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;

public class HeidelbergReader extends AbstractDendroFileReader {

private static final SimpleLogger log = new SimpleLogger(HeidelbergReader.class);
	
	private TridasProject project = null;
	private HeidelbergToTridasDefaults defaults = null;
	private TridasToHeidelbergDefaults fileMetadata = null;
	
	static {
		TridasIO.registerFileReader(HeidelbergReader.class);
	}
	
	public HeidelbergReader() {
		super(HeidelbergToTridasDefaults.class);
	}

	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) {
		
		defaults = (HeidelbergToTridasDefaults) argDefaultFields;
		fileMetadata = new TridasToHeidelbergDefaults();
		
		initializeProject();
		
		int lineNum = 0;
		int fileLength = argFileString.length;
		if(argFileString[lineNum].trim().equals("HEADER:")){
			lineNum++;
		}
		
		// HEADER
		
		ArrayList<String> header = new ArrayList<String>();
		String line = argFileString[lineNum];
		while(!line.startsWith("DATA")){
			header.add(line);
			lineNum++;
			if(lineNum==fileLength){
				log.error(I18n.getText("heidelberg.meta.error"));
				//addWarningToList(new ConversionWarning(WarningType., warningMessage))
				// TODO add warning of data not found
				// TODO split up metadata warnings and general file conversion warnings
				break;
			}
		}
		processHeader(header.toArray(new String[0]));
		
		// DATA
		lineNum++; // we're still on the "DATA" line, so move forward
		
		ArrayList<String> data = new ArrayList<String>();
		while(lineNum<fileLength){
			data.add(argFileString[lineNum]);
		}
		processData(data.toArray(new String[0]));
	}

	private void processHeader(String[] argHeader){
		HashMap<String,String> data = new HashMap<String, String>();
		for(String s: argHeader){
			String[] split = s.split("=");
			if(split.length != 2){
				// TODO locale
				new ConversionWarning(WarningType.INVALID, "Could not determine key and value", split[0]);
				continue;
			}
			data.put(split[0], split[1]);
		}
		
		//load all data
		for(String key :data.keySet()){
			fileMetadata.loadData(key, data.get(key));
		}
	}
	
	private void processData(String[] argData){
		TridasRadius radius = project.getObjects().get(0).getElements().get(0).getSamples().get(0).getRadiuses().get(0);
		
		
	}
	
	private void initializeProject() {
		project = defaults.getProjectWithDefaults(true);
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"fh"};
	}

	@Override
	public TridasProject getProject() {
		return project;
	}
}
