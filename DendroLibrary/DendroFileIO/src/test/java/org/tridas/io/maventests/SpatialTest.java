package org.tridas.io.maventests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.spatial.SpatialUtils;

import junit.framework.TestCase;

public class SpatialTest extends TestCase {
	private static final Logger log = LoggerFactory.getLogger(SpatialTest.class);

	public void testLatLongParsing() {
		
		
		
		
		String str = "79 16 46.08 W";
		
		
		try{
			Double d = SpatialUtils.parseLatLonFromHalfLatLongString(str);
			log.debug("Parsed string '"+str+"' to coordinate : "+d.toString());
		} catch (Exception e)
		{
			log.error("Failed to parse string '"+str+"'");
			log.error(e.getMessage());
			fail();
			
		}
		
		
		
	}
	
}
