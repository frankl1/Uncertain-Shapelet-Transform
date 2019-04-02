/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package timeseriesweka.classifiers;

import weka.classifiers.Classifier;
import weka.core.Instances;

import java.io.Serializable;

/**
 *
 * @author ajb
 */
public interface ParameterSplittable extends Serializable, Classifier {
    default void setParamSearch(boolean b) {

    }
/* The actual parameter values should be set internally. This integer
  is just a key to maintain different parameter sets. The range starts at 1
    */
    public void setParametersFromIndex(int x);
    public String getParas();
    double getAcc();
    default void setUpParameters(Instances trainInstances) {

    }

    default int size() {
        return -1;
    }

    default void useOnlineParameterSearch() {
        setParametersFromIndex(-1);
    }

    default void setPostProcess(boolean postProcess) {

    }
}
