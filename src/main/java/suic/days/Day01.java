package suic.days;

import suic.Puzzle;
import suic.util.io.FileUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class Day01 implements Puzzle<Integer> {

    private List<String> input;
    private final List<String> WORDS = List.of(
            "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "1", "2", "3", "4", "5", "6", "7", "8", "9"
    );


    @Override
    public void parse() {
        input = FileUtils.read(getClass().getSimpleName() + "Input.txt");
    }

    @Override
    public Integer solvePart1() {
        return input.stream()
                .map(line -> line.replaceAll("\\D", ""))
                .mapToInt(line -> {
                    int firstDigit = Character.getNumericValue(line.charAt(0));
                    int lastDigit = Character.getNumericValue(line.charAt(line.length() - 1));
                    return firstDigit * 10 + lastDigit;
                })
                .sum();
    }

    @Override
    public Integer solvePart2() {
        return input.stream().mapToInt(line -> {
            int firstDigit = findDigit(line, true);
            int lastDigit = findDigit(line, false);
            return firstDigit * 10 + lastDigit;
        }).sum();
    }

    private int findDigit(String line, boolean findFirst) {
        Comparator<Integer> keyComparator = findFirst ? Comparator.naturalOrder() : Comparator.reverseOrder();
        Comparator<String> comparator = Comparator.comparing(findFirst ? line::indexOf : line::lastIndexOf, keyComparator);
        return WORDS.stream()
                .filter(line::contains)
                .min(comparator)
                .map(foundWord -> WORDS.indexOf(foundWord) % 9 + 1)
                .orElseThrow();
    }


}
