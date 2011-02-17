/*******************************************************************************
 * Copyright 2011 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
