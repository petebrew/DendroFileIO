/*******************************************************************************
 * Copyright 2011 Daniel Murphy and Peter Brewer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.tridas.io.naming;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.util.FileHelper;
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
	
		return FileHelper.sanitiseFilename(StringUtils.rightPadWithTrim(super.getDendroFilename(argSeries), 8).trim());

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
