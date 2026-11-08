import dp.EditDistance;
import dp.LCS;
import matching.KMPMatcher;
import matching.NaiveMatcher;
import matching.RabinKarpMatcher;
import matching.StringMatcher;
import suffix.Kasai;
import suffix.SuffixArray;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class AllTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        lcsTests();
        editDistanceTests();
        matchingTests();
        kasaiTests();
        System.out.println();
        System.out.println("Passed: " + passed + ", Failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    private static void check(String name, Object expected, Object actual) {
        if (expected.equals(actual)) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name + "  expected=" + expected + " actual=" + actual);
        }
    }

    private static List<Integer> list(Integer... values) { return Arrays.asList(values); }

    private static void checkMatchers(String name, String text, String pattern, List<Integer> expected) {
        StringMatcher[] matchers = {new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher()};
        for (StringMatcher m : matchers) {
            check(name + " [" + m.name() + "]", expected, m.search(text, pattern));
        }
    }

    private static void lcsTests() {
        check("LCS identical strings", 9, LCS.length("algorithm", "algorithm"));
        check("LCS completely different strings", 0, LCS.length("abc", "xyz"));
        check("LCS first string empty", 0, LCS.length("", "abc"));
        check("LCS second string empty", 0, LCS.length("abc", ""));
        check("LCS both empty", 0, LCS.length("", ""));
        check("LCS general case (ABCBDAB, BDCABA)", 4, LCS.length("ABCBDAB", "BDCABA"));
    }

    private static void editDistanceTests() {
        check("Edit identical strings", 0, EditDistance.distance("algorithm", "algorithm"));
        check("Edit first string empty", 3, EditDistance.distance("", "abc"));
        check("Edit second string empty", 3, EditDistance.distance("abc", ""));
        check("Edit substitution (cat -> cut)", 1, EditDistance.distance("cat", "cut"));
        check("Edit insertion (cat -> cart)", 1, EditDistance.distance("cat", "cart"));
        check("Edit deletion (cart -> cat)", 1, EditDistance.distance("cart", "cat"));
        check("Edit general case (kitten -> sitting)", 3, EditDistance.distance("kitten", "sitting"));
    }

    private static void matchingTests() {
        checkMatchers("Match at beginning", "abcdefgh", "abc", list(0));
        checkMatchers("Match in middle", "xxabcxx", "abc", list(2));
        checkMatchers("Match at end", "xxxxabc", "abc", list(4));
        checkMatchers("Multiple occurrences", "abababab", "ab", list(0, 2, 4, 6));
        checkMatchers("Overlapping occurrences", "aaaaa", "aa", list(0, 1, 2, 3));
        checkMatchers("Pattern absent", "abcdef", "xyz", list());
        checkMatchers("Pattern longer than text", "ab", "abcd", list());
        checkMatchers("Empty pattern (no occurrences)", "abc", "", list());
        checkMatchers("Empty text", "", "a", list());

        Random rnd = new Random(1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) sb.append((char) ('a' + rnd.nextInt(4)));
        String text = sb.toString();
        String pattern = text.substring(100, 105);
        List<Integer> expected = new NaiveMatcher().search(text, pattern);
        RabinKarpMatcher smallMod = new RabinKarpMatcher(5);
        check("Rabin-Karp with forced collisions returns correct indices", expected, smallMod.search(text, pattern));
        check("Rabin-Karp with forced collisions had spurious hits", true, smallMod.getSpuriousHits() > 0);
    }

    private static void kasaiTests() {
        String s = "banana";
        int[] expectedSa = {5, 3, 1, 0, 4, 2};
        int[] expectedLcp = {0, 1, 3, 0, 0, 2};
        check("Suffix array of banana", Arrays.toString(expectedSa), Arrays.toString(SuffixArray.build(s)));
        check("Kasai LCP of banana (given manual suffix array)", Arrays.toString(expectedLcp),
                Arrays.toString(new Kasai().build(s, expectedSa)));
    }
}
