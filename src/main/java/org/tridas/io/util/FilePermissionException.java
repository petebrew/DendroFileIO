package org.tridas.io.util;

import java.io.File;

public class FilePermissionException extends Exception {

	private static final long serialVersionUID = 1L;
	private PermissionType type;
	private File file;
	
	
	public enum PermissionType{
		READ,
		WRITE;
	}
	
	public FilePermissionException(File file, PermissionType type)
	{
		this.type = type;
		this.file = file;
		
	}

	public PermissionType getType() {
		return type;
	}


	public File getFile() {
		return file;
	}
	
	@Override
	public String getLocalizedMessage()
	{
		return "Permission to "+type.toString().toLowerCase()+" to file "+file+" was denied.";
	
	}
	
	@Override
	public String getMessage()
	{
		return getLocalizedMessage();
	
	}


}
