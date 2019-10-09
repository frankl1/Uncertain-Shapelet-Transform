/**
 * Decision Tree Classification With Uncertain Data (UDT)
 * Copyright (C) 2009, The Database Group,
 * Department of Computer Science, The University of Hong Kong
 *
 * This file is part of UDT.
 *
 * UDT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UDT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.decisiontree.operation;

import org.apache.log4j.Logger;

/**
*
* SplitSearchFactory - Creates an instance of split search based on the algorithm and dispersion
*
* @author Smith Tsang
* @since 0.8
*
*/
public class SplitSearchFactory {
		
	public static Logger log = Logger.getLogger(SplitSearchFactory.class);
	
	public static SplitSearch createSplitSearch(String algorithm, String dispersionStr){

		SplitSearch splitSearch = null;
		if(algorithm.equals(SplitSearch.UDT))
			splitSearch = new SplitSearchUnp(dispersionStr);
		else if(algorithm.equals(SplitSearch.UDTBP))
			splitSearch = new SplitSearchBP(dispersionStr);
		else if(algorithm.equals(SplitSearch.UDTGP))
			splitSearch = new SplitSearchGP(dispersionStr);
		else if(algorithm.equals(SplitSearch.UDTLP))
			splitSearch = new SplitSearchLP(dispersionStr);
		else if(algorithm.equals(SplitSearch.UDTES))
			splitSearch = new SplitSearchES(dispersionStr);
		else if(algorithm.equals(SplitSearch.AVG))
			splitSearch = new SplitSearchORI(dispersionStr);
		else if(algorithm.equals(SplitSearch.UDTUD))
			splitSearch = new SplitSearchUD(dispersionStr);
		else if(algorithm.equals(SplitSearch.AVGUD))
			splitSearch = new SplitSearchUD(dispersionStr);
		else if(algorithm.equals(SplitSearch.POINT))
			splitSearch = new SplitSearchORI(dispersionStr);
		else {
			log.error("Incorrect split search is selected.");
		}
		return splitSearch;
	}
}
