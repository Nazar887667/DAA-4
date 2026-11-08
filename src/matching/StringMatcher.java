package matching;

import java.util.List;

public interface StringMatcher {
    List<Integer> search(String text, String pattern);

    long getComparisons();

    String name();
}
