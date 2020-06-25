import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class Solution {

    public static int[] solution(int[][] times, int timeLimit) {
	// We start by checking for minimum distances and negative cycles
	// with Bellman-Ford algorithm.
	int[][] minDistances = bellmanFord(times);
	if (minDistances == null) {
	    // Negative cycle. We can save all the bunnies.
	    return IntStream.range(0, times.length - 2).toArray();
	}

	// Now we just brute force.

	// Solutions sorted by score and bunny index.
	TreeSet<List<Integer>> possibleSolutions = new TreeSet<>(Solution::solutionSortingComparator);

	// Powerset of bunnies.
	Set<Set<Integer>> bunniesPowerset = powerset(IntStream.range(0, times.length).toArray());
	for (Set<Integer> currentSet : bunniesPowerset) {
	    Set<List<Integer>> permutations = permutations(new ArrayList<>(currentSet));

	    for (List<Integer> candidateSolution : permutations) {
		// Skip empty permutations and permutations that does not start at 0 or that
		// doesn't end at the bulkhead.
		if (candidateSolution.isEmpty() || !candidateSolution.get(0).equals(0)
			|| !candidateSolution.get(candidateSolution.size() - 1).equals(times.length - 1)) {
		    continue;
		}

		// This is a possible solution, we compute the time needed.
		int solutionTimeLeft = timeLimit;
		for (int i = 0, j = 1; j < candidateSolution.size(); i++, j++) {
		    int initialPosition = candidateSolution.get(i);
		    int finalPosition = candidateSolution.get(j);
		    int moveCost = minDistances[initialPosition][finalPosition];
		    solutionTimeLeft -= moveCost;
		}

		// If we have time left, the solution is valid.
		if (solutionTimeLeft >= 0) {
		    Collections.sort(candidateSolution);
		    possibleSolutions.add(candidateSolution);
		}
	    }
	}

	// Formats the best possible solution and returns it.
	if (possibleSolutions.isEmpty()) {
	    return new int[] {};
	}
	List<Integer> bestSolution = possibleSolutions.last();
	return bestSolution.subList(1, bestSolution.size() - 1).stream().mapToInt(i -> i - 1).toArray();
    }

    private static int solutionSortingComparator(List<Integer> a, List<Integer> b) {
	// Sorts solutions by giving precedences to the ones with more elements and then
	// the ones with the lowest bunny indexes.
	if (a.size() != b.size()) {
	    return a.size() - b.size();
	}
	for (int i = 0; i < a.size(); i++) {
	    int ai = a.get(i);
	    int bi = b.get(i);
	    if (ai == bi) {
		continue;
	    }
	    return bi - ai;
	}
	return 0;
    }

    private static int[][] bellmanFord(int[][] bunnies) {
	// Simple implementation of the Bellman Ford algorithm. For more informations,
	// check this website: https://brilliant.org/wiki/bellman-ford-algorithm
	int[][] minDistances = new int[bunnies.length][bunnies.length];
	for (int i = 0; i < bunnies.length; i++) {
	    int[] minDistancesFromNodeI = new int[bunnies.length];

	    // Since the max time is 999 a node with distance 1000 is always unreacheable.
	    // We use this value as infinity to avoid overflows.
	    Arrays.fill(minDistancesFromNodeI, 1000);

	    minDistancesFromNodeI[i] = 0;
	    for (int u = 0; u < bunnies.length - 1; u++) {
		for (int v = 0; v < bunnies.length; v++) {
		    for (int k = 0; k < bunnies.length; k++) {
			if (v == k) {
			    continue;
			}
			relax(v, k, minDistancesFromNodeI, bunnies);
		    }
		}
	    }

	    for (int u = 0; u < bunnies.length; u++) {
		for (int v = 0; v < bunnies.length; v++) {
		    if (u == v)
			continue;
		    if (minDistancesFromNodeI[v] > minDistancesFromNodeI[u] + weight(u, v, bunnies)) {
			// Negative cycle.
			return null;
		    }
		}
	    }
	    minDistances[i] = minDistancesFromNodeI;
	}
	return minDistances;
    }

    private static void relax(int u, int v, int[] distances, int[][] bunnies) {
	int tentativeDistance = distances[u] + weight(u, v, bunnies);
	if (distances[v] > tentativeDistance) {
	    distances[v] = tentativeDistance;
	}
    }

    private static int weight(int u, int v, int[][] bunnies) {
	return bunnies[u][v];
    }

    private static Set<List<Integer>> permutations(List<Integer> originalSet) {
	// Heap's algorithm
	int[] indexes = new int[originalSet.size()];
	Set<List<Integer>> permutations = new HashSet<>();
	List<Integer> currentSet = originalSet;
	permutations.add(currentSet);
	for (int i = 0; i < originalSet.size();) {
	    if (indexes[i] < i) {
		currentSet = new ArrayList<>(currentSet);
		Collections.swap(currentSet, i % 2 == 0 ? 0 : indexes[i], i);
		permutations.add(currentSet);
		indexes[i]++;
		i = 0;
	    } else {
		indexes[i] = 0;
		i++;
	    }
	}
	return permutations;
    }

    private static Set<Set<Integer>> powerset(int[] set) {
	int setSize = set.length;
	int powersetSize = (int) Math.pow(2, setSize);
	Set<Set<Integer>> powerset = new HashSet<>();
	for (int i = 0; i < powersetSize; i++) {
	    Set<Integer> currentSet = new HashSet<>();
	    for (int j = 0; j < setSize; j++) {
		if (((i >> j) & 1) == 1) {
		    currentSet.add(set[j]);
		}
	    }
	    powerset.add(currentSet);
	}
	return powerset;
    }

    public static void main(String... args) {
	int[][] timesOne = { { 0, 1, 1, 1, 1 }, { 1, 0, 1, 1, 1 }, { 1, 1, 0, 1, 1 }, { 1, 1, 1, 0, 1 },
		{ 1, 1, 1, 1, 0 } };
	int timeLimitOne = 3;
	int[] solutionOne = { 0, 1 };
	int[][] timesTwo = { { 0, 2, 2, 2, -1 }, { 9, 0, 2, 2, -1 }, { 9, 3, 0, 2, -1 }, { 9, 3, 2, 0, -1 },
		{ 9, 3, 2, 2, 0 } };
	int timeLimitTwo = 1;
	int[] solutionTwo = { 1, 2 };

	assert Arrays.equals(solution(timesOne, timeLimitOne), solutionOne);
	assert Arrays.equals(solution(timesTwo, timeLimitTwo), solutionTwo);
    }

}