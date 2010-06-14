package org.tridas.io.formats.windendro;

import java.util.ArrayList;

import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.warningsandexceptions.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

public class WinDendroReader extends AbstractDendroFileReader {

	Integer currentLineNumber = -1;
	
	public WinDendroReader() {
		super(WinDendroToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		return I18n.getText("windendro.about.description");
	}

	@Override
	public String[] getFileExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullName() {
		return I18n.getText("windendro.about.fullName");
	}

	@Override
	public TridasProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortName() {
		return I18n.getText("windendro.about.shortName");
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		checkFileIsValid(argFileString);

	}

	@Override
	protected void resetReader() {
		// TODO Auto-generated method stub

	}
	
	
	private void checkFileIsValid(String[] argFileString) throws InvalidDendroFileException{
		
		if(argFileString.length<3)
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.filetooshort"));
		}
		
		
		// Catch problems with the header line 1
		String[] headerParts = argFileString[0].split("\\t");
		if (headerParts.length!=8)
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidHeaderLine"), 1);
		}
		
		if (!headerParts[0].equalsIgnoreCase("WINDENDRO"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidHeaderWindendro"), 1);
		}
		
		if (!headerParts[1].equalsIgnoreCase("3") && !headerParts[1].equalsIgnoreCase("4"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidVersion", headerParts[1]), 1);
		}
		
		if (!headerParts[2].equalsIgnoreCase("R") )
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.onlyRowWiseSupported"), 1);
		}
		
		if (!headerParts[3].equalsIgnoreCase("13") && !headerParts[3].equalsIgnoreCase("36"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidVersion", I18n.getText("unknown")), 1);
		}
		
		if (!headerParts[4].equalsIgnoreCase("P") && !headerParts[4].equalsIgnoreCase("B"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidOrdering"), 1);
		}
		
		if (!headerParts[5].equalsIgnoreCase("I"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.incrementalOnly"), 1);
		}
		
		if (!headerParts[6].equalsIgnoreCase("Y") && !headerParts[6].equalsIgnoreCase("N"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidBarkYesNo"), 1);
		}
		
		if (!headerParts[7].equalsIgnoreCase("RING"))
		{
			throw new InvalidDendroFileException(I18n.getText("windendro.invalidHeaderRING"), 1);
		}
				
	}

}
