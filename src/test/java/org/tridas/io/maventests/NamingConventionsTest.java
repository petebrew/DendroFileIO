/**
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tridas.io.maventests;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.tridas.io.IDendroFile;
import org.tridas.io.formats.tridas.TridasFile;
import org.tridas.io.formats.tucson.TucsonFile;
import org.tridas.io.naming.NumericalNamingConvention;

public class NamingConventionsTest extends TestCase {
	
	public void testNumerical() {
		NumericalNamingConvention naming = new NumericalNamingConvention();
		ArrayList<IDendroFile> files = new ArrayList<IDendroFile>();
		files.add(new TridasFile(null));
		files.add(new TridasFile(null));
		files.add(new TridasFile(null));
		files.add(new TucsonFile(null));
		for (IDendroFile f : files) {
			naming.registerFile(f, null, null, null, null, null, null);
		}
		
		for (int i = 0; i < 3; i++) {
			assertEquals(files.get(i).getClass().getSimpleName() + "(" + (i + 1) + ")", naming
					.getFilename(files.get(i)));
		}
		assertEquals(files.get(3).getClass().getSimpleName(), naming.getFilename(files.get(3)));
		
		naming.setBaseFilename("file");
		naming.clearRegisteredFiles();
		
		for (IDendroFile f : files) {
			naming.registerFile(f, null, null, null, null, null, null);
		}
		
		for (int i = 0; i < files.size(); i++) {
			assertEquals("file(" + (i + 1) + ")", naming.getFilename(files.get(i)));
		}
	}
}
