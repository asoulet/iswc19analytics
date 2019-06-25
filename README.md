# Anytime Large-Scale Analytics of Linked Open Data

Analytical queries are queries with numerical aggregators: computing the average number of objects per property, identifying the most frequent subjects, etc. Such queries are essential to monitor the quality and the content of the Linked Open Data (LOD) cloud. Many analytical queries cannot be executed directly on the SPARQL endpoints, because the fair use policy cuts off expensive queries. This paper shows how to rewrite such queries into a set of queries that each satisfy the fair use policy.

## Publication

*Anytime Large-Scale Analytics of Linked Open Data.* Arnaud Soulet and Fabian M. Suchanek, Full paper at ISWC19 (research track).

## Results of experiments

- *Validation on DBpedia:*
  - *Quality of convergence:* [dbpedia_convergence.csv](results/dbpedia_convergence.csv)
  - *Top-k precision:* [dbpedia_top.csv](results/dbpedia_top.csv)
  - *Efficiency:* [dbpedia_efficiency.csv](results/dbpedia_efficiency.csv)
- *Usage query on the LOD cloud:* [LOD_statistics.csv](results/LOD_statistics.csv)
- *Representativeness of LOD:* [LOD_representativeness.csv](results/LOD_representativeness.csv)

## Source code

We provide the Java source code of the prototype:

- *Usage query:* This implementation measures property usage and class usage in the [statistics directory](https://github.com/asoulet/iswc19analytics/tree/master/statistics).
- *Representativeness of LOD:* This implementation computes for each property, a distribution over the frequency of the first significant digit of the number of objects per subject in the [representativeness directory](https://github.com/asoulet/iswc19analytics/tree/master/representativeness). We used the method proposed [here](http://www.info.univ-tours.fr/~soulet/prototype/iswc18/) to convert this distribution into a score between 0 and 1 that measures the "representativeness" of the triplestores.
