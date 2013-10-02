package org.tridas.io.formats.heidelberg;


public class HeidelbergUnstackedWriter extends HeidelbergWriter {

	
	public HeidelbergUnstackedWriter() {
		super(new HeidelbergUnstackedFormat());
		super.isstacked = false;
		
	}
	
}
