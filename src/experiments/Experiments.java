package experiments;

import dp.EditDistance;
import dp.LCS;
import matching.KMPMatcher;
import matching.NaiveMatcher;
import matching.RabinKarpMatcher;
import matching.StringMatcher;
import suffix.Kasai;
import suffix.SuffixArray;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.LongSupplier;

public final class Experiments {
    static final String ALPHABET = "abcd";
    static final long SEED = 42L;
    static final int RUNS = 5;
    static final int WARMUP_RUNS = 30;
    static final long WARMUP_MIN_NANOS = 300_000_000L;
    static final int PATTERN_LENGTH = 50;

    static final int[] DP_SIZES = {100, 500, 1000};
    static final int[] TEXT_LENGTHS = {10_000, 50_000, 100_000};
    static final int[] KASAI_SIZES = {1000, 5000, 10_000};

    static volatile long sink;

    static String randomString(int length, Random rnd) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
        return sb.toString();
    }

    static double averageMs(LongSupplier task) {
        long warmStart = System.nanoTime();
        int warm = 0;
        while (warm < WARMUP_RUNS || System.nanoTime() - warmStart < WARMUP_MIN_NANOS) {
            sink += task.getAsLong();
            warm++;
        }
        System.gc();
        long total = 0;
        for (int r = 0; r < RUNS; r++) {
            long start = System.nanoTime();
            long value = task.getAsLong();
            long end = System.nanoTime();
            total += end - start;
            sink += value;
        }
        return total / (double) RUNS / 1_000_000.0;
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");
        File resultsDir = new File("results");
        File plotsDir = new File("plots");
        resultsDir.mkdirs();
        plotsDir.mkdirs();

        StringBuilder md = new StringBuilder();

        double[] dpX = new double[DP_SIZES.length];
        double[] lcsMs = new double[DP_SIZES.length];
        double[] edMs = new double[DP_SIZES.length];
        int[] lcsResult = new int[DP_SIZES.length];
        int[] edResult = new int[DP_SIZES.length];
        Random dpRnd = new Random(SEED);
        for (int k = 0; k < DP_SIZES.length; k++) {
            int n = DP_SIZES[k];
            String a = randomString(n, dpRnd);
            String b = randomString(n, dpRnd);
            dpX[k] = n;
            lcsMs[k] = averageMs(() -> LCS.length(a, b));
            edMs[k] = averageMs(() -> EditDistance.distance(a, b));
            lcsResult[k] = LCS.length(a, b);
            edResult[k] = EditDistance.distance(a, b);
        }

        String[] algNames = {"Naive", "KMP", "Rabin-Karp"};
        int cases = 2;
        double[][][] matchMs = new double[algNames.length][cases][TEXT_LENGTHS.length];
        long[][][] matchCmp = new long[algNames.length][cases][TEXT_LENGTHS.length];
        long[][] rkSpurious = new long[cases][TEXT_LENGTHS.length];
        int[][] occurrences = new int[cases][TEXT_LENGTHS.length];
        for (int k = 0; k < TEXT_LENGTHS.length; k++) {
            int n = TEXT_LENGTHS[k];
            String text = randomString(n, new Random(SEED + n));
            String present = text.substring(n / 2, n / 2 + PATTERN_LENGTH);
            Random patRnd = new Random(SEED + 7);
            String absent;
            do {
                absent = randomString(PATTERN_LENGTH, patRnd);
            } while (!new NaiveMatcher().search(text, absent).isEmpty());
            String[] patterns = {present, absent};

            for (int c = 0; c < cases; c++) {
                final String pattern = patterns[c];
                StringMatcher[] matchers = {new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher()};
                List<Integer> reference = matchers[0].search(text, pattern);
                occurrences[c][k] = reference.size();
                for (int a = 0; a < matchers.length; a++) {
                    final StringMatcher m = matchers[a];
                    if (!m.search(text, pattern).equals(reference)) {
                        throw new IllegalStateException(m.name() + " disagrees with Naive on n=" + n);
                    }
                    matchMs[a][c][k] = averageMs(() -> m.search(text, pattern).size());
                    m.search(text, pattern);
                    matchCmp[a][c][k] = m.getComparisons();
                    if (m instanceof RabinKarpMatcher) rkSpurious[c][k] = ((RabinKarpMatcher) m).getSpuriousHits();
                }
            }
        }

        double[] kasaiX = new double[KASAI_SIZES.length];
        double[] kasaiMs = new double[KASAI_SIZES.length];
        long[] kasaiCmp = new long[KASAI_SIZES.length];
        for (int k = 0; k < KASAI_SIZES.length; k++) {
            int n = KASAI_SIZES[k];
            String s = randomString(n, new Random(SEED + n));
            int[] sa = SuffixArray.build(s);
            Kasai kasai = new Kasai();
            kasaiX[k] = n;
            kasaiMs[k] = averageMs(() -> kasai.build(s, sa)[n - 1]);
            kasai.build(s, sa);
            kasaiCmp[k] = kasai.getComparisons();
        }

        StringBuilder csv = new StringBuilder("algorithm,input_size,avg_ms,result\n");
        md.append("### LCS and Edit Distance (average of ").append(RUNS).append(" runs)\n\n");
        md.append("| Length n (both strings) | LCS time (ms) | Edit Distance time (ms) | DP cells computed (n*n) | LCS length | Edit distance |\n");
        md.append("|---:|---:|---:|---:|---:|---:|\n");
        for (int k = 0; k < DP_SIZES.length; k++) {
            int n = DP_SIZES[k];
            md.append(String.format("| %d | %.4f | %.4f | %d | %d | %d |%n", n, lcsMs[k], edMs[k], (long) n * n, lcsResult[k], edResult[k]));
            csv.append(String.format("LCS,%d,%.6f,%d%n", n, lcsMs[k], lcsResult[k]));
            csv.append(String.format("EditDistance,%d,%.6f,%d%n", n, edMs[k], edResult[k]));
        }
        Files.writeString(new File(resultsDir, "dp_results.csv").toPath(), csv.toString());

        csv = new StringBuilder("algorithm,case,text_length,pattern_length,occurrences,avg_ms,char_comparisons,rk_spurious_hits\n");
        String[] caseNames = {"present", "absent"};
        for (int c = 0; c < cases; c++) {
            md.append("\n### String matching, pattern ").append(caseNames[c])
                    .append(" (pattern length ").append(PATTERN_LENGTH).append(", average of ").append(RUNS).append(" runs)\n\n");
            md.append("| Text length | Occurrences | Naive time (ms) | KMP time (ms) | Rabin-Karp time (ms) | Naive comparisons | KMP comparisons | Rabin-Karp comparisons | Rabin-Karp spurious hits |\n");
            md.append("|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n");
            for (int k = 0; k < TEXT_LENGTHS.length; k++) {
                md.append(String.format("| %d | %d | %.4f | %.4f | %.4f | %d | %d | %d | %d |%n",
                        TEXT_LENGTHS[k], occurrences[c][k],
                        matchMs[0][c][k], matchMs[1][c][k], matchMs[2][c][k],
                        matchCmp[0][c][k], matchCmp[1][c][k], matchCmp[2][c][k], rkSpurious[c][k]));
                for (int a = 0; a < algNames.length; a++) {
                    csv.append(String.format("%s,%s,%d,%d,%d,%.6f,%d,%d%n", algNames[a], caseNames[c], TEXT_LENGTHS[k],
                            PATTERN_LENGTH, occurrences[c][k], matchMs[a][c][k], matchCmp[a][c][k],
                            a == 2 ? rkSpurious[c][k] : 0));
                }
            }
        }
        Files.writeString(new File(resultsDir, "matching_results.csv").toPath(), csv.toString());

        csv = new StringBuilder("input_size,avg_ms,char_comparisons\n");
        md.append("\n### Kasai's algorithm (average of ").append(RUNS).append(" runs, suffix array construction not timed)\n\n");
        md.append("| String length n | Time (ms) | Character comparisons |\n|---:|---:|---:|\n");
        for (int k = 0; k < KASAI_SIZES.length; k++) {
            md.append(String.format("| %d | %.4f | %d |%n", KASAI_SIZES[k], kasaiMs[k], kasaiCmp[k]));
            csv.append(String.format("%d,%.6f,%d%n", KASAI_SIZES[k], kasaiMs[k], kasaiCmp[k]));
        }
        Files.writeString(new File(resultsDir, "kasai_results.csv").toPath(), csv.toString());
        Files.writeString(new File(resultsDir, "tables.md").toPath(), md.toString());

        List<Plotter.Series> s1 = Plotter.newList();
        s1.add(new Plotter.Series("LCS", lcsMs, new Color(31, 119, 180), false));
        s1.add(new Plotter.Series("Edit Distance", edMs, new Color(214, 39, 40), false));
        Plotter.lineChart("LCS and Edit Distance: execution time vs input size",
                "String length n (both strings)", "Average time (ms)", dpX, s1,
                new File(plotsDir, "dp_time_vs_size.png"));

        double[] textX = new double[TEXT_LENGTHS.length];
        for (int k = 0; k < textX.length; k++) textX[k] = TEXT_LENGTHS[k];
        Color[] colors = {new Color(31, 119, 180), new Color(44, 160, 44), new Color(214, 39, 40)};
        List<Plotter.Series> s2 = Plotter.newList();
        for (int c = 0; c < cases; c++) {
            for (int a = 0; a < algNames.length; a++) {
                s2.add(new Plotter.Series(algNames[a] + " (" + caseNames[c] + ")", matchMs[a][c], colors[a], c == 1));
            }
        }
        Plotter.lineChart("String matching: execution time vs text length (pattern length " + PATTERN_LENGTH + ")",
                "Text length", "Average time (ms)", textX, s2,
                new File(plotsDir, "matching_time_vs_text_length.png"));

        List<Plotter.Series> s3 = Plotter.newList();
        s3.add(new Plotter.Series("Kasai LCP", kasaiMs, new Color(148, 103, 189), false));
        Plotter.lineChart("Kasai's algorithm: execution time vs string length",
                "String length n", "Average time (ms)", kasaiX, s3,
                new File(plotsDir, "kasai_time_vs_size.png"));

        System.out.println(md);
        System.out.println("Wrote results/ and plots/ (checksum " + sink + ")");
    }
}
