package org.tridas.io.util;

public class LinkSeriesHandler {

	

	public static String getXPathForObjectSearch(String domain, String id)
	{
		String xpath = "//object/identifier[text()=\""+id+"\" and @domain=\""+domain+"\"]";
		
		return xpath;

	}

}


