package development.go.Ee;

import development.go.Ee.ParameterIteration.SourcedIterator;
import timeseriesweka.classifiers.Nn.Specialised.Ddtw.ClassicTunedDdtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Ddtw.TunedDdtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Dtw.ClassicTunedDtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Dtw.TunedDtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Erp.TunedErpNn;
import timeseriesweka.classifiers.Nn.Specialised.Lcss.TunedLcssNn;
import timeseriesweka.classifiers.Nn.Specialised.Msm.TunedMsmNn;
import timeseriesweka.classifiers.Nn.Specialised.Twe.TunedTweNn;
import timeseriesweka.classifiers.Nn.Specialised.Wddtw.ClassicTunedWddtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Wddtw.TunedWddtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Wdtw.ClassicTunedWdtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Wdtw.TunedWdtwNn;
import utilities.Utilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class Ee extends AbstractTuned {
    protected List<AbstractTuned> constituents = new ArrayList<>();

    @Override
    protected java.util.Iterator<Integer> getParameterPermutationIterator(int seed) {
        constituentIterator.setSource(constituents);
        constituentIterator.setSeed(seed);
        return constituentIterator;
    }

    @Override
    protected void populateParameterPermutationIndices() {
        for(AbstractTuned constituent : constituents) {
            constituent.populateParameterPermutationIndices();
        }
        super.populateParameterPermutationIndices();
    }

    @Override
    protected void setParameterPermutationIndex(int index) throws Exception {
        boolean match = false;
        int constituentIndex = 0;
        AbstractTuned constituent;
        do {
            constituent = constituents.get(constituentIndex);
            if(index < constituent.size()) {
                match = true;
            } else {
                index -= constituent.size();
                constituentIndex++;
            }
        } while (!match && constituentIndex < constituents.size());
        if(!match) {
            throw new IllegalArgumentException("index out of range");
        }
        constituent.setParameterPermutationIndex(index);
        setClassifier(constituent.getClassifier());
    }

    private SourcedIterator<Integer, List<AbstractTuned>> constituentIterator = new RandomConstituentIterator();

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        for(AbstractTuned constituent : constituents) {
            constituent.useTrainInstances(trainInstances);
        }
        parameterSpace.clear();
        parameterSpace.addParameter("paramIndex", Utilities.naturalNumbersFromZero(size()));
    }

    @Override
    public int size() {
        int size = 0;
        for(AbstractTuned constituent : constituents) {
            size += constituent.size();
        }
        return size;
    }

    public List<AbstractTuned> getConstituents() {
        return constituents;
    }

    public Ee() {
        constituents.clear();
        constituents.add(new TunedDtwNn());
        constituents.add(new TunedDdtwNn());
        constituents.add(new TunedWdtwNn());
        constituents.add(new TunedWddtwNn());
        constituents.add(new TunedLcssNn());
        constituents.add(new TunedMsmNn());
        constituents.add(new TunedErpNn());
        constituents.add(new TunedTweNn());
    }

    public void classicSetup() {
        constituents.clear();
        constituents.add(new ClassicTunedDtwNn());
        constituents.add(new ClassicTunedDdtwNn());
        constituents.add(new ClassicTunedWdtwNn());
        constituents.add(new ClassicTunedWddtwNn());
        constituents.add(new TunedLcssNn());
        constituents.add(new TunedMsmNn());
        constituents.add(new TunedErpNn());
        constituents.add(new TunedTweNn());
    }
}
