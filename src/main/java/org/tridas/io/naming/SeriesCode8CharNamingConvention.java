package org.tridas.io.naming;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.util.StringUtils;

/**
 * Same as SeriesCodeNamingConvention but limits to 8 characters which can be useful for
 * DOS programs such as CATRAS.
 * 
 * @author pwb48
 *
 */
public class SeriesCode8CharNamingConvention extends SeriesCodeNamingConvention {

	@Override
	protected String getDendroFilename(ITridasSeries argSeries) {
	
		return StringUtils.rightPadWithTrim(super.getDendroFilename(argSeries), 8).trim();

	}
	
	@Override
	public String getDescription() {
		return I18n.getText("namingconvention.seriescode8char.description");
	}
	
	@Override
	public String getName() {
		return I18n.getText("namingconvention.seriescode8char");
	}
}
