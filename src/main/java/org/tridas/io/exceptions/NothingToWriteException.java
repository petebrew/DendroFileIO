package org.tridas.io.exceptions;

/**
 * This exception is thrown when a file writer cannot write to disk, typically because the format 
 * is unable to handle the data provided.  Calls to the writer's getWarnings() method should provide
 * a detailed message explaining what is missing.
 * 
 * @author pbrewer
 *
 */
public class NothingToWriteException extends Exception {

	private static final long serialVersionUID = 1L;

	public NothingToWriteException()
	{
		super("This format writer has no files to write to disk");
	}
}
