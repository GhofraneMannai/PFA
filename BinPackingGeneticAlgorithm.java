import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
public class BinPackingGeneticAlgorithm {
    static class Chromosome {
        ArrayList<ArrayList<Integer>> bins;
        int fitness;
        public Chromosome(ArrayList<ArrayList<Integer>> bins, int fitness) {
            this.bins = bins;
            this.fitness = fitness;
        }
    }
    public static void main(String[] args) {
        try {
            BinData binData = readBinDataFromFile("input.txt");
            int[] weights = binData.weights;
            int maxBinWeight = binData.maxBinWeight;
            int populationSize = 100;
            int maxGenerations = 1000;
            double mutationRate = 0.01;
            ArrayList<Chromosome> population = initializePopulation(weights, maxBinWeight, populationSize);
            Random random = new Random(42);
            for (int generation = 1; generation <= maxGenerations; generation++) {
                ArrayList<Chromosome> selectedParents = selectParents(population, random);
                ArrayList<Chromosome> offspring = crossover(selectedParents, populationSize, random);
                mutate(offspring, mutationRate, maxBinWeight, random);
                evaluateFitness(offspring, weights, maxBinWeight);
                replaceWorst(population, offspring);
                System.out.println("Generation " + generation + ": Best fitness = " + getBestFitness(population));
            }
            System.out.println("Final Result:");
            Chromosome bestChromosome = getBestChromosome(population);
            System.out.println("Number of bins used: " + bestChromosome.bins.size());
            System.out.println("Items in each bin:");
            for (int i = 0; i < bestChromosome.bins.size(); i++) {
                System.out.println("Bin " + (i + 1) + ": " + bestChromosome.bins.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();}}

    static class BinData {
        int[] weights;
        int maxBinWeight;

        public BinData(int[] weights, int maxBinWeight) {
            this.weights = weights;
            this.maxBinWeight = maxBinWeight;
        }
    }
    public static BinData readBinDataFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        int numItems = Integer.parseInt(reader.readLine().trim()); 
        int maxBinWeight = Integer.parseInt(reader.readLine().trim()); 
        int[] weights = new int[numItems];

        String line;
        int index = 0;
        while ((line = reader.readLine()) != null && index < numItems) {
            weights[index++] = Integer.parseInt(line.trim());
        }
        reader.close();

        return new BinData(weights, maxBinWeight);
    }
    public static ArrayList<Chromosome> initializePopulation(int[] weights, int maxBinWeight, int populationSize) {
        ArrayList<Chromosome> population = new ArrayList<>();
        Random random = new Random(42);

        // Sorting weights in decreasing order for better initial solutions
        int[] sortedWeights = weights.clone();
        Arrays.sort(sortedWeights);
        for (int i = 0; i < sortedWeights.length / 2; i++) {
            int temp = sortedWeights[i];
            sortedWeights[i] = sortedWeights[sortedWeights.length - i - 1];
            sortedWeights[sortedWeights.length - i - 1] = temp;
        }
        for (int i = 0; i < populationSize; i++) {
            ArrayList<ArrayList<Integer>> bins = new ArrayList<>();
            for (int weight : sortedWeights) {
                boolean placed = false;
                for (ArrayList<Integer> bin : bins) {
                    int binWeight = bin.stream().mapToInt(Integer::intValue).sum();
                    if (binWeight + weight <= maxBinWeight) {
                        bin.add(weight);
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    ArrayList<Integer> newBin = new ArrayList<>();
                    newBin.add(weight);
                    bins.add(newBin);
                }
            }
            population.add(new Chromosome(bins, bins.size())); // Fitness is initially the number of bins used
        }
        return population;
    }

    public static ArrayList<Chromosome> selectParents(ArrayList<Chromosome> population, Random random) {
        ArrayList<Chromosome> selectedParents = new ArrayList<>();
        int tournamentSize = 5;
        for (int i = 0; i < population.size(); i++) {
            ArrayList<Chromosome> tournament = new ArrayList<>();
            for (int j = 0; j < tournamentSize; j++) {
                tournament.add(population.get(random.nextInt(population.size())));
            }
            selectedParents.add(getBestChromosome(tournament));
        }
        return selectedParents;
    }

    public static Chromosome getBestChromosome(ArrayList<Chromosome> chromosomes) {
        Chromosome best = chromosomes.get(0);
        for (Chromosome chromosome : chromosomes) {
            if (chromosome.fitness < best.fitness) {
                best = chromosome;
            }
        }
        return best;
    }

    public static ArrayList<Chromosome> crossover(ArrayList<Chromosome> parents, int populationSize, Random random) {
        ArrayList<Chromosome> offspring = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Chromosome parent1 = parents.get(random.nextInt(parents.size()));
            Chromosome parent2 = parents.get(random.nextInt(parents.size()));
            ArrayList<ArrayList<Integer>> bins = new ArrayList<>();
            int crossoverPoint = random.nextInt(Math.min(parent1.bins.size(), parent2.bins.size()) + 1);
            for (int j = 0; j < crossoverPoint; j++) {
                bins.add(new ArrayList<>(parent1.bins.get(j)));
            }
            for (int j = crossoverPoint; j < parent2.bins.size(); j++) {
                bins.add(new ArrayList<>(parent2.bins.get(j)));
            }
            offspring.add(new Chromosome(bins, bins.size())); 
        }
        return offspring;
    }
    public static void mutate(ArrayList<Chromosome> offspring, double mutationRate, int maxBinWeight, Random random) {
        for (Chromosome chromosome : offspring) {
            if (random.nextDouble() < mutationRate) {
                int binIndex1 = random.nextInt(chromosome.bins.size());
                int binIndex2 = random.nextInt(chromosome.bins.size());
                if (binIndex1 != binIndex2) {
                    ArrayList<Integer> bin1 = chromosome.bins.get(binIndex1);
                    ArrayList<Integer> bin2 = chromosome.bins.get(binIndex2);
                    if (!bin1.isEmpty() && !bin2.isEmpty()) {
                        int itemIndex1 = random.nextInt(bin1.size());
                        int itemIndex2 = random.nextInt(bin2.size());
                        int item1 = bin1.get(itemIndex1);
                        int item2 = bin2.get(itemIndex2);
                        int newBin1Weight = bin1.stream().mapToInt(Integer::intValue).sum() - item1 + item2;
                        int newBin2Weight = bin2.stream().mapToInt(Integer::intValue).sum() - item2 + item1;
                        if (newBin1Weight <= maxBinWeight && newBin2Weight <= maxBinWeight) {
                            bin1.set(itemIndex1, item2);
                            bin2.set(itemIndex2, item1);
                        }
                    }
                }
            }
        }
    }

    public static void evaluateFitness(ArrayList<Chromosome> population, int[] weights, int maxBinWeight) {
        for (Chromosome chromosome : population) {
            int binsOverMax = 0;
            int underutilizedBins = 0;
            for (ArrayList<Integer> bin : chromosome.bins) {
                int binWeight = bin.stream().mapToInt(Integer::intValue).sum();
                if (binWeight > maxBinWeight) {
                    binsOverMax++;
                }
                if (binWeight < maxBinWeight * 0.75) {
                    underutilizedBins++;
                }
            }
            chromosome.fitness = chromosome.bins.size() + binsOverMax + underutilizedBins; 
        }
    }

    public static void replaceWorst(ArrayList<Chromosome> population, ArrayList<Chromosome> offspring) {
        population.sort((c1, c2) -> c2.fitness - c1.fitness); 
        for (int i = 0; i < offspring.size(); i++) {
            population.set(population.size() - 1 - i, offspring.get(i)); 
        }
    }

    public static int getBestFitness(ArrayList<Chromosome> population) {
        return getBestChromosome(population).fitness;
    }
}
