package org.tridas.io.util;

/**
 * Class to represent the typical 1.1.1 style version code for software, XML schemas etc.
 * 
 * @author pwb48
 *
 */
public class ThreePartVersionCode implements Comparable<ThreePartVersionCode>{

	Integer major;
	Integer minor;
	Integer revision;
	
	
	public ThreePartVersionCode(String s) throws NumberFormatException
	{
		if(s==null) throw new NumberFormatException("Version codes must be of format: 1; 1.1; or 1.1.1");
		
		String[] parts = s.split("\\.");
		
		if(parts.length>3 || parts.length<1)  throw new NumberFormatException("Version codes must be of format: 1; 1.1; or 1.1.1");
			
		try{
			major = Integer.parseInt(parts[0]);
			
			if(parts.length>1)
			{
				minor = Integer.parseInt(parts[1]);
			}
			
			if(parts.length>2)
			{
				revision = Integer.parseInt(parts[2]);
			}
			
		} catch (NumberFormatException e)
		{
			throw new NumberFormatException("Version codes must be of format: 1; 1.1; or 1.1.1");
		}

	}

	public void setMajorVersionNumber(Integer i)
	{
		major = i;
	}
	

	public void setMinorVersionNumber(Integer i)
	{
		minor = i;
	}
	
	public void setRevisionNumber(Integer i)
	{
		revision = i;
	}
	
	public Integer getMajorVersionNumber()
	{
		return major;
	}
	
	public Integer getMinorVersionNumber()
	{
		return minor;
	}
	
	public Integer getRevisionVersionNumber()
	{
		return revision;
	}
	
	public Integer getMajorVersionNumberOrZero()
	{
		if(major==null) return 0;
		
		return major;
	}
	
	public Integer getMinorVersionNumberOrZero()
	{
		if(minor==null) return 0;
		
		return minor;
	}
	
	public Integer getRevisionVersionNumberOrZero()
	{
		if(revision==null) return 0;
		
		return revision;
	}
	
	
	
	public boolean hasMajorVersionNumber()
	{
		return major!=null;
	}
	
	public boolean hasMinorVersionNumber()
	{
		return minor!=null;
	}
	
	public boolean hasRevisionVersionNumber()
	{
		return minor!=null;
	}
	
	public String getFullVersionString()
	{
		String v = major.toString();
		
		if(minor!=null) v+="."+minor.toString();
		if(revision!=null) v+="."+revision.toString();

		return v;
	}


	@Override
	public int compareTo(ThreePartVersionCode other) {

		if(other == null) throw new NullPointerException();

		if(getMajorVersionNumberOrZero().compareTo(other.getMajorVersionNumberOrZero())!=0) 
		{
			// Major versions are different so return 
			return major.compareTo(other.getMajorVersionNumber());
		}
		else
		{
			if(getMinorVersionNumberOrZero().compareTo(other.getMinorVersionNumberOrZero())!=0) 
			{
				return minor.compareTo(other.getMinorVersionNumber());
			}
			else
			{
				if(getRevisionVersionNumberOrZero().compareTo(other.getRevisionVersionNumberOrZero())!=0) 
				{
					return minor.compareTo(other.getRevisionVersionNumber());
				}
				else
				{
					return 0;
				}
			}
		}
		
	}
}
