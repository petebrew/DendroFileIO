package org.tridas.io.maventests;

import java.util.ArrayList;

import org.tridas.io.IDendroFile;
import org.tridas.io.formats.tridas.TridasFile;
import org.tridas.io.formats.tucson.TucsonFile;
import org.tridas.io.naming.NumericalNamingConvention;

import junit.framework.TestCase;

public class NamingConventionsTest extends TestCase {
	
	public void testNumerical(){
		NumericalNamingConvention naming = new NumericalNamingConvention();
		ArrayList<IDendroFile> files = new ArrayList<IDendroFile>();
		files.add(new TridasFile(null));
		files.add(new TridasFile(null));
		files.add(new TridasFile(null));
		files.add(new TucsonFile(null, null));
		for(IDendroFile f : files){
			naming.registerFile(f, null, null, null, null, null, null);
		}
		
		for(int i=0; i<3; i++){
			assertEquals(files.get(i).getClass().getSimpleName()+"("+(i+1)+")", naming.getFilename(files.get(i)));
		}
		assertEquals(files.get(3).getClass().getSimpleName(), naming.getFilename(files.get(3)));
		
		naming.setBaseFilename("file");
		naming.clearRegisteredFiles();
		
		for(IDendroFile f : files){
			naming.registerFile(f, null, null, null, null, null, null);
		}
		
		for(int i=0; i<files.size(); i++){
			assertEquals("file("+(i+1)+")", naming.getFilename(files.get(i)));
		}
	}
}
