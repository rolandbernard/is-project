package rainbow;

import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

/**
 * Tool for generating rainbow tables for the given problem. Multiple tables
 * could be generated and used to increase the hit rate.
 */
public class RainbowGen {
    private static final int NUM_THREAD = 20;
    private static final int ITER_LENGTH = 1_000;
    public final RainbowTable table;
    private BufferedWriter output;
    private Random random;

    public RainbowGen(Path file, HashProblem problem, int tableIdx, int chainLength) throws IOException {
        table = RainbowTable.loadFile(file, problem, tableIdx, chainLength);
        output = Files.newBufferedWriter(file, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        random = new SecureRandom();
    }

    public synchronized byte[] randomPassword() {
        var bytes = new byte[16];
        random.nextBytes(bytes);
        return table.reduce(table.hash(bytes), 0);
    }

    private synchronized void registerChains(Map<ByteArray, byte[]> chains) throws IOException {
        var encoder = Base64.getUrlEncoder();
        for (var entry : chains.entrySet()) {
            if (!table.chains.containsKey(entry.getKey())) {
                output.write(encoder.encodeToString(entry.getKey().bytes));
                output.write(" ");
                output.write(encoder.encodeToString(entry.getValue()));
                output.write("\n");
                table.chains.put(entry.getKey(), entry.getValue());
            }
        }
        System.out.println("num chains: " + table.chains.size());
        output.flush();
    }

    private void buildChains(int numberIter) {
        try {
            var chains = new HashMap<ByteArray, byte[]>();
            for (int j = 0; j < numberIter; j++) {
                var start = randomPassword();
                var end = table.buildChain(start, table.chainLength);
                chains.put(new ByteArray(end), start);
            }
            registerChains(chains);
        } catch (IOException e) {
            throw Utils.panic(e);
        }
    }

    public void generate() throws InterruptedException, ExecutionException, IOException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
        Future<?>[] futures = new Future[10 * NUM_THREAD];
        for (;;) {
            for (int i = 0; i < 10 * NUM_THREAD; i++) {
                futures[i] = executor.submit(() -> buildChains(ITER_LENGTH));
            }
            for (int i = 0; i < 10 * NUM_THREAD; i++) {
                futures[i].get();
            }
        }
    }
}
