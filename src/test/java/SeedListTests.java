import com.seedfinding.mccore.util.pos.CPos;
import kludwisz.data.SeedList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SeedListTests {
    @Test
    public void testSeedListSimple() {
        SeedList seedList = new SeedList();
        assertEquals(0, seedList.getEntries().size());
        seedList.addEntry(List.of(1L));
        assertEquals(1, seedList.getEntries().size());
        assertEquals(1L, seedList.getEntry(0).getSeed());
    }

    @Test
    public void testSeedListWithFormat() {
        SeedList seedList = new SeedList(SeedList.EntryFormat.CHUNK_POS, SeedList.EntryFormat.SEED, SeedList.EntryFormat.INTEGER);

        seedList.addEntry(List.of(1L, 1L, -1234627132142164L, 214357128812L));
        seedList.addEntry(List.of(2L, 2L, -213217132142164L, 1232141542421L));
        seedList.addEntry(List.of(-3L, -3L, 42134627132142164L, 912634712124312L));
        assertEquals(3, seedList.getEntries().size());

        assertEquals(-1234627132142164L, seedList.getEntry(0).getSeed());
        assertEquals(-213217132142164L, seedList.getEntry(1).getSeed());
        assertEquals(42134627132142164L, seedList.getEntry(2).getSeed());

        assertEquals(new CPos(-3, -3), seedList.getEntry(2).getChunkPos(0));

        assertEquals(1232141542421L, seedList.getEntry(1).getInteger(0));

        assertThrows(IndexOutOfBoundsException.class, () -> seedList.getEntry(3));
        assertThrows(IndexOutOfBoundsException.class, () -> seedList.getEntry(1).getInteger(1));
    }

    @Test
    public void testFileIO() {
        List<SeedList.EntryFormat> format = List.of(SeedList.EntryFormat.BLOCK_POS, SeedList.EntryFormat.SEED, SeedList.EntryFormat.CHUNK_POS);
        SeedList list = SeedList.fromFile("src/test/resources/test_seedlist.txt", format);
        assertNotNull(list);
        assertEquals(4, list.getEntries().size());

        for (int i = 0; i < 4; i++) {
            assertEquals(i, list.getEntry(i).getChunkPos(0).getX());
            assertEquals(i, list.getEntry(i).getChunkPos(0).getZ());
        }

        assertTrue(list.toFile("src/test/resources/test_seedlist_out.txt"));
    }
}
