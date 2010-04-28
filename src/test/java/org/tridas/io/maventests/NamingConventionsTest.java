package org.tridas.io.maventests;

import java.util.ArrayList;

import org.tridas.io.DendroFile;
import org.tridas.io.formats.heidelberg.HeidelbergFile;
import org.tridas.io.formats.tridas.TridasFile;
import org.tridas.io.formats.tucson.TucsonFile;
import org.tridas.io.naming.NumericalNamingConvention;

import junit.framework.TestCase;

public class NamingConventionsTest extends TestCase {
	
	public void testNumerical(){
		NumericalNamingConvention naming = new NumericalNamingConvention();
		ArrayList<DendroFile> files = new ArrayList<DendroFile>();
		files.add(new TridasFile());
		files.add(new TucsonFile());
		files.add(new HeidelbergFile());
		for(DendroFile f : files){
			naming.registerFile(f, null, null, null, null, null, null);
		}
		
		for(int i=0; i<files.size(); i++){
			assertEquals((i+1)+"", naming.getFilename(files.get(i)));
		}
		
		naming.setBaseFilename("file");
		naming.clearRegisteredFiles();
		
		for(DendroFile f : files){
			naming.registerFile(f, null, null, null, null, null, null);
		}
		
		for(int i=0; i<files.size(); i++){
			assertEquals("file"+(i+1), naming.getFilename(files.get(i)));
		}
	}
}
