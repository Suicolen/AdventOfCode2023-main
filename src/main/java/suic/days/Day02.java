package suic.days;

import one.util.streamex.StreamEx;
import suic.Puzzle;
import suic.util.io.FileUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static suic.util.CustomCollectors.teeing3;
import static java.util.stream.Collectors.*;

public class Day02 implements Puzzle<Integer> {

    private List<Game> games;

    @Override
    public void parse() {
        games = FileUtils.readAsStreamEx(getClass().getSimpleName() + "Input.txt")
                .map(Game::parse)
                .toList();
    }

    @Override
    public Integer solvePart1() {
        return games.stream()
                .filter(this::isValidPart1)
                .mapToInt(Game::id)
                .sum();
    }

    private boolean isValidPart1(Game game) {
        return game.sets.stream()
                .allMatch(set -> set.red <= 12 && set.green <= 13 && set.blue <= 14);
    }

    @Override
    public Integer solvePart2() {
        return StreamEx.of(games).map(Game::sets).foldLeft(0, (acc, sets) -> {
            int multiplied = sets.stream().collect(
                    teeing3(
                            collectingAndThen(mapping(GameSet::red, maxBy(Integer::compare)), Optional::orElseThrow),
                            collectingAndThen(mapping(GameSet::green, maxBy(Integer::compare)), Optional::orElseThrow),
                            collectingAndThen(mapping(GameSet::blue, maxBy(Integer::compare)), Optional::orElseThrow),
                            this::power
                    )
            );
            return acc + multiplied;
        });
    }

    private int power(int r, int g, int b) {
        return r * g * b;
    }

    private record Game(int id, List<GameSet> sets) {
        private static Game parse(String str) {
            String[] gameData = str.split(": ");
            int id = Integer.parseInt(gameData[0].substring("Game ".length()));
            List<GameSet> sets = StreamEx.of(gameData)
                    .skip(1)
                    .map(s -> s.split("; "))
                    .flatMap(s -> Arrays.stream(s).map(s1 -> s1.split(", ")))
                    .map(GameSet::parse)
                    .toList();
            return new Game(id, sets);
        }
    }

    private record GameSet(int red, int green, int blue) {
        private static GameSet parse(String[] data) {
            int red = 0;
            int green = 0;
            int blue = 0;
            for (String str : data) {
                String[] split = str.split(", ");
                for (String colorData : split) {
                    String[] split1 = colorData.split(" ");
                    int count = Integer.parseInt(split1[0]);
                    String color = split1[1];
                    switch (color) {
                        case "red" -> red = count;
                        case "green" -> green = count;
                        case "blue" -> blue = count;
                    }
                }
            }
            return new GameSet(red, green, blue);
        }
    }

}
