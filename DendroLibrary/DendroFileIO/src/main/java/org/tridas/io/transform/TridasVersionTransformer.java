package org.tridas.io.transform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.util.FileHelper;
import org.tridas.io.util.IOUtils;
import org.tridas.io.util.ThreePartVersionCode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Converts TRiDaS files between various versions.  The transform is performed between one version and its successor (or predecessor for downgrades).  If the user
 * requests to upgrade across multiple versions, then a step-wise transform is performed for each step.  The transforms are performed by running the XML file with 
 * an XSL file. 
 * 
 * @author pbrewer
 *
 */
public class TridasVersionTransformer {

	private final static Logger log = LoggerFactory.getLogger(TridasVersionTransformer.class);
	

	public static void main(String[] args)
	{
		try {
			File output = new File("/tmp/output.xml");
			
			TridasVersionTransformer.transformTridas(
					new File("/home/pwb48/dev/java8/DendroFileIO/TestData/TRiDaS/Extensive.xml"), 
					TridasVersion.V_1_2_3,
					output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	



	public static File transformTridas(File inputFile, TridasVersion outputVersion, File outputFile) throws Exception {
	
		FileHelper fileHelper = new FileHelper(inputFile.getAbsolutePath());
		log.debug("loading file: " + inputFile.getAbsolutePath());
		String[] strings = null;
		try{
			if (TridasIO.getReadingCharset() != null) {
				strings = fileHelper.loadStrings(inputFile.getAbsolutePath(), TridasIO.getReadingCharset());
			}
			else {
				if (TridasIO.isCharsetDetection()) {
					strings = fileHelper.loadStringsFromDetectedCharset(inputFile.getAbsolutePath());
				}
				else {
					strings = fileHelper.loadStrings(inputFile.getAbsolutePath());
				}
			}
			if (strings == null) {
				throw new IOException(I18n.getText("fileio.loadfailed"));
			}
		} catch (UnsupportedEncodingException e){
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] transformedStrings = transformTridas(strings, outputVersion);
		
		IOUtils.saveStrings(outputFile, transformedStrings);
		return outputFile;
	}
	
	public static String[] transformTridas(String[] inputFileStrings, TridasVersion outputVersion) throws Exception {
				
		// Read the input file and determine it's current version
		TridasVersion inputVersion = getTridasVersionFromXMLStrings(inputFileStrings);
		if(inputVersion==null) 
		{
			throw new Exception("Unable to determine version of existing TRiDaS file so version transformation failed");
		}
		
		// Create a list of XSL file streams one for each step in the upgrade/downgrade process.
		ArrayList<InputStream> xsllist = new ArrayList<InputStream>();
		if(inputVersion.equals(outputVersion))
		{
			log.info("The input file is already v"+inputVersion.versionString+" so file will not be transformed");
			return inputFileStrings;
		}
		if(inputVersion.equals(TridasVersion.V_1_2_2) &&
				outputVersion.equals(TridasVersion.V_1_2_3))
		{		
			log.debug("User has requested to upgrade their file from v1.2.2 to v.1.2.3");
			InputStream xslstream = TridasIO.class.getResourceAsStream("/xslt/Upgrade1.2.2-to-1.2.3.xsl");
			xsllist.add(xslstream);
		}
		else if (inputVersion.equals(TridasVersion.V_1_2_3) &&
				outputVersion.equals(TridasVersion.V_1_2_2))
		{
			log.debug("User has requested to upgrade their file from v1.2.3 to v.1.2.2");
			InputStream xslstream = TridasIO.class.getResourceAsStream("/xslt/Downgrade1.2.3-to-1.2.2.xsl");
			xsllist.add(xslstream);
		}
		//TODO Add support for other steps
		else
		{			
			throw new Exception("Transforming from v"+inputVersion.versionString+" to v"+outputVersion.versionString+" is currently not supported");
		}

		// Run each XSL transform in turn
		String[] currentFileStrings = inputFileStrings;
		for(InputStream currentXSL : xsllist)
		{
			currentFileStrings = TridasVersionTransformer.xslTransform(currentFileStrings, currentXSL);
			if(currentFileStrings==null) throw new Exception("Failed to transform TRiDaS file");
		}
		
		// Return the converted file
		return currentFileStrings;
	}
	
	
	
	
	
		
	
	public static String[] xslTransform(String[] inputFileStrings, InputStream xslstream) {
		
		if(inputFileStrings==null || xslstream == null )
		{
			log.error("All parameters are required.  Unable to perform transform.");
			return null;
		}


		File outputFile = null;
		try {
			outputFile = File.createTempFile("tricycle", "transform");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outputFile.deleteOnExit();
			
		//factory.setNamespaceAware(true);
		//factory.setValidating(true);
		try {

			StringBuilder lines = new StringBuilder();
			for(String line: inputFileStrings)
			{
				lines.append(line);
			}
			
			
			Document document = null;
			document = loadXMLFromString(lines.toString());
			

			// Use a Transformer for output
			TransformerFactory tFactory = TransformerFactory.newInstance();
			StreamSource stylesource = new StreamSource(xslstream);
			Transformer transformer = tFactory.newTransformer(stylesource);

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new StringWriter());
			
			transformer.transform(source, result);
			String outstring = result.getWriter().toString();
			String[] outstrings = outstring.split("\\n");
			log.debug("Transformation complete!");
			return outstrings;
			
		} catch (TransformerConfigurationException tce) {
			// Error generated by the parser
			log.error("\n** Transformer Factory error");
			log.error("   " + tce.getMessage());

			// Use the contained exception, if any
			Throwable x = tce;

			if (tce.getException() != null) {
				x = tce.getException();
			}

			x.printStackTrace();
			return null;
		} catch (TransformerException te) {
			// Error generated by the parser
			log.error("\n** Transformation error");
			log.error("   " + te.getMessage());

			// Use the contained exception, if any
			Throwable x = te;

			if (te.getException() != null) {
				x = te.getException();
			}

			x.printStackTrace();
			return null;
		} catch (SAXException sxe) {
			// Error generated by this application
			// (or a parser-initialization error)
			Exception x = sxe;

			if (sxe.getException() != null) {
				x = sxe.getException();
			}

			x.printStackTrace();
			return null;
		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();
		} catch (IOException ioe) {
			// I/O error
			ioe.printStackTrace();
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
		
		return null;
	} 
	

	public static Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();

	    return builder.parse(new ByteArrayInputStream(xml.getBytes()));
	}
	
	/**
	 * Get the TRiDaS version number of the specified TRiDaS input file 
	 * 
	 * @param inputFile
	 * @return
	 */
	public static TridasVersion getTridasVersionFromXML(File inputFile)
	{
		
		FileHelper fileHelper = new FileHelper(inputFile.getAbsolutePath());
		log.debug("loading file: " + inputFile.getAbsolutePath());
		String[] strings = null;
		try{
			if (TridasIO.getReadingCharset() != null) {
				strings = fileHelper.loadStrings(inputFile.getAbsolutePath(), TridasIO.getReadingCharset());
			}
			else {
				if (TridasIO.isCharsetDetection()) {
					strings = fileHelper.loadStringsFromDetectedCharset(inputFile.getAbsolutePath());
				}
				else {
					strings = fileHelper.loadStrings(inputFile.getAbsolutePath());
				}
			}
			if (strings == null) {
				throw new IOException(I18n.getText("fileio.loadfailed"));
			}
		} catch (UnsupportedEncodingException e){
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getTridasVersionFromXMLStrings(strings);
		
		
	}
	
	public static TridasVersion getTridasVersionFromXMLStrings(String[] lines)
	{

		if(lines==null) return null;
		
		StringBuilder fileString = new StringBuilder();
		Boolean firstLine = true;
		for (String s : lines) {
			if(firstLine)
			{
				fileString.append(s.replaceFirst("^[^<]*", "")+"\n");
				firstLine = false;
			}
			else
			{
				fileString.append(s + "\n");
			}
		}
		
		
		
		String regex = null;
		Pattern p1;
		Matcher m1;
		regex = "http://www.tridas.org/[\\d.]*";
		p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		String xmlfile = fileString.toString();
		m1 = p1.matcher(xmlfile);
		if (m1.find()) 
		{
			String versionstr = xmlfile.substring(m1.start(), m1.end());
			String v2 = versionstr.substring(versionstr.lastIndexOf("/")+1);
			return TridasVersion.getTridasVersionFromCodeString(v2);
		}
		else
		{
			return null;
		}
		
	}
	
	
	public enum TridasVersion{
		V_1_0(1, "1.0"),
		V_1_1(2, "1.1"),
		V_1_2(3, "1.2"),
		V_1_2_1(4, "1.2.1"),
		V_1_2_2(5, "1.2.2"),
		V_1_2_3(6, "1.2.3"),
		V_FUTURE(9999999, "Unknown");
		
		private int sequence;
		private String versionString;
		
		TridasVersion(int sequence, String versionString)
		{
			this.sequence = sequence;
			this.versionString = versionString;
		}
		
		public static TridasVersion getTridasVersionFromCode(ThreePartVersionCode code)
		{
			if(code.getFullVersionString().equals("1.0")) return TridasVersion.V_1_0;
			if(code.getFullVersionString().equals("1.1")) return TridasVersion.V_1_1;
			if(code.getFullVersionString().equals("1.2")) return TridasVersion.V_1_2;
			if(code.getFullVersionString().equals("1.2.1")) return TridasVersion.V_1_2_1;
			if(code.getFullVersionString().equals("1.2.2")) return TridasVersion.V_1_2_2;
			if(code.getFullVersionString().equals("1.2.3")) return TridasVersion.V_1_2_3;
			
			ThreePartVersionCode latestCode = new ThreePartVersionCode(TridasVersion.V_1_2_3.versionString);
			
			if(code.compareTo(latestCode)>0) return TridasVersion.V_FUTURE;
						
			return null;
		}
		
		public static TridasVersion getTridasVersionFromCodeString(String str)
		{
			try{
				ThreePartVersionCode code = new ThreePartVersionCode(str);
				return getTridasVersionFromCode(code);

			} catch (Exception e)
			{
				return null;
			}
			
		}
		
		public int getSequence()
		{
			return sequence;
		}
		
		public String getVersionString()
		{
			return versionString;
		}	
	}
}
