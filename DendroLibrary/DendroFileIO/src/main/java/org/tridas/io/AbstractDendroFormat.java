package org.tridas.io;

public abstract class AbstractDendroFormat implements Comparable<AbstractDendroFormat>{

	
	/**
	 * Returns a list of the file extensions for this file
	 * 
	 * @return
	 */
	public abstract String[] getFileExtensions();
	
	/**
	 * Get the short name of the format
	 * 
	 * @return
	 */
	public abstract String getShortName();
	
	/**
	 * Get the full name of the format
	 * 
	 * @return
	 */
	public abstract String getFullName();
	
	/**
	 * Get the description of the format
	 * 
	 * @return
	 */
	public abstract String getDescription();
	
	
	public DendroFileFilter getDendroFileFilter() {

		return new DendroFileFilter(getFileExtensions(), getShortName());

	}

	@Override
	public int compareTo(AbstractDendroFormat other) {
		if(this.getFullName().equals(other.getFullName()))
		{
			if(this.getDescription().equals(other.getDescription()))
			{
				return 0;
			}
			else
			{
				return this.getDescription().compareTo(other.getDescription());
			}
		}
		else 
		{
			return this.getFullName().compareTo(other.getFullName());
		}
	}
}
