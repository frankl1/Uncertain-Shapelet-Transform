//package development.go.Ee;
//
//import evaluation.storage.ClassifierResults;
//import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
//import timeseriesweka.classifiers.Nn.NeighbourWeighting.WeightByDistance;
//import timeseriesweka.classifiers.Nn.Nn;
//import timeseriesweka.measures.dtw.Dtw;
//import utilities.ArrayUtilities;
//import utilities.Utilities;
//import weka.classifiers.Classifier;
//import weka.core.Instance;
//import weka.core.Instances;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//public class TSEF extends AdvancedAbstractClassifier {
//    private int num = 500;
//
//    private static class Member {
//        private final Classifier classifier;
//        private final ClassifierResults trainResults;
//        private final int intervalStart;
//
//        public Classifier getClassifier() {
//            return classifier;
//        }
//
//        public ClassifierResults getTrainResults() {
//            return trainResults;
//        }
//
//        public int getIntervalStart() {
//            return intervalStart;
//        }
//
//        public int getIntervalLength() {
//            return intervalLength;
//        }
//
//        public int getIntervalEnd() {
//            return intervalStart + intervalLength;
//        }
//
//        private final int intervalLength;
//
//        private Member(final Classifier classifier, final ClassifierResults trainResults, final int intervalStart, final int intervalLength) {
//            this.classifier = classifier;
//            this.trainResults = trainResults;
//            this.intervalStart = intervalStart;
//            this.intervalLength = intervalLength;
//        }
//
//        @Override
//        public String toString() {
//            return String.valueOf(trainResults.getAcc());
//        }
//    }
//
//    private final List<Member> members = new ArrayList<>();
//
//    private Instances trimInterval(Instances instances, int min, int max) {
//        Instances trimmed = new Instances(instances);
//        for(int i = trimmed.numAttributes() - 2; i > max; i--) {
//            trimmed.deleteAttributeAt(i);
//        }
//        for(int i = min - 1; i >= 0; i--) {
//            trimmed.deleteAttributeAt(i);
//        }
//        return trimmed;
//    }
//
//    private Instance trimInterval(Instance instance, int min, int max) {
//        Instances dataset = new Instances(instance.dataset(), 0);
//        dataset.add(instance);
//        for(int i = dataset.numAttributes() - 2; i > max; i--) {
//            dataset.deleteAttributeAt(i);
//        }
//        for(int i = min - 1; i >= 0; i--) {
//            dataset.deleteAttributeAt(i);
//        }
//        return dataset.get(0);
//    }
//
//    private boolean postProcess = true;
//
//    @Override
//    public void buildClassifier(final Instances trainInstances) throws Exception {
//        if(postProcess) {
//            File[] files = new File(savePath).listFiles();
//            if(files == null) {
//                throw new IllegalStateException("no files found");
//            }
//            for(File file : files) {
//                Nn classifier = new Nn();
//                classifier.setSeed(seed);
//                classifier.setNeighbourWeighter(new WeightByDistance());
//                ClassifierResults classifierResults = new ClassifierResults();
//                classifierResults.loadResultsFromFile(file.getPath());
//                String options = classifierResults.getParas();
//                int index = options.indexOf(",");
//                options = options.substring(index + 1);
//                index = options.indexOf(",");
//                int intervalStart = Integer.parseInt(options.substring(0, index));
//                options = options.substring(index + 1);
//                index = options.indexOf(",");
//                options = options.substring(index + 1);
//                index = options.indexOf(",");
//                int intervalLength = Integer.parseInt(options.substring(0, index));
//                options = options.substring(index + 1);
//                classifier.setOptions(options.split(","));
//                classifier.setCvTrain(false);
//                System.out.println(intervalStart + " - " + intervalLength + ", " + classifierResults.getAcc());
//                members.add(new Member(classifier, classifierResults, intervalStart, intervalLength));
//            }
//        } else {
//            int instanceLength = trainInstances.numAttributes() - 1;
//            int minIntervalLength = (int) Math.sqrt(instanceLength);
////        for(int i = 0; i < num; i++) {
////            Nn nn = new Nn();
////            nn.setCvTrain(isCvTrain());
////            nn.setNeighbourWeighter(new WeightByDistance());
//////            Nn.setSampleSizePercentage((double) trainInstances.numClasses() / trainInstances.numInstances());
////            Dtw dtw = new Dtw();
////            dtw.setWarpingWindow(1);
////            nn.setDistanceMeasure(dtw);
////            nn.setUseRandomTieBreak(false);
////            int intervalStart = random.nextInt(instanceLength - minIntervalLength);
////            int intervalLength = random.nextInt(instanceLength - (intervalStart + minIntervalLength) + 1) + minIntervalLength;
////            int intervalEnd = intervalStart + intervalLength;
////            Instances intervalTrainInstances = trimInterval(trainInstances, intervalStart, intervalEnd);
////            nn.buildClassifier(intervalTrainInstances);
////            Member member = new Member(nn, nn.getTrainResults(), intervalStart, intervalLength);
////            members.add(member);
////            System.out.println(i + ", " + intervalStart + ", " + intervalEnd + ", " + member.getTrainResults().getAcc());
////            if(isCheckpointing() && isCvTrain()) {
////                nn.getTrainResults().writeFullResultsToFile(savePath + "/" + i + ".csv");
////            }
////        }
//            for (int i = 0, count = 0; i <= instanceLength; i++) {
//                for (int j = 0; j < i; j++, count++) {
//                    Nn nn = new Nn();
//                    nn.setSeed(seed);
//                    nn.setCvTrain(isCvTrain());
//                    nn.setNeighbourWeighter(new WeightByDistance());
////            Nn.setSampleSizePercentage((double) trainInstances.numClasses() / trainInstances.numInstances());
//                    Dtw dtw = new Dtw();
//                    dtw.setWarpingWindow(1);
//                    nn.setDistanceMeasure(dtw);
//                    nn.setUseRandomTieBreak(false);
////                int intervalStart = random.nextInt(instanceLength - minIntervalLength);
////                int intervalLength = random.nextInt(instanceLength - (intervalStart + minIntervalLength) + 1) + minIntervalLength;
////                int intervalEnd = intervalStart + intervalLength;
//                    int intervalStart = j;
//                    int intervalEnd = i;
//                    int intervalLength = intervalEnd - intervalStart + 1;
//                    Instances intervalTrainInstances = trimInterval(trainInstances, intervalStart, intervalEnd);
//                    nn.buildClassifier(intervalTrainInstances);
//                    Member member = new Member(nn, nn.getTrainResults(), intervalStart, intervalLength);
//                    members.add(member);
//                    System.out.println(count + ", " + intervalStart + ", " + intervalEnd + ", " + member.getTrainResults().getAcc());
//                    if (isCheckpointing() && isCvTrain()) {
//                        ClassifierResults trainResults = nn.getTrainResults();
//                        trainResults.setParas("start," + intervalStart + ",length," + intervalLength + "," + trainResults.getParas());
//                        trainResults.writeFullResultsToFile(savePath + "/" + count + ".csv");
//                    }
//                }
//            }
//            Collections.shuffle(members, random);
//        }
//        members.sort((member, t1) -> Double.compare(t1.getTrainResults().getAcc(), member.getTrainResults().getAcc()));
//        List<List<Member>> best = new ArrayList<>();
////        List<Member> best = new ArrayList<>();
//        System.out.println("Best:");
//        for(int i = 0; i < members.size(); i++) {
//            Member member = members.get(i);
//            boolean overlap = false;
//            for(int j = 0; j < best.size(); j++) {
//                List<Member> list = best.get(j);
//                for(Member bestMember : list) {
//                    if(!overlap && member.getIntervalStart() >= bestMember.getIntervalStart() || member.getIntervalEnd() <= bestMember.getIntervalEnd()) {
//                        overlap = true;
//                    }
//                }
//                if(overlap) {
//
//                    overlap = false;
//                }
//            }
//            System.out.println(member.getIntervalStart() + ", " + member.getIntervalEnd() + ", " + member.getTrainResults().getAcc());
//        }
////        for(int i = 0; i < trainInstances.size(); i++) {
////            Member member = members.get(i);
////            best.add(member);
////            System.out.println(member.getIntervalStart() + ", " + member.getIntervalEnd() + ", " + member.getTrainResults().getAcc());
////        }
//        members.clear();
//        members.addAll(best);
//        if(isCheckpointing() && isCvTrain()) {
//            trainResults = new ClassifierResults();
//            if(postProcess) {
//                for(Member member : members) {
//                    Instances trimmed = trimInterval(trainInstances, member.getIntervalStart(), member.getIntervalEnd());
//                    member.getClassifier().buildClassifier(trimmed);
//                }
//            }
//            for(int i = 0; i < trainInstances.size(); i++) {
//                Instance trainInstance = trainInstances.get(i);
//                double[] overallDistribution = new double[trainInstances.numClasses()];
//                for(int j = 0; j < members.size(); j++) {
//                    ClassifierResults results = members.get(j).getTrainResults();
//                    double[] distribution = results.getProbabilityDistribution(i);
//                    ArrayUtilities.multiply(distribution, results.getAcc());
//                    ArrayUtilities.add(overallDistribution, distribution);
//                }
//                ArrayUtilities.normalise(overallDistribution);
//                trainResults.addPrediction(trainInstance.classValue(), overallDistribution, Utilities.argMax(overallDistribution, random), -1, null);
//            }
//            trainResults.writeFullResultsToFile(trainFilePath);
//        }
//    }
//
//    @Override
//    public double[] distributionForInstance(final Instance testInstance) throws Exception {
//        double[] distribution = new double[testInstance.numClasses()];
//        for(int i = 0; i < members.size(); i++) {
//            Member member = members.get(i);
//            Instance trimmedTestInstance = trimInterval(testInstance, member.getIntervalStart(), member.getIntervalEnd());
//            double[] constituentDistribution = member.getClassifier().distributionForInstance(trimmedTestInstance);
//            ArrayUtilities.normalise(constituentDistribution);
//            ArrayUtilities.multiply(constituentDistribution, member.getTrainResults().getAcc());
//            ArrayUtilities.add(distribution, constituentDistribution);
//        }
//        ArrayUtilities.normalise(distribution);
//        return distribution;
////        return votingScheme.distributionForInstance(modules, testInstance);
//    }
//
//}
