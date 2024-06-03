
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AntColonyOptimization {

    private static final double EVAPORATION_RATE = 0.1;
    private static final double PHEROMONE_INCREMENT = 5.0;

    private static class Item {
        int id;
        int weight;

        public Item(int id, int weight) {
            this.id = id;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Item{" + "id=" + id + ", weight=" + weight + '}';
        }
    }

    private static class BinPackingData {
        List<Item> items;
        int binCapacity;

        BinPackingData(List<Item> items, int binCapacity) {
            this.items = items;
            this.binCapacity = binCapacity;
        }
    }

    private static int[][] initializePheromones(int bins, int items) {
        int[][] pheromones = new int[bins][items];
        for (int i = 0; i < bins; i++) {
            Arrays.fill(pheromones[i], 1); // Initializing pheromone values
        }
        return pheromones;
    }

    private static int[][] addBin(int[][] pheromones, int items) {
        int[][] newPheromones = new int[pheromones.length + 1][items];
        System.arraycopy(pheromones, 0, newPheromones, 0, pheromones.length);
        Arrays.fill(newPheromones[pheromones.length], 1); // Initialize the new bin's pheromone values
        return newPheromones;
    }

    private static List<List<Item>> packItems(List<Item> items, int[][] pheromones, int maxWeight) {
        int bins = pheromones.length;
        List<List<Item>> binContents = new ArrayList<>();
        for (int i = 0; i < bins; i++) {
            binContents.add(new ArrayList<>());
        }
        int[] binWeights = new int[bins];

        Random random = new Random();
        for (Item item : items) {
            List<Integer> options = new ArrayList<>();
            for (int j = 0; j < bins; j++) {
                if (binWeights[j] + item.weight <= maxWeight) {
                    options.add(j);
                }
            }
            if (options.isEmpty()) {
                bins++;
                binContents.add(new ArrayList<>());
                binWeights = Arrays.copyOf(binWeights, bins);
                pheromones = addBin(pheromones, items.size());
                options.add(bins - 1);  // Include new bin as an option
            }

            int chosenBin = options.get(random.nextInt(options.size()));
            binContents.get(chosenBin).add(item);
            binWeights[chosenBin] += item.weight;
            updatePheromones(pheromones, chosenBin, item.id);
        }return binContents;}

    private static void updatePheromones(int[][] pheromones, int binIndex, int itemId) {
        for (int i = 0; i < pheromones.length; i++) {
            for (int j = 0; j < pheromones[i].length; j++) {
                pheromones[i][j] *= (1 - EVAPORATION_RATE);  // Pheromone evaporation
            }
        }
        pheromones[binIndex][itemId] += PHEROMONE_INCREMENT;  // Reinforce successful placement
    }

    private static BinPackingData parseFile(String filename) throws IOException {
        List<Item> items = new ArrayList<>();
        int binCapacity;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int numItems = Integer.parseInt(reader.readLine()); // Number of items
            binCapacity = Integer.parseInt(reader.readLine()); // Bin capacity
            String line;
            int id = 0;
            while ((line = reader.readLine()) != null) {
                int weight = Integer.parseInt(line);
                items.add(new Item(id++, weight));
            }
        }
        return new BinPackingData(items, binCapacity);
    }

    public static void main(String[] args) {
        try {
            BinPackingData binPackingData = parseFile("input.txt");
            List<Item> items = binPackingData.items;
            int binCapacity = binPackingData.binCapacity;
            int[][] pheromones = initializePheromones(1, items.size()); // Start with 1 bin
            List<List<Item>> bins = packItems(items, pheromones, binCapacity);
            for (int binIndex = 0; binIndex < bins.size(); binIndex++) {
                System.out.println("Bin " + binIndex + " contains: " + bins.get(binIndex));
            }
            System.out.println("Total number of bins used: " + bins.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
