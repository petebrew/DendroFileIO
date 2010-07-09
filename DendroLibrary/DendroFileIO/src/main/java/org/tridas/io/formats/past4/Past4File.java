package org.tridas.io.formats.past4;

import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;

public class Past4File implements IDendroFile {

	private final static SimpleLogger log = new SimpleLogger(Past4File.class);

	
	private String project;
	private ArrayList<String> groups = new ArrayList<String>();
	private ArrayList<String> records = new ArrayList<String>();

	private IMetadataFieldSet defaults;
	
	public Past4File(IMetadataFieldSet defaults) {
		this.defaults = defaults;
	}
	
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	@Override
	public String getExtension() {
		return "P4P";
	}

	@Override
	public ITridasSeries[] getSeries() {
		return null;
	}

	public void setPast4Project(String proj)
	{
		project = proj;
	}
	
	public void setPast4Groups(ArrayList<String> grps)
	{
		groups = grps;
	}
	
	public void setPast4Records(ArrayList<String> recs)
	{
		records = recs;
	}
	
	@Override
	public String[] saveToString() {
		
		ArrayList<String> file = new ArrayList<String>();
		
		file.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<PAST_4_PROJECT_FILE>");
		file.add(project);
		
		for (String grp : groups)
		{
			file.add(grp);
		}
		
		for (String rec : records)
		{
			file.add(rec);
		}
		
		file.add("</PAST_4_PROJECT_FILE>");
		
		return file.toArray(new String[0]);
	}

}
