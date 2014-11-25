package org.tridas.io.util;

import java.util.HashSet;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.exceptions.ConversionWarning;

public class ConversionUtils {

	
	public static String formatConversionWarnings(AbstractDendroFileReader reader, AbstractDendroCollectionWriter writer)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Conversion log"+System.getProperty("line.separator"));
		sb.append("**************"+System.getProperty("line.separator"));
		sb.append(""+System.getProperty("line.separator"));
		sb.append("Converted        : "+reader.getOriginalFilename()+System.getProperty("line.separator"));
		sb.append("Input format     : "+reader.getFullName()+System.getProperty("line.separator"));
		sb.append("Output format    : "+writer.getFullName()+System.getProperty("line.separator"));
		sb.append("No. output files : "+writer.getFiles().length+System.getProperty("line.separator"));
		sb.append(""+System.getProperty("line.separator"));
		
		if(reader.getWarnings().length==0 && writer.getWarnings().length==0)
		{
			sb.append("File converted successfully with no warnings");
			return sb.toString();
		}
		
		sb.append("WARNING!!!"+System.getProperty("line.separator"));
		
		if(reader.getWarnings().length>0)
		{
			sb.append("There are warnings associated with reading the input file:"+System.getProperty("line.separator"));
			
			
			// put them all in a hash set first so no duplicates.
			HashSet<String> set = new HashSet<String>();
			
			for (ConversionWarning warning : reader.getWarnings()) {
				set.add(warning.toStringWithField());
			}
			
			for(String warning : set)
			{
				sb.append("  - "+warning+System.getProperty("line.separator"));
			}
			
			sb.append(""+System.getProperty("line.separator"));
		}
				
		if(writer.getWarnings().length>0)
		{
			sb.append("There are warnings associated with writing the output file:"+System.getProperty("line.separator"));
			
			// put them all in a hash set first so no duplicates.
			HashSet<String> set = new HashSet<String>();
			
			for (ConversionWarning warning : writer.getWarnings()) {
				set.add(warning.toStringWithField());
			}
			
			for(String warning : set)
			{
				sb.append("  - "+warning+System.getProperty("line.separator"));
			}
		}
		
		
		return sb.toString();
		
	}
	
}
