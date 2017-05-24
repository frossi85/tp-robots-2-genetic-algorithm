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
    // The board is represented by an array of intergers. This integer represents the column where one queen
    // is placed in the row i where i is the index in the array.
    // I choose this representation because if we analyze the problem we can not have more than one queen in the same
    // row. We can also say the same for columns.

    private int boardSize;
    private int populationSize;
    private static int defaultNumberOfQueens = 8;
    private static int defaultPopulationSize = 100;

    NQueenProblem(int boardSize, int populationSize) {
        this.boardSize = boardSize;
        this.populationSize = populationSize;
    }

    NQueenProblem(int boardSize) {
        this(boardSize, defaultPopulationSize);
    }

    NQueenProblem() {
        this(defaultNumberOfQueens, defaultPopulationSize);
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
                        codecs.ofPermutation(boardSize))
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

        long numberOfIterations = engine
                .stream()
                .limit(byFitnessThreshold(1L))
                .limit(limit.byExecutionTime(Duration.ofMinutes(30)))
                .count();

        System.out.println("Number of iterations: " + numberOfIterations);

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
