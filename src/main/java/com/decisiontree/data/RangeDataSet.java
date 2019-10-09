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
package com.decisiontree.data;

import java.util.Iterator;
import java.util.List;

/**
 * 
 * RangeDataSet - Stores the database information for interval-valued dataset.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class RangeDataSet extends PointDataSet{


	public RangeDataSet(int noCls, int noAttr){
		super(noCls, noAttr);
	}
	
	public RangeDataSet(List<Tuple> data, int noCls, int noAttr){
		super(data, noCls, noAttr);
	}
	
	public RangeDataSet(String input, int noCls, int noAttr) {
		super(input, noCls, noAttr);
	}

	
	@Override
	public double getMax(int pos){

		if( !isContinuous(pos)) return -1;
		
		double max = Double.NEGATIVE_INFINITY;
		Iterator<Tuple> iter = data.iterator();
		while(iter.hasNext()){
			RangeAttribute b = (RangeAttribute) iter.next().getAttribute(pos);
	
			if(b.getEnd() > max)
				max = b.getEnd();
		}

		return max;
	}

	@Override
	public double getMin(int pos){
	
		if( !isContinuous(pos)) return -1;
		double min = Double.POSITIVE_INFINITY;
		Iterator<Tuple> iter = data.iterator();
		while(iter.hasNext()){
			RangeAttribute b = (RangeAttribute) iter.next().getAttribute(pos);
		
			if(b.getStart() < min)
				min = b.getStart();
		}
		
		return min;
	}
	
}