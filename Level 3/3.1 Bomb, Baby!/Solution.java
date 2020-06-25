import java.math.BigInteger;

public class Solution {

    public static String solution(String m, String f) {
	// Working with big integers since the values may be out of range for a long.
	BigInteger mBig = new BigInteger(m);
	BigInteger fBig = new BigInteger(f);
	BigInteger stepCounter = BigInteger.ZERO;
	BigInteger batchStepCounter = BigInteger.ONE;

	// Basically, we reverse engineer the number by finding the biggest of the two
	// and subtracting the smallest until we reach 1.
	while (true) {
	    switch (mBig.compareTo(fBig)) {
	    case 1:
		// m is bigger
		if (fBig.signum() != 1) {
		    return "impossible";
		}
		batchStepCounter = getBatchStepCounter(mBig, fBig);
		mBig = mBig.subtract(fBig.multiply(batchStepCounter));
		break;
	    case 0:
		// If the numbers are equals but not one, then the result is impossible since
		// there's no previous operation that could lead to this state.s
		if (!mBig.equals(BigInteger.ONE)) {
		    return "impossible";
		}
		// Return the number of steps.
		return stepCounter.toString();
	    case -1:
		// f is bigger
		if (mBig.signum() != 1) {
		    return "impossible";
		}
		batchStepCounter = getBatchStepCounter(fBig, mBig);
		fBig = fBig.subtract(mBig.multiply(batchStepCounter));
		break;
	    }

	    // Increase the counter of steps.
	    stepCounter = stepCounter.add(batchStepCounter);
	}
    }

    private static BigInteger getBatchStepCounter(BigInteger bigger, BigInteger smaller) {
	// Check if we can subtract the smaller number more than one time to optimize
	// the process.
	if (smaller.equals(BigInteger.ONE)) {
	    return BigInteger.ONE;
	}
	BigInteger result = bigger.divide(smaller);

	// If the number is negative, we can only subtract one time.
	return result.signum() == 1 ? result : BigInteger.ONE;
    }

    public static void main(String... args) {
	String mOne = "2", fOne = "1", outputOne = "1";
	String mTwo = "4", fTwo = "7", outputTwo = "4";

	assert solution(mOne, fOne).equals(outputOne);
	assert solution(mTwo, fTwo).equals(outputTwo);
    }

}