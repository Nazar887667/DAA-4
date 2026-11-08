package dp;

public final class EditDistance {
    private EditDistance() {}

    public static int distance(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                int best = dp[i - 1][j - 1] + cost;
                best = Math.min(best, dp[i - 1][j] + 1);
                best = Math.min(best, dp[i][j - 1] + 1);
                dp[i][j] = best;
            }
        }
        return dp[m][n];
    }
}
