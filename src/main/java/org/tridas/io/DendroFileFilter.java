package org.tridas.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;


public class DendroFileFilter extends FileFilter implements Comparable<DendroFileFilter>{

	private final String[] okFileExtensions;
	private final String name;
	private final String anyFile= "*.*";
	
	public DendroFileFilter(String[] okFileExtensions, String formatName)
	{
		this.okFileExtensions = okFileExtensions;
		this.name = formatName;
	}
	
	@Override
	public boolean accept(File file)
	{
		if(file.isFile())
		{
			for (String extension : okFileExtensions)
			{
				if(extension.equals(anyFile))
				{
					return true;
				}
	    	
				if (file.getName().toLowerCase().endsWith(extension.replace("*", "")))
				{
					return true;
				}
			}
			return false;
		}
		else if (file.isDirectory())
		{
			return true;
		}
		return false;
	}

	/**
	 * Get a string containing a comma delimited list of acceptable file
	 * extensions.  Useful for including in file dialog box file filter 
	 * combo.
	 * 
	 * @return
	 */
	public String getAcceptableExtensions()
	{
		String ext = "";
		
		for (String extension : okFileExtensions)
		{
			if(extension.equals(anyFile))
			{
				return anyFile;
			}
			
			ext = ext+"."+extension+"; ";
    	
		}
		
		return ext.substring(0, ext.length()-2);
		
	}

	@Override
	public String getDescription() {
		return name + " (" + getAcceptableExtensions() +")";
	}

	@Override
	public int compareTo(DendroFileFilter o) {
		return this.name.compareTo(o.name);
	}
	
	public String getFormatName()
	{
		return name;
	}

}
