package rainbow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        if (args.length == 3 && args[0].equals("generate") && Utils.isUnsignedInteger(args[2])) {
            int idx = Integer.parseUnsignedInt(args[2]);
            RainbowGen gen = new RainbowGen(Path.of(args[1]), HashProblem.md5AsciiAlphaNum(5), idx, 1_000);
            gen.generate();
        } else if (args.length >= 3 && args.length % 2 == 1 && args[0].equals("evaluate")) {
            var gens = new RainbowGen[(args.length - 1) / 2];
            var tables = new RainbowTable[(args.length - 1) / 2];
            for (int i = 0; i < gens.length; i++) {
                int idx = Integer.parseUnsignedInt(args[2 * i + 2]);
                gens[i] = new RainbowGen(Path.of(args[2 * i + 1]), HashProblem.md5AsciiAlphaNum(5), idx, 1_000);
                tables[i] = gens[i].table;
            }
            RainbowTables table = new RainbowTables(tables);
            long found = 0;
            long total = 0;
            long start = System.nanoTime();
            for (;;) {
                var password = gens[0].randomPassword();
                var hash = tables[0].hash(password);
                var f = table.lookup(hash);
                if (f != null && Arrays.equals(tables[0].hash(f), hash)) {
                    found += 1;
                }
                total += 1;
                if (total % 1 == 0) {
                    System.out.println(
                            "found " + Math.round(100 * found / (double) total) + "% (" + found + " out of " + total
                                    + ")  "
                                    + ((total * 1_000_000_000) / (System.nanoTime() - start)) + "/s");
                }
            }
        } else if (args.length >= 3 && args.length % 2 == 0 && args[0].equals("find")) {
            var hash = new byte[args[1].length() / 2];
            for (int i = 0; i < hash.length; i++) {
                hash[i] = (byte) Integer.parseInt(args[1].substring(2 * i, 2 * i + 2), 16);
            }
            var gens = new RainbowGen[(args.length - 2) / 2];
            var tables = new RainbowTable[(args.length - 2) / 2];
            for (int i = 0; i < gens.length; i++) {
                int idx = Integer.parseUnsignedInt(args[2 * i + 3]);
                gens[i] = new RainbowGen(Path.of(args[2 * i + 2]), HashProblem.md5AsciiAlphaNum(5), idx, 1_000);
                tables[i] = gens[i].table;
            }
            RainbowTables table = new RainbowTables(tables);
            var password = table.lookup(hash);
            if (password != null) {
                System.out.println("found matching password: '" + new String(password) + "'");
            } else {
                System.out.println("no match found");
            }
        } else {
            System.err.println("Usage: java rainbow.Main {generate|evaluate|find} [FILE IDX]");
        }
    }
}
