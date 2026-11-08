package matching;

import java.util.ArrayList;
import java.util.List;

public final class RabinKarpMatcher implements StringMatcher {
    private static final long DEFAULT_MOD = 1_000_000_007L;
    private static final long BASE = 256L;

    private final long mod;
    private long comparisons;
    private long spuriousHits;

    public RabinKarpMatcher() { this(DEFAULT_MOD); }

    public RabinKarpMatcher(long mod) { this.mod = mod; }

    @Override
    public List<Integer> search(String text, String pattern) {
        comparisons = 0;
        spuriousHits = 0;
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0 || m > n) return result;

        long highPower = 1;
        for (int i = 0; i < m - 1; i++) highPower = (highPower * BASE) % mod;

        long patternHash = 0;
        long windowHash = 0;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % mod;
            windowHash = (windowHash * BASE + text.charAt(i)) % mod;
        }

        for (int i = 0; i <= n - m; i++) {
            if (patternHash == windowHash) {
                boolean equal = true;
                for (int j = 0; j < m; j++) {
                    comparisons++;
                    if (text.charAt(i + j) != pattern.charAt(j)) { equal = false; break; }
                }
                if (equal) result.add(i); else spuriousHits++;
            }
            if (i < n - m) {
                long removed = (text.charAt(i) * highPower) % mod;
                windowHash = ((windowHash - removed + mod) % mod * BASE + text.charAt(i + m)) % mod;
            }
        }
        return result;
    }

    public long getSpuriousHits() { return spuriousHits; }

    @Override public long getComparisons() { return comparisons; }
    @Override public String name() { return "Rabin-Karp"; }
}
