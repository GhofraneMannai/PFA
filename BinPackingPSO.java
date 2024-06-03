import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class BinPackingPSO {

    static class Particle {
        ArrayList<ArrayList<Integer>> bins;
        int fitness;//nombre de bacs utilisés
        ArrayList<ArrayList<Integer>> personalBestBins;//meilleure solution personnelle de la particule.
        int personalBestFitness;//valeur de fitness de la meilleure solution personnelle
        ArrayList<Integer> velocity; // vitesse de la particule (vecteur représentant le changement de position)

        public Particle(ArrayList<ArrayList<Integer>> bins, int fitness, ArrayList<Integer> velocity) {
            this.bins = new ArrayList<>(bins);
            this.fitness = fitness;
            this.personalBestBins = new ArrayList<>(bins);
            this.personalBestFitness = fitness;
            this.velocity = new ArrayList<>(velocity);
        }

       // met à jour la meilleure solution personnelle si l'actuelle est meilleure.

        public void updatePersonalBest() {
            if (this.fitness < this.personalBestFitness) {
                this.personalBestBins = new ArrayList<>(this.bins);
                this.personalBestFitness = this.fitness;
            }
        }
    }
 public static void main(String[] args) {
        try {
            BinData binData = readBinDataFromFile("input.txt");
            int[] weights = binData.weights;
            int maxBinWeight = binData.maxBinWeight;
            
            int swarmSize = 100;
            int maxIterations = 1000;
            double inertiaWeight = 0.9;
            double cognitiveWeight = 2.0;
            double socialWeight = 2.0;
           // le swarm (essaim) de particules
            ArrayList<Particle> swarm = initializeSwarm(weights, maxBinWeight, swarmSize);
            Particle globalBest = getBestParticle(swarm);
            Random random = new Random(42);
            for (int iteration = 1; iteration <= maxIterations; iteration++) {
                for (Particle particle : swarm) {
                    //Met à jour la vitesse d'une particule.
                    updateVelocity(particle, globalBest, inertiaWeight, cognitiveWeight, socialWeight, random);
                    //Met à jour les bacs d'une particule en fonction de sa vitesse
                    updateBins(particle, maxBinWeight);
                //Évalue la fitness d'une particule
                    evaluateFitness(particle, weights, maxBinWeight);
                    particle.updatePersonalBest();
                }
                globalBest = getBestParticle(swarm);
                System.out.println("Iteration " + iteration + ": Best fitness = " + globalBest.fitness);}
            System.out.println("Final Result:");
            System.out.println("Number of bins used: " + globalBest.bins.size());
            System.out.println("Items in each bin:");
            for (int i = 0; i < globalBest.bins.size(); i++) {
                System.out.println("Bin " + (i + 1) + ": " + globalBest.bins.get(i));}
        } catch (IOException e) {
            e.printStackTrace();}
    }

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
    public static ArrayList<Particle> initializeSwarm(int[] weights, int maxBinWeight, int swarmSize) {
        ArrayList<Particle> swarm = new ArrayList<>();
    
        Random random = new Random();
    
        for (int i = 0; i < swarmSize; i++) {
            ArrayList<ArrayList<Integer>> bins = new ArrayList<>();
            ArrayList<Integer> velocity = new ArrayList<>();
            int totalWeight = 0;
            for (int j = 0; j < weights.length; j++) {
                if (totalWeight + weights[j] <= maxBinWeight) {
                    if (bins.isEmpty() || totalWeight + weights[j] > maxBinWeight) {
                        bins.add(new ArrayList<>());
                    }
                    bins.get(bins.size() - 1).add(weights[j]);
                    totalWeight += weights[j];
                    velocity.add(bins.size() - 1);
                } else {
                    bins.add(new ArrayList<>());
                    bins.get(bins.size() - 1).add(weights[j]);
                    totalWeight = weights[j];
                    velocity.add(bins.size() - 1);
                }
            }
            Particle particle = new Particle(bins, bins.size(), velocity);
            swarm.add(particle);
        }
        return swarm;
    }
    
    public static void updateBins(Particle particle, int maxBinWeight) {
        ArrayList<ArrayList<Integer>> newBins = new ArrayList<>();
        for (int i = 0; i < particle.bins.size(); i++) {
            newBins.add(new ArrayList<>());
        }
        for (int i = 0; i < particle.bins.size(); i++) {
            ArrayList<Integer> currentBin = particle.bins.get(i);
            ArrayList<Integer> currentBinItems = new ArrayList<>(currentBin); // Create a copy to avoid ConcurrentModificationException
            for (Integer item : currentBinItems) {
                int newBinIndex = particle.velocity.get(i) % particle.bins.size();
                if (newBinIndex < 0) {
                    newBinIndex += particle.bins.size(); // Ensure non-negative index
                }
                if (fitsInBin(newBins, newBinIndex, item, maxBinWeight)) {
                    newBins.get(newBinIndex).add(item);
                    currentBin.remove(item); // Remove item from the current bin
                } else {
                    // Find the next bin where the item fits
                    int nextBinIndex = (newBinIndex + 1) % particle.bins.size();
                    while (nextBinIndex != newBinIndex && !fitsInBin(newBins, nextBinIndex, item, maxBinWeight)) {
                        nextBinIndex = (nextBinIndex + 1) % particle.bins.size();
                    }
                    if (nextBinIndex != newBinIndex) {
                        newBins.get(nextBinIndex).add(item);
                        currentBin.remove(item); // Remove item from the current bin
                    }
                }
            }
        }
        // Remove empty bins
        ArrayList<ArrayList<Integer>> nonEmptyBins = new ArrayList<>();
        for (ArrayList<Integer> bin : newBins) {
            if (!bin.isEmpty()) {
                nonEmptyBins.add(bin);
            }
        }particle.bins = nonEmptyBins;
    }   
    public static void updateVelocity(Particle particle, Particle globalBest, double inertiaWeight,
                                      double cognitiveWeight, double socialWeight, Random random) {
        for (int i = 0; i < particle.velocity.size(); i++) {
            int newVelocity = (int) (inertiaWeight * particle.velocity.get(i) +
                    cognitiveWeight * random.nextDouble() * (particle.personalBestBins.size() - i) +
                    socialWeight * random.nextDouble() * (globalBest.personalBestBins.size() - i));
            particle.velocity.set(i, newVelocity);}}       
    public static boolean fitsInBin(ArrayList<ArrayList<Integer>> bins, int binIndex, int item, int maxBinWeight) {
        int binWeight = bins.get(binIndex).stream().mapToInt(Integer::intValue).sum();
        return binWeight + item <= maxBinWeight;
    }
    public static void evaluateFitness(Particle particle, int[] weights, int maxBinWeight) {
        int binsOverMax = 0;
        int underutilizedBins = 0;
        for (ArrayList<Integer> bin : particle.bins) {
            int binWeight = bin.stream().mapToInt(Integer::intValue).sum();
            if (binWeight > maxBinWeight) {
                binsOverMax++;
            }
            if (binWeight < maxBinWeight * 0.75) {
                underutilizedBins++;}
        }
        particle.fitness = particle.bins.size() + binsOverMax + underutilizedBins;
    }

    public static Particle getBestParticle(ArrayList<Particle> swarm) {
        Particle best = swarm.get(0);
        for (Particle particle : swarm) {
            if (particle.fitness < best.fitness) {
                best = particle;
            }
        }
        return best;
    }
    
}