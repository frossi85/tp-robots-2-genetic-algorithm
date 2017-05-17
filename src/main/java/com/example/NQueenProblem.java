package com.example;

import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.codecs;
import org.jenetics.engine.limit;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.jenetics.engine.EvolutionResult.toBestPhenotype;
import static org.jenetics.engine.limit.byFitnessThreshold;

public class NQueenProblem {
    //La representacion es un array de tamaño numberOfQueens en el , por que un array y no una matriz? Por que
    //sabemos que para que sea una solucion valida no puede haber mas de una reina en la misma fila.
    //Esta decision va a reducir la memoria y tiempo utilizado para obtener la solucion


    //Si el cromosoma = {3,6,8,5,1,4,0,7,9,2}, el primeri numero define la columna en donde se posiciona la reina en la primera fila,
    // , el segundo numero define la columna de la reina en la segunda fila y asi con los demas


    private int numberOfQueens;
    private int populationSize;
    private static int defualtNumberOfQueens = 8;
    private static int defaultPopulationSize = 100;

    NQueenProblem(int numberOfQueens, int populationSize) {
        this.numberOfQueens = numberOfQueens;
        this.populationSize = populationSize;
    }

    NQueenProblem(int numberOfQueens) {
        this(numberOfQueens, defaultPopulationSize);
    }

    NQueenProblem() {
        this(defualtNumberOfQueens, defaultPopulationSize);
    }

    /**
     * This method will count the
     * number of queen that threat each other corisponding to the given queens
     * configuration
     *
     * @param config configuration of the queens in the chess board
     * @return total number of threat
     */
    private static long countDiagonal(final int[] config) {
        long sum = IntStream
                .range(0, config.length)
                .parallel()
                .mapToLong((i) -> IntStream
                        .range(0, config.length)
                        .parallel()
                        .filter((j) -> {
                            if (i != j) {
                                int deltaRow = Math.abs(i - j);
                                int deltaCol = Math.abs(config[i] - config[j]);
                                return deltaRow == deltaCol;
                            } else {
                                return false;
                            }
                        }).count()).sum();
        return sum;
    }

    public List<Integer> getResult() {
        long start = System.currentTimeMillis();

        System.out.println("Executors Initialized");

        final ExecutorService executor = Executors.newFixedThreadPool(8);

        System.out.println("Engine Initialized");

        final Engine<EnumGene<Integer>, Long> engine = Engine
                .builder(
                        NQueenProblem::countDiagonal,
                        codecs.ofPermutation(numberOfQueens))
                .optimize(Optimize.MINIMUM)
                .survivorsSelector(new TournamentSelector<>(5))
                .offspringSelector(new LinearRankSelector<>())
                .populationSize(populationSize)
                .alterers(new SwapMutator<>(0.01),
                        new PartiallyMatchedCrossover<>(0.8))
                .executor(executor)
                .build();

        System.out.println("Engine Started, Please Wait");

        final Phenotype<EnumGene<Integer>, Long> best
                = engine.stream()
                .limit(byFitnessThreshold(1L))
                .limit(limit.byExecutionTime(Duration.ofMinutes(30)))
                .collect(toBestPhenotype());

        System.out.println("Executors Shutdown");

        executor.shutdown();

        long end = System.currentTimeMillis();

        System.out.println("Prepare Result");

        int[] resultConfig = best
                .getGenotype()
                .getChromosome()
                .stream()
                .mapToInt((EnumGene gene) -> (Integer) gene.getAllele())
                .toArray();

        System.out.println("Total Time: " + (end - start) + " ms");

        return IntStream.of(resultConfig).boxed().collect(Collectors.toList());
    }
}
