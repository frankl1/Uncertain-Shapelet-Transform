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
package com.decisiontree.datagen;

import java.io.File;

import com.decisiontree.param.GlobalParam;

/**
 * 
 * DataCleaner (Interface) - Cleans the generated interval-value data files.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class RangeDataCleaner implements DataCleaner{

	public void cleanGeneratedData(String input) {
		File file = new File(input + GlobalParam.RANGE_FILE);
		if(file.exists()) file.delete();
	}

	public void cleanGeneratedDataWithTest(String training, String testing) {
		cleanGeneratedData(training);
		if(testing != null) cleanGeneratedData(testing);
	}

}
