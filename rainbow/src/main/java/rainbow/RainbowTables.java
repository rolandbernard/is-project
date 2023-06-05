package rainbow;

import java.util.*;

/**
 * Class for using multiple rainbow tables in parallel.
 */
public class RainbowTables {
    private final RainbowTable[] tables;

    /**
     * @param tables
     *            The tables that are to be used for lookup. All tables must be for
     *            the same problem but may have different chain length and chain
     *            index.
     */
    public RainbowTables(RainbowTable... tables) {
        this.tables = tables;
        Arrays.sort(tables, (a, b) -> Integer.compare(b.chainLength, a.chainLength));
    }

    /**
     * @param hash
     *            The hash to search for.
     * @return The password if found or null otherwise.
     */
    public byte[] lookup(byte[] hash) {
        for (int i = 0; i < tables[0].chainLength; i++) {
            for (RainbowTable table : tables) {
                if (i >= table.chainLength) {
                    break;
                }
                var res = table.lookup(hash, i);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }
}
