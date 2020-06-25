import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Solution {

    public static int solution(int src, int dest) {
        int movesCount = 0;
        Set<Integer> visitedSquares = Collections.singleton(src);
        // Simple breadth-first search algorithm.
        while (!visitedSquares.contains(dest)) {
            visitedSquares = visitedSquares.parallelStream()
                .map(Solution::getNextMovesFromSquare)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toSet());
            movesCount++;
        }
        return movesCount;
    }

    static Set<Integer> getNextMovesFromSquare(int square) {
        int modSquare = square % 8;
        Set<Integer> nextMoves = new HashSet<>();

        // Adds the valid moves according to our current position.
        if (modSquare < 7) {
            if (modSquare < 6) {
                nextMoves.add(square + 10);
                nextMoves.add(square - 6);
            }
            nextMoves.add(square + 17);
            nextMoves.add(square - 15);
        }
        if (modSquare > 0) {
            if (modSquare > 1) {
                nextMoves.add(square - 10);
                nextMoves.add(square + 6);
            }
            nextMoves.add(square - 17);
            nextMoves.add(square + 15);
        }
        // Purge moves outside the checkboard.
        nextMoves.removeIf(n -> n > 63 || n < 0);
        return nextMoves;
    }

    public static void main(String... args) {
        int srcOne = 19, destOne = 36, outputOne = 1;
        int srcTwo = 0, destTwo = 1, outputTwo = 3;

        assert solution(srcOne, destOne) == outputOne;
        assert solution(srcTwo, destTwo) == outputTwo;
    }

}