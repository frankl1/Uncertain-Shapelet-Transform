# Uncertain Shapelet Transform

UST is as extension of the ST algorithm that take into account the uncertaintity in data.

**Test class**: src/main/java/u_shapelet_transform/UShapeletTransformTest.java

## Uncertain Euclidian Distance
\[
    UED(S \pm \delta S,R \pm \delta R)=(\frac{1}{l}\sum_{i=1}^l (s_i-r_i)^2)\pm (\frac{2}{l}\sum_{i=1}^l|s_i-r_i| \times (\delta s_i + \delta r_i))
\]

\[
    UED(S \pm \delta S,R \pm \delta R)=ED(S,R)\pm (\frac{2}{l}\sum_{i=1}^l|s_i-r_i| \times (\delta s_i + \delta r_i))
\]

## Flat Architecture
![Flat architecture](https://github.com/frankl1/Uncertain-Shapelet-Transform/blob/master/ust-flat-architecture.png)

## Gauss Architecture
![Gauss architecture](https://github.com/frankl1/Uncertain-Shapelet-Transform/blob/master/ust-gauss-architecture.png)

## Flat-Gauss  Architecture
![Flat-Gauss architecture](https://github.com/frankl1/Uncertain-Shapelet-Transform/blob/master/ust-flat-gauss-architecture.png)

## Add noise illustration
![noise illustration](https://github.com/frankl1/Uncertain-Shapelet-Transform/blob/master/Chinatown-noised.png)




Find more info in this ![report](https://github.com/frankl1/Uncertain-Shapelet-Transform/blob/master/rapport-stage.pdf).
