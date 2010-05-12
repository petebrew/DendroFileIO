package org.tridas.io.formats.heidelberg;

import java.util.ArrayList;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.IDendroCollectionWriter;
import org.tridas.io.IDendroFile;

public class HeidelbergFile implements IDendroFile {

	private IDendroCollectionWriter writer;
	private ArrayList<ITridasSeries> series = new ArrayList<ITridasSeries>();
	private TridasToHeidelbergDefaults defaults;
	
	public HeidelbergFile(IDendroCollectionWriter argWriter, TridasToHeidelbergDefaults argDefaults){
		writer = argWriter;
		defaults = argDefaults;
	}
	
	@Override
	public String getExtension() {
		return "fh";
	}
	
	public void setSeries(ITridasSeries argSeries){
		series.set(0, argSeries);
	}

	@Override
	public ITridasSeries[] getSeries() {
		return series.toArray(new ITridasSeries[0]);
	}

	@Override
	public IDendroCollectionWriter getWriter() {
		return writer;
	}

	@Override
	public String[] saveToString() {
		
		return null;
	}

}
