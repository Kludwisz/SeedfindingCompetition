package kludwisz.data;

import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

@SuppressWarnings("unused")
public class SeedList {
    private final ArrayList<Entry> entries = new ArrayList<>();
    private final List<EntryFormat> formatSequence;

    /**
     * Creates a new, flat SeedList with the default format sequence (only the seed).
     */
    public SeedList() {
        this.formatSequence = Collections.singletonList(EntryFormat.SEED);
        this.parseFormatIndices();
    }

    /**
     * Creates a new SeedList with the specified format sequence.
     * @param formatSequence the format sequence to use.
     */
    public SeedList(List<EntryFormat> formatSequence) {
        this.formatSequence = formatSequence;
        this.parseFormatIndices();
    }

    /**
     * Creates a new SeedList with the specified format sequence.
     * @param formatSequence the format sequence to use.
     */
    public SeedList(EntryFormat... formatSequence) {
        this.formatSequence = Arrays.asList(formatSequence);
        this.parseFormatIndices();
    }

    // ----------------------------------------------------------------------------------

    /**
     * @return the list of all entries in the SeedList.
     */
    public List<Entry> getEntries() {
        return this.entries;
    }

    /**
     * @param ix the index of the entry to get.
     * @return the entry at the specified index.
     */
    public Entry getEntry(int ix) {
        return this.entries.get(ix);
    }

    /**
     * Adds a new entry to the SeedList.
     * @param entry the entry to add.
     */
    public void addEntry(List<Long> entry) {
        if (entry.size() != this.formatSequence.size()) {
            throw new IllegalArgumentException("Entry size does not match format sequence size");
        }
        this.entries.add(new Entry(entry));
    }

    // ----------------------------------------------------------------------------------

    /**
     * Converts the SeedList to a flat SeedList, where each entry contains only the first seed.
     * @return a new SeedList with only the first seed in each entry.
     */
    public SeedList flatten() {
        SeedList result = new SeedList();
        for (Entry entry : this.entries) {
            result.addEntry(Collections.singletonList(entry.getSeed()));
        }
        return result;
    }

    /**
     * Creates a new SeedList of the same format as this one, but only containing entries
     * such that the second SeedList contains the entry's seed.
     * @param other the SeedList used for filtering.
     * @return a new SeedList with the aforementioned entries.
     */
    public SeedList filterSeeds(SeedList other) {
        HashSet<Long> otherSeeds = new HashSet<>();
        for (Entry entry : other.getEntries()) {
            otherSeeds.add(entry.getSeed());
        }

        SeedList result = new SeedList(this.formatSequence);
        for (Entry entry : this.entries) {
            if (otherSeeds.contains(entry.getSeed())) {
                result.addEntry(entry.values);
            }
        }

        return result;
    }

    // ----------------------------------------------------------------------------------

    /**
     * Parses a flat SeedList from the given file using the default format sequence of only the seed.
     * @param filename the name of the file to read from.
     * @return the SeedList read from the file, or null if an error occurred.
     */
    public static SeedList fromFile(String filename) {
        return SeedList.fromFile(filename, Collections.singletonList(EntryFormat.SEED));
    }

    /**
     * Parses a SeedList from the given file using the given format.
     * @param filename the name of the file to read from.
     * @param formatSequence the format sequence to use.
     * @return the SeedList read from the file, or null if an error occurred.
     */
    public static SeedList fromFile(String filename, EntryFormat... formatSequence) {
        return SeedList.fromFile(filename, Arrays.asList(formatSequence));
    }

    /**
     * Parses a SeedList from the given file using the given format.
     * @param filename the name of the file to read from.
     * @param formatSequence the format sequence to use.
     * @return the SeedList read from the file, or null if an error occurred.
     */
    public static SeedList fromFile(String filename, List<EntryFormat> formatSequence) {
        try {
            Scanner fin = new Scanner(new File(filename));
            SeedList result = new SeedList(formatSequence);

            while (fin.hasNextLine()) {
                String line = fin.nextLine();
                List<Long> entry = new ArrayList<>();
                for (String s : line.split(" ")) {
                    entry.add(Long.parseLong(s));
                }
                result.addEntry(entry);
            }

            fin.close();
        }
        catch (Exception ignored) {}
        return null;
    }

    /**
     * Writes the SeedList to the given file. Deletes the previous contents of the file.
     * @param filename the name of the file to write to.
     * @return true if the operation was successful, false otherwise.
     */
    public boolean toFile(String filename) {
        try {
            FileWriter fout = new FileWriter(filename);

            for (Entry entry : this.entries) {
                for (long value : entry.values) {
                    fout.write(value + " ");
                }
                fout.write("\n");
            }

            fout.close();
        }
        catch (Exception ignored) {}
        return false;
    }


    // ----------------------------------------------------------------------------------

    /**
     * The possible formats for a single entry inside a SeedList.
     */
    public enum EntryFormat {
        SEED,
        CHUNK_POS,
        BLOCK_POS,
        INTEGER
    }

    private int seedIndex = -1;
    private final List<Integer> chunkIndices = new ArrayList<>();
    private final List<Integer> blockIndices = new ArrayList<>();
    private final List<Integer> integerIndices = new ArrayList<>();

    private void parseFormatIndices() {
        int ix = 0;
        for (EntryFormat format : this.formatSequence) {
            switch (format) {
                case SEED:
                    this.seedIndex = ix;
                    ix++;
                    break;
                case CHUNK_POS:
                    this.chunkIndices.add(ix);
                    ix += 2;
                    break;
                case BLOCK_POS:
                    this.blockIndices.add(ix);
                    ix += 3;
                    break;
                case INTEGER:
                    this.integerIndices.add(ix);
                    ix++;
                    break;
            }
        }
    }

    /**
     * A class that wraps the numerical values stored inside a SeedList.
     * Allows easy access to specific objects, like the seed, chunk positions,
     * block positions, or simple integers based on the entry format.
     */
    public class Entry {
        private final List<Long> values;

        /**
         * Constructs a new Entry with the given values.
         */
        public Entry(List<Long> values) {
            this.values = values;
        }

        /**
         * @return the entry's seed.
         */
        public long getSeed() {
            return this.values.get(seedIndex);
        }

        /**
         * @return the i-th integer in the entry.
         * @throws IndexOutOfBoundsException if the entry contains less than i-1 integers.
         */
        public long getInteger(int i) {
            int ix = integerIndices.get(i);
            return this.values.get(ix);
        }

        /**
         * @return the i-th chunk position in the entry.
         * @throws IndexOutOfBoundsException if the entry contains less than i-1 chunk positions.
         * @throws ArithmeticException if the resulting values are too large to be chunk positions.
         */
        public CPos getChunkPos(int i) {
            int ix = chunkIndices.get(i);
            return new CPos(
                    Math.toIntExact(this.values.get(ix)),
                    Math.toIntExact(this.values.get(ix + 1))
            );
        }

        /**
         * @return the i-th block position in the entry.
         * @throws IndexOutOfBoundsException if the entry contains less than i-1 block positions.
         * @throws ArithmeticException if the resulting values are too large to be chunk positions.
         */
        public BPos getBlockPos(int i) {
            int ix = blockIndices.get(i);
            return new BPos(
                    Math.toIntExact(this.values.get(ix)),
                    Math.toIntExact(this.values.get(ix + 1)),
                    Math.toIntExact(this.values.get(ix + 2))
            );
        }
    }
}
