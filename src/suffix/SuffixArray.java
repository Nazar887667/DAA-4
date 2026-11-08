package suffix;

import java.util.Arrays;

public final class SuffixArray {
    private SuffixArray() {}

    public static int[] build(String s) {
        int n = s.length();
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (x, y) -> compareSuffixes(s, x, y));
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) sa[i] = idx[i];
        return sa;
    }

    private static int compareSuffixes(String s, int x, int y) {
        int n = s.length();
        while (x < n && y < n) {
            char cx = s.charAt(x);
            char cy = s.charAt(y);
            if (cx != cy) return Character.compare(cx, cy);
            x++;
            y++;
        }
        return Integer.compare(n - x, n - y);
    }
}
