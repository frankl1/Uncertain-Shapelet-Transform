package timeseriesweka.classifiers.Nn.Specialised.Wddtw;

import timeseriesweka.classifiers.Nn.Specialised.Wdtw.ClassicTunedWdtwNn;

public class ClassicTunedWddtwNn extends ClassicTunedWdtwNn {
    public ClassicTunedWddtwNn() {
        setClassifier(new WddtwNn());
    }
}
