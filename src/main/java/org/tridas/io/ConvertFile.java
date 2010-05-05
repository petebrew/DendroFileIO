package org.tridas.io;

import java.io.IOException;

import org.grlea.log.SimpleLog;
import org.tridas.io.warnings.ConversionWarning;
import org.tridas.io.warnings.ConversionWarningException;
import org.tridas.io.warnings.IncompleteTridasDataException;
import org.tridas.io.warnings.IncorrectDefaultFieldsException;
import org.tridas.io.warnings.InvalidDendroFileException;
import org.tridas.schema.TridasProject;

public class ConvertFile {
	
	static String asciilogo = "???????????????????????????????????????????????????????????????????????????????????????????????????\n"+
	"???????????????????????????????????????????????????????????????????????????????????????????????????\n"+
	"??????????????????????????????????::~,,~:==,~=++?+?+???????????????????????????????????????????????\n"+
	"???????????????????????????????+=.,:=::=~:~,:~~=~:,:=+?????????????????????????????????????????????\n"+
	"????????????????????????????=::.~~,~,:~,:~===~:.~~.=,:+????????????????????????????????????????????\n"+
	"?????????????????????????????==:.~,..,:~:,.=.~.,~~~=~,:~=~~++??????????????????????????????????????\n"+
	"????????????????????????=++,,~:........:.,,,:~.........~:+++:??????????????????????????????????????\n"+
	"II?III???IIIIIIII?I::~.+,~.,,...~..:...::.,.:,,.:~,,+,~===~+=:+I???????????????????????????????????\n"+
	"IIIIIIIIIIIIIIIII?~~,:~=....,:~..,.....,~..,..,:.~==,.,~,:~=+?:?~.:.???I???????????????????????????\n"+
	"IIIIIIIIIIIIIIII?~~:=..:~~:~..=.::,,.~.,......:.,=,?~..,~,~~~~+=,~.+?.,+???????????????????????????\n"+
	"IIIIIIIIIIIIIII++=:,.,~:....:,...,=:,.~,:~+:.~...,,,.,.,~.,~~:,:~,,=::+~?IIIII?????????????????????\n"+
	"IIIIIIIIIIIII=~.....:..........=~~,,:~...,,..~,:~::~~,+..=~~:.~~.~.~=.:?IIIIIIIIIIIIIIII???????????\n"+
	"IIIIIIIIIIII,~:::..,.:,,:~,....,,=~:..+~,.~~:.~~:~~.:.,:+.:,,.,:~:,::~::+=IIIIIIIIIIIIIIIIIII???III\n"+
	"IIIIIIIIIII::~.=....::...=:~:::~~~:.=.~:=~.:..,...~...,,.,:..,.~,=::~=:+?IIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"IIIIIIIIIII~:..,?:,:...,~=~::::.,....=:.,.,,.,,,....~:~,~=.,...,:~,.~::=?IIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"IIIIIIIIIII+.:~,......,,..,.,.~,:~,=,,....~~.,...~=,:.~,::~~~:.~.:.,.,~~IIIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"IIIIIIIIIIII=?::,.....,~=.=,.:::=~:=:~,.....,,,..,,,.,,.~=.:~,,,,.:,::~~IIIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"IIIIIIII:::~,=~,,.::,,,,~.??..,....,..:~,.:?=.:~..,,,~,..:~,=.~==?==I:~:~~IIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"IIIIIII~:...:..,=I,.......,?I,.,:..~~.~.:.,.,...~.~.:,:..,,,~:::.,,.~,~:,=:?IIIIIIIIIIIIIIIIIIIIIII\n"+
	"IIIIII=,~:,,.,..,..,~=..,.:=.,.....,,...,=........,.,.,~,~=~..,.:=~..,.~..~IIIIIIIIIIIIIIIIIIIIIIII\n"+
	"IIIII?:+7+I,:,::.++.....,::,.,,....,.,.=.,.......:...~:.:::.....+,~:+..=:::IIIIIIIIIIIIIIIIIIIIIIII\n"+
	"~IIIIIIIIII,.,.+I~.~.,=......,,........~::+:,..,:,:,,~~,,~~~,=:~.,.....,.+=IIIIIIIIIIIIIIIIIIIIIIII\n"+
	":~IIIIIIIIIIIII?=.II?:..~,:~~~~:=.:,~=.::..,,:...=..~,...~.,~,~~=I~,,,?IIIIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"IIIIIIIIIIIII7,,.+I,..~,:.,~,:~~.::,,~....,~==~.:,,~=~,.,..::,,.:.~~..~IIIIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"II77777777II7,..::,,~~~....,.::..~:..,.,.+.+:.~.~.:,,.:,?I:+~~,~...,~,,=7IIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"7777777777777.+~:=~+:~.:,:::=~,~...,~:~=..I.~.,..~,.,:~+~=++:,,:=II~,=+IIIIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"77777777777777?7I=,.II=..:~..,..~........=...,::,~::,...:,==,~+~,~.~~IIIIIIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"777777777777777I==I7I~I+=:...77IIII+,..,.,,.,...,=,..:,::,:,,I77I7I7IIIIIIIIIIIIIIIIIIIIIIIIIIIIIII\n"+
	"7777777777777777777777I+=7,?7777777777I?,.,....:I+.:.~~~,==I77777777777777777IIIIIIIIIIIIIIIIIIIIII\n"+
	"777777777777??~I777777777777777777777777=:.....777777I777777777777777777777777777IIIIIIIIIIIIIIIIII\n"+
	"I777777777I=~~?~~=77777777777777777777777I.,,,~77777777777777777777777777777777777777IIIIIIIIIIIIII\n"+
	"=77777777I+7:~++:~+=7777777777777777777777:.==+77777777777777777777777777777777777777777IIIIIIIIIII\n"+
	"=+??+?=+I7I=+==~~~~=I7I?I?II+I+I?I=~:~?I7I==I=??II77IIIII7I7I7777III77777777777I7I7II7III7III7IIIII\n"+
	"~~~=~~=~=~=~~:~:~~~~~:~:~~~~:~~:~~~~~:~~=~+==:?~~~==~~~:~~~~:~==~~:~==+?+==~+===++=I???III?+I??????\n"+
	"========~==~========~========~=~======~====~~~=====~==~=====~~~====~=~=~~========~==~~=~===~~~~~===\n"+
	"~=~~==~===~~~==~=~==~===~=~==~=~=~=~~====~==~=~=====~=====~~=~=~=~==+=??=?=++===~=~===~==?~+~===?==\n"+
	"==~====~~~==~~=====~====~~~=~===~====~=====~=~==~===~~=~~===~~~=~====~I=?~=+===~=~~~=~=~?~~?I=:?=~~\n"+
	"~~==++~==~=====~=~==~~~~==~~=~~~=~~=~====~======~========~=~==~==~~=~~~:==~~=======~~~===~~~~~==~~~\n"+
	"======~~==~===~=~==~==~~~~~==+=~~~+~~=~=~=~=~:~=~=~~~~+~=~+==~~~~=:=:=~=~=~=~~~==~~=~~:~~=~=~~+~~~~\n";
	
	
	/**
	 * Basic command line interface to the library
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String inputfilename = null;
		String outputFolder = null;
		String outputFormat = null;
		boolean debug = false;
		String inputFormat = null;
		
		IDendroCollectionWriter writer;
		IDendroFileReader reader;
		TridasProject project = null;
		
		// Check we have the right number of args
		if(args.length > 5 || args.length < 3){
			showHelp(true);
			return;
		}
		
		int index = 0;
		if(args[index].equalsIgnoreCase("-debug")){
			debug = true;
			index++;
		}
		if(args[index].startsWith("-")){
			inputFormat = args[index].substring(1);
			index++;
		}
		
		// Set up filenames
		inputfilename = args[index];
		outputFormat = args[index+1];
		outputFolder = args[index+2];
		
		if(debug){
			// ! figure this out, how do I change log stuff
		}
		
		if(inputFormat != null){
			reader = TridasIO.getFileReader(inputFormat);
		}else{
			reader = TridasIO.getFileReaderFromExtension(inputfilename.substring(inputfilename.lastIndexOf(".")+1));
		}
		if(reader == null){
			System.out.println("Could not find reader");
			showHelp(false);
			return;
		}
		
		writer = TridasIO.getFileWriter(outputFormat);
		if(writer == null){
			System.out.println("Could not find writer");
			showHelp(false);
			return;
		}

		// Read in File
		try {
			reader.loadFile(inputfilename);
			project = reader.getProject();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidDendroFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(reader.getDefaults() != null){
			for(ConversionWarning cw : reader.getDefaults().getConversionWarnings()){
				System.out.println("  - ["+ cw.getWarningType().toString()+ "]: " + cw.getMessage());
			}
		}
		
		
	    // Write out project
		try {
			writer.loadProject(project);
			writer.saveAllToDisk(outputFolder);
			
		} catch (IncompleteTridasDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConversionWarningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncorrectDefaultFieldsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(writer.getDefaults() != null){
			for(ConversionWarning cw : reader.getDefaults().getConversionWarnings()){
				System.out.println("  - ["+ cw.getWarningType().toString()+ "]: " + cw.getMessage());
			}
		}
		
		
		
		// Show warnings if necessary
		if(writer.getWarnings().size() > 0)
			System.out.println("*** Warnings were thrown while converting your data ***".toUpperCase());
		for(ConversionWarning cw : writer.getWarnings())
			System.out.println("  - ["+ cw.getWarningType().toString()+ "]: " + cw.getMessage());
		
		// Grab a list of dendro files that will be written
		DendroFile[] files = writer.getFiles();
		
		// Show list of output files
		for(DendroFile f : files)
		{
			System.out.println("File saved to: " + writer.getNamingConvention().getFilename(f));
		}
				
		
	}
	
	private static void showHelp(boolean argLogo){
		if(argLogo){
			System.out.print(ConvertFile.asciilogo);
		}
		System.out.println("");
		System.out.println("Dendro File Converter:");
		System.out.println("Usage: [-debug] [-inputFormatName] inputFilename outputFormatName outputFolder");
		System.out.println("Supported reading formats: ");
		for( String format : TridasIO.getSupportedReadingFormats()){
			System.out.println("  "+format);
		}
		System.out.println("Supported writing formats: ");
		for( String format : TridasIO.getSupportedWritingFormats()){
			System.out.println("  "+format);
		}
	}

}
