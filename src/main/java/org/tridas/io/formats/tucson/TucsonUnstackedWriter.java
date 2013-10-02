package org.tridas.io.formats.tucson;


public class TucsonUnstackedWriter extends TucsonWriter {

	public TucsonUnstackedWriter() {
		super(new TucsonUnstackedFormat());
		super.isstacked = false;
		
	}
		
	
}
