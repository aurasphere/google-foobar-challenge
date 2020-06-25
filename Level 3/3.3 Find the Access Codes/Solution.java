public class Solution {

    public static int solution(int[] l) {
	// Simply iterates through the numbers and counts the triplets.
	int luckyTripletsCount = 0;
	for (int i = 0; i < l.length - 2; i++) {
	    int num1 = l[i];
	    for (int j = i + 1; j < l.length - 1; j++) {
		int num2 = l[j];
		if (num2 % num1 != 0)
		    continue;
		for (int k = j + 1; k < l.length; k++) {
		    int num3 = l[k];
		    if (num3 % num2 != 0)
			continue;
		    else
			luckyTripletsCount++;
		}
	    }
	}
	return luckyTripletsCount;
    }

    public static void main(String... args) {
	int[] lOne = new int[] { 1, 1, 1 };
	int[] lTwo = new int[] { 1, 2, 3, 4, 5, 6 };
	int outputOne = 1;
	int outputTwo = 3;

	assert solution(lOne) == outputOne;
	assert solution(lTwo) == outputTwo;
    }

}