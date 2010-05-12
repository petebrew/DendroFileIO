package org.tridas.io;

public class DendroFormatInfo {

	private final String baseTag;
	
	/**
	 * Constructor for the Dendro Format Info class.   All the info is stored 
	 * in the TextBundle file e.g. thisfileformat.about.shortName, 
	 * thisfileformat.about.description etc where 'thisfileformat' is the 
	 * i18nBaseTag provided. 
	 *  
	 * @param i18nBaseTag
	 */
	public DendroFormatInfo(String i18nBaseTag){
		baseTag = i18nBaseTag;
	}
	
	/**
	 * @return the baseTag used for the locale file
	 */
	public String getBaseTag() {
		return baseTag;
	}

	/**
	 * Return the name of the format. This is typically a single word
	 * used to refer to this format.
	 * 
	 * @return
	 */
	public String getShortName()
	{
		String val = null;
		try{
		val = I18n.getText(baseTag+".about.shortName");
		}
		catch (Exception e){}
		
		return val;
	}
	
	/**
	 * Get the full descriptive name for this format
	 * 
	 * @return
	 */
	public String getFullName()
	{
		String val = null;
		try{
		val = I18n.getText(baseTag+".about.fullName");
		}
		catch (Exception e){}
		
		return val;
	}
	
	/**
	 * Get a description of this format
	 * 
	 * @return
	 */
	public String getDescription()
	{
		String val = null;
		try{
		val = I18n.getText(baseTag+".about.description");
		}
		catch (Exception e){}
		
		return val;
	}
	
	/**
	 * Get the text that should appear in the save file dialog 
	 * filter.
	 * 
	 * @return
	 */
	public String getFileSaveFilterText(String argExtension)
	{
		String name = this.getFullName();
		String ext = argExtension;
		String val = null;
		
		if (name!=null)
		{
			val = name;
			
			if (ext!=null)
			{
				val += " (*."+ext.toLowerCase()+")";
			}
			else
			{
				val += " (*.*)";
			}
		}
		
		return val;
	}
	
}
