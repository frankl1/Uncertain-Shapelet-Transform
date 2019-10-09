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

import java.util.List;

import com.decisiontree.data.PointAttrClass;
import com.decisiontree.data.PointAttribute;
import com.decisiontree.data.Tuple;

/**
 * 
 * SplitSearchORI - finding the best split point for a set of data (point-valued data) using point-valued pruning technique.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SplitSearchORI extends SplitSearchBP {
	
	public SplitSearchORI(String dispersionStr){
		super(dispersionStr);
	}

	
	protected SplitSearchORI(Split split){
		super(split);
	}

	@Override
	protected PointAttrClass [] generatePointAttrClass(List<Tuple> data, int attr){
		
		int noTuples = data.size();
		PointAttrClass n [] = new PointAttrClass[noTuples];
		for(int j = 0; j < noTuples; j++){
					
			PointAttribute nB = (PointAttribute)(data.get(j).getAttribute(attr));
			n[j] = new PointAttrClass(nB,data.get(j).getCls(), data.get(j).getWeight());
		}
		return n;
	}
	
	

}
