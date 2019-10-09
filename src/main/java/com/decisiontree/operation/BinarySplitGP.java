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

import com.decisiontree.data.SampleAttrClass;
import com.decisiontree.eval.DispersionMeasure;

/**
 *
 * BinarySplitGP - Finds the best binary split point of an attribute using the global pruning technique.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class BinarySplitGP extends BinarySplitLP{


	public BinarySplitGP(String dispersionStr) {
		super(dispersionStr);
	}

	public BinarySplitGP(DispersionMeasure dispersion) {
		super(dispersion);
	}

	public void run(Histogram [] segments, double [] lowerBounds, SampleAttrClass [] attrClassSet, double threshold){

		pruned = true;
		this.threshold = threshold;
		boolean unpruned[] = findUnprunedRegion(segments, lowerBounds);

		if(pruned) return;

		double left[] = new double[noCls];
		double right[] = new double[noCls];

		for(int i = 0 ; i < segments.length; i++){
			for(int j = 0 ; j < noCls; j++){
				right[j] += segments[i].getCls(j);
			}

		}

		for(int i = 0; i < segments.length; i++){
			if(unpruned[i]){
				double bestEnt = findEntInRegion(segments[i],attrClassSet,left,right);
				if(this.threshold - bestEnt > 1E-12){
					this.threshold = bestEnt;
					localOptimal = tempOptimal;
				}
			}
			for(int j = 0 ; j < noCls; j++){
				left[j] += segments[i].getCls(j);
				right[j] -= segments[i].getCls(j);
			}


		}
	}



}
