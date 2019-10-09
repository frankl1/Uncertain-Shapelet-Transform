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
package com.decisiontree.eval;

import com.decisiontree.param.GlobalParam;

/**
 *
 * EntropyFunction
 *
 * @author Smith Tsang
 * @since 0.9
 *
 */
public class EntropyFunction implements DispersionMeasure{

	private int noCls;
	private double noTuples;

	public EntropyFunction(){
		
	}

	@Override
	public void init(double noTuples, int noCls){
		this.noTuples = noTuples;
		this.noCls = noCls;
	}

	public double getDispersion(double [] dist, double distSize){

		double entropy = 0.0;

		for(int i = 0; i < dist.length ; i++)
			if(dist[i] > GlobalParam.DOUBLE_PRECISION)
				entropy += dist[i] * Math.log(dist[i]/distSize)/Math.log(2.0);

		return -1.0 * entropy/distSize;
	}

	public double averageDispersion(double [] left, double [] right){

		double leftSize = getDistributionSum(left);
		double rightSize = getDistributionSum(right);

		return (getDispersion(left, leftSize) * leftSize +
				getDispersion(right, rightSize) * rightSize) / noTuples;

	}

	private double getDistributionSum(double [] dist){
		double distSize =0 ;
		for(int i = 0; i < dist.length; i++)
			distSize += dist[i];
		return distSize;

	}


	public double findLowerBound(double [] left, double [] right, double [] region){

		double leftSize = 0.0, rightSize = 0.0;
		for(int i = 0 ; i < noCls; i++){
			leftSize += left[i];
			rightSize += right[i] - region[i];
		}

		double ent = 0.0;
		for(int i = 0 ; i < noCls; i++){

			double leftT = left[i] + region[i];
			double logValueL = 0;
			if( leftT > GlobalParam.DOUBLE_PRECISION){
				logValueL = Math.log(leftT/(leftSize + region[i]))/Math.log(2.0);
			}
			double sumLeft = left[i] * logValueL;

			double logValueR = 0;
			if(right[i] > GlobalParam.DOUBLE_PRECISION)
				logValueR = Math.log(right[i]/(rightSize + region[i]))/Math.log(2.0);
			double sumRight = (right[i] - region[i]) * logValueR;


			double remain = 0;
			if(region[i] > GlobalParam.DOUBLE_PRECISION){
				if(logValueL > logValueR)
					remain = region[i] * logValueL;
				else remain = region[i] * logValueR;
			}

			ent +=  (sumLeft + sumRight + remain);
		}

		return -1.0 * ent/noTuples;
	}

}
