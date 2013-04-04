package org.tridas.io.formats.fhx2;

import java.util.Arrays;
import java.util.List;

import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;

public class FHX2Reader extends AbstractDendroFileReader {
	private Integer lineNumDataBegins = null;
	private FHX2ToTridasDefaults defaults = new FHX2ToTridasDefaults();
	private SafeIntYear startYear;
	private Integer numberOfSamples;
	private Integer codeLength;
	
	
	public FHX2Reader() {
		super(FHX2ToTridasDefaults.class);
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		
		
		for(int i=0; i<argFileString.length; i++)
		{
			String line = argFileString[i].trim();
			if(line.equalsIgnoreCase("FHX2 FORMAT") || line.equalsIgnoreCase("FIRE2 FORMAT"))
			{
				if(lineNumDataBegins!=null) throw new InvalidDendroFileException ("FHX2 FORMAT line found in file more than once.", i);
				lineNumDataBegins = i;
			}
			
		}
		
		if(lineNumDataBegins==null) throw new InvalidDendroFileException ("FHX2 FORMAT line not found in file");
		
		parseMetadata(argFileString);
		parseData(argFileString);
		
		

	}

	private String getMetadataValue(String line)
	{
		if(line==null || line.length()==0) return null;
		if(!line.contains(":")) return null;
		
		int ind = line.indexOf(":")+1;
		
		return line.substring(ind).trim();
		
		
	}
	
	private void parseMetadata(String[] argFileString) throws InvalidDendroFileException
	{
		Integer metadataLineNum = lineNumDataBegins+1;
		String[] parts = argFileString[metadataLineNum].split(" ");
		if(parts.length!=3) throw new InvalidDendroFileException("Metadata line does not contain start year, sample number and code length as expected", metadataLineNum);
		
		
		try{
			startYear = new SafeIntYear(parts[0]);
			numberOfSamples = Integer.parseInt(parts[1]);
			codeLength = Integer.parseInt(parts[2]);
		} catch (NumberFormatException e)
		{
			throw new InvalidDendroFileException("Metadata in unexpected format", metadataLineNum);
		}
		
		for(int i=0; i<lineNumDataBegins-2; i++)
		{
			String line = argFileString[i];
			
			if(line.startsWith("Name of site"))
			{
				String value = getMetadataValue(line);
				defaults.getStringDefaultValue(TridasMandatoryField.OBJECT_TITLE).setValue(value);
			}
			
			else if(line.startsWith("Site code"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Collection date"))
			{
				String value = getMetadataValue(line);
			}			

			
			else if(line.startsWith("Collectors"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Crossdaters"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Number samples"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Species name"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Common name"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Habitat type"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Country"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("State"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("County"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Park/Monument"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("National Forest"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Ranger district"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Township"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Range"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Section"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Quarter section"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("UTM easting"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("UTM northing"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Latitude"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Longitude"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Topographic map"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Lowest elev"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Highest elev"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Slope"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Aspect"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Area sampled"))
			{
				String value = getMetadataValue(line);
			}			
			
			else if(line.startsWith("Substrate type"))
			{
				String value = getMetadataValue(line);
			}			
			
			
		}
		

	}
	
	private void parseData(String[] argFileString) throws InvalidDendroFileException
	{
		
		for(int samplenumber=0; samplenumber<numberOfSamples; samplenumber++)
		{
			// Loop through sample columns
			
			String seriesname = "";
			String datachars = "";
			for(int i=lineNumDataBegins+2; i<lineNumDataBegins+2+codeLength; i++)
			{
				String line = argFileString[i];
				seriesname+= line.substring(samplenumber,samplenumber);				
			}
			
			for(int i=lineNumDataBegins+2+codeLength+2; i<argFileString.length; i++)
			{
				String line = argFileString[i];
				datachars+= line.substring(samplenumber,samplenumber);	
			}
			
			
		}
	}
	
	
	@Override
	protected void resetReader() {
		lineNumDataBegins = null;
		defaults = new FHX2ToTridasDefaults();

	}

	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"fhx"};
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("fhx2.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("fhx2.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("fhx2.about.shortName");
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	private TridasProject getProject(){
		return defaults.getProjectWithDefaults(true);
	}
	
	@Override
	public TridasProject[] getProjects() {
		TridasProject projects[] = new TridasProject[1];
		projects[0] = this.getProject();
		return projects;
	}

	@Override
	public TridasTridas getTridasContainer() {
		TridasTridas container = new TridasTridas();
		List<TridasProject> list = Arrays.asList(getProjects());
		container.setProjects(list);
		return container;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getDendroFileFilter()
	 */
	@Override
	public DendroFileFilter getDendroFileFilter() {

		String[] exts = new String[] {"fhx"};
		
		return new DendroFileFilter(exts, getShortName());

	}

}
