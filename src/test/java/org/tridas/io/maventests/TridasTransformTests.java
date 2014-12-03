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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.transform.TridasVersionTransformer;
import org.tridas.io.transform.TridasVersionTransformer.TridasVersion;
import org.tridas.io.util.FileHelper;

public class TridasTransformTests extends TestCase {
	private static final Logger log = LoggerFactory.getLogger(TridasTransformTests.class);

	public void testStringResizing() {
		String inputFile = "/home/pwb48/dev/java8/DendroFileIO/TestData/TRiDaS/1.2.3.xml";
		TridasVersion outputVersion = TridasVersion.V_1_2_2;
		
		
		FileHelper fileHelper = new FileHelper(inputFile);
		log.debug("loading file: " + inputFile);
		String[] strings = null;
		try{
			if (TridasIO.getReadingCharset() != null) {
				strings = fileHelper.loadStrings(inputFile, TridasIO.getReadingCharset());
			}
			else {
				if (TridasIO.isCharsetDetection()) {
					strings = fileHelper.loadStringsFromDetectedCharset(inputFile);
				}
				else {
					strings = fileHelper.loadStrings(inputFile);
				}
			}
			if (strings == null) {
				throw new IOException(I18n.getText("fileio.loadfailed"));
			}
			
			TridasVersionTransformer.transformTridas(strings, outputVersion);

			
		} catch (UnsupportedEncodingException e){
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		
	}
}
