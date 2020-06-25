import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Solution {

    public static int[] solution(int[][] m) {
	// Simplest case.
	if (m.length == 1) {
	    return new int[] { 1, 1 };
	}

	// Absorbing Markov Chain. You can read more here:
	// https://brilliant.org/wiki/absorbing-markov-chains
	FractionMatrix normalizedMatrix = normalize(m);
	FractionMatrix r = getR(normalizedMatrix);
	FractionMatrix q = getQ(normalizedMatrix);
	FractionMatrix i = getI(q.size());
	FractionMatrix iqDifference = i.subtract(q);
	FractionMatrix f = iqDifference.invert();
	FractionMatrix fr = f.multiply(r);

	// Formats the result.
	Fraction[] frFirstRow = fr.getRow(0);
	int frFirstRowLength = frFirstRow.length;
	int[] result = new int[frFirstRowLength + 1];

	// Last element is the shared denominator.
	int commonDenominator = (int) Arrays.stream(frFirstRow).map(Fraction::reduce)
		.reduce(Fraction::addWithoutReduction).get().denominator;
	result[result.length - 1] = commonDenominator;

	// Fills the first elements of the array with the numerators.
	for (int j = 0; j < frFirstRowLength; j++) {
	    result[j] = (int) frFirstRow[j].convert(commonDenominator).numerator;
	}

	return result;
    }

    private static FractionMatrix normalize(int[][] m) {
	int[][] normalizedMatrix = new int[m.length][m.length];
	int[] newRowOrders = new int[m.length];
	int absorbingStateCount = 0;

	// Determines the new matrix order, with absorbing (terminal) states first.
	List<Integer> normalState = new ArrayList<Integer>();
	for (int i = 0; i < m.length; i++) {
	    int[] row = m[i];
	    if (isTerminalState(row)) {
		newRowOrders[absorbingStateCount] = i;
		absorbingStateCount++;
	    } else {
		normalState.add(i);
	    }
	}

	// Fills in the missing spots.
	for (int i = 0; i < normalState.size(); i++) {
	    newRowOrders[absorbingStateCount + i] = normalState.get(i);
	}

	// Puts the matrix in the new order.
	for (int i = 0; i < m.length; i++) {
	    for (int j = 0; j < m.length; j++) {
		normalizedMatrix[i][j] = m[newRowOrders[i]][newRowOrders[j]];
	    }
	}

	// Converts the normalized matrix into fractions.
	return new FractionMatrix(normalizedMatrix, absorbingStateCount);
    }

    private static FractionMatrix getR(FractionMatrix normalizedMatrix) {
	int abStates = normalizedMatrix.absorbingStateCount;
	Fraction[][] r = new Fraction[normalizedMatrix.size() - abStates][abStates];
	for (int i = 0; i < r.length; i++) {
	    for (int j = 0; j < abStates; j++) {
		r[i][j] = normalizedMatrix.getRow(abStates + i)[j];
	    }
	}
	return new FractionMatrix(r);
    }

    private static FractionMatrix getQ(FractionMatrix normalizedMatrix) {
	int abStates = normalizedMatrix.absorbingStateCount;
	Fraction[][] q = new Fraction[normalizedMatrix.size() - abStates][normalizedMatrix.size() - abStates];
	for (int i = 0; i < q.length; i++) {
	    for (int j = 0; j < q.length; j++) {
		q[i][j] = normalizedMatrix.getRow(abStates + i)[abStates + j];
	    }
	}
	return new FractionMatrix(q);
    }

    private static FractionMatrix getI(int size) {
	Fraction[][] i = new Fraction[size][size];
	for (int j = 0; j < size; j++) {
	    for (int k = 0; k < size; k++) {
		if (j == k) {
		    i[j][k] = new Fraction(1);
		} else {
		    i[j][k] = new Fraction();
		}
	    }
	}
	return new FractionMatrix(i);
    }

    private static boolean isTerminalState(int[] row) {
	return Arrays.stream(row).sum() == 0;
    }

    private static class Fraction {
	private long numerator, denominator;

	public Fraction() {
	    this(0);
	}

	public Fraction(long num) {
	    this(num, 1);
	}

	public Fraction(long num, long den) {
	    numerator = num;
	    denominator = den;
	}

	public Fraction addAndReduce(Fraction b) {
	    return addWithoutReduction(b).reduce();
	}

	public Fraction addWithoutReduction(Fraction b) {
	    long common = lcd(denominator, b.denominator);
	    Fraction commonA = convert(common);
	    Fraction commonB = b.convert(common);
	    return new Fraction(commonA.numerator + commonB.numerator, common);
	}

	public Fraction subtract(Fraction b) {
	    return addWithoutReduction(b.multiply(new Fraction(-1))).reduce();
	}

	public Fraction multiply(Fraction b) {
	    return new Fraction(numerator * b.numerator, denominator * b.denominator).reduce();
	}

	public Fraction divide(Fraction b) {
	    return new Fraction(numerator * b.denominator, denominator * b.numerator).reduce();
	}

	private long lcd(long denom1, long denom2) {
	    long factor = denom1;
	    while ((denom1 % denom2) != 0)
		denom1 += factor;
	    return denom1;
	}

	private long gcd(long denom1, long denom2) {
	    long factor = denom2;
	    while (denom2 != 0) {
		factor = denom2;
		denom2 = denom1 % denom2;
		denom1 = factor;
	    }
	    return denom1;
	}

	private Fraction convert(long common) {
	    Fraction result = new Fraction();
	    long factor = common / denominator;
	    result.numerator = numerator * factor;
	    result.denominator = common;
	    return result;
	}

	private Fraction reduce() {
	    long common = 0;
	    // get absolute values for numerator and denominator
	    long num = Math.abs(numerator);
	    long den = Math.abs(denominator);
	    // figure out which is less, numerator or denominator
	    if (num > den)
		common = gcd(num, den);
	    else if (num < den)
		common = gcd(den, num);
	    else // if both are the same, don't need to call gcd
		common = num;

	    // set result based on common factor derived from gcd
	    this.numerator = numerator / common;
	    this.denominator = denominator / common;
	    return this;
	}

	public Fraction abs() {
	    return new Fraction(Math.abs(numerator), Math.abs(denominator));
	}

	public boolean isGreaterThan(Fraction other) {
	    long commonDen = lcd(denominator, other.denominator);
	    Fraction convertedOther = other.convert(commonDen);
	    Fraction convertedThis = convert(commonDen);
	    return convertedThis.numerator > convertedOther.numerator;
	}
    }

    private static class FractionMatrix {

	private Fraction[][] matrix;
	public int absorbingStateCount;

	public FractionMatrix(Fraction[][] matrix) {
	    this.matrix = matrix;
	}

	public FractionMatrix(int[][] matrix, int absorbingStateCount) {
	    // Assumes the input is a square matrix.
	    Fraction[][] convertedMatrix = new Fraction[matrix.length][matrix.length];
	    for (int i = 0; i < matrix.length; i++) {
		int rowSum = Arrays.stream(matrix[i]).sum();
		for (int j = 0; j < matrix.length; j++) {
		    convertedMatrix[i][j] = new Fraction(matrix[i][j], rowSum);
		}
	    }
	    this.matrix = convertedMatrix;
	    this.absorbingStateCount = absorbingStateCount;
	}

	public FractionMatrix multiply(FractionMatrix r) {
	    int l = size();
	    int m = matrix[0].length;
	    int n = r.matrix[0].length;
	    Fraction[][] result = new Fraction[l][n];
	    Arrays.stream(result).forEach(a -> Arrays.fill(a, new Fraction()));
	    for (int i = 0; i < l; ++i)
		for (int j = 0; j < n; ++j)
		    for (int k = 0; k < m; ++k)
			result[i][j] = result[i][j].addAndReduce(matrix[i][k].multiply(r.matrix[k][j]));
	    return new FractionMatrix(result);
	}

	public FractionMatrix invert() {
	    int n = matrix.length;
	    Fraction x[][] = new Fraction[n][n];
	    Fraction b[][] = new Fraction[n][n];
	    Arrays.stream(b).forEach(a -> Arrays.fill(a, new Fraction()));
	    int index[] = new int[n];
	    for (int i = 0; i < n; ++i)
		b[i][i] = new Fraction(1);

	    // Transform the matrix into an upper triangle
	    gaussian(matrix, index);

	    // Update the matrix b[i][j] with the ratios stored
	    for (int i = 0; i < n - 1; ++i)
		for (int j = i + 1; j < n; ++j)
		    for (int k = 0; k < n; ++k)
			b[index[j]][k] = b[index[j]][k].subtract(matrix[index[j]][i].multiply(b[index[i]][k]));

	    // Perform backward substitutions
	    for (int i = 0; i < n; ++i) {
		x[n - 1][i] = b[index[n - 1]][i].divide(matrix[index[n - 1]][n - 1]);

		for (int j = n - 2; j >= 0; --j) {
		    x[j][i] = b[index[j]][i];
		    for (int k = j + 1; k < n; ++k) {
			x[j][i] = x[j][i].subtract(matrix[index[j]][k].multiply(x[k][i]));
		    }
		    x[j][i] = x[j][i].divide(matrix[index[j]][j]);
		}
	    }

	    return new FractionMatrix(x);
	}

	// Method to carry out the partial-pivoting Gaussian
	// elimination. Here index[] stores pivoting order.
	private static void gaussian(Fraction a[][], int index[]) {
	    int n = index.length;
	    Fraction c[] = new Fraction[n];

	    // Initialize the index
	    for (int i = 0; i < n; ++i)
		index[i] = i;

	    // Find the rescaling factors, one from each row
	    for (int i = 0; i < n; ++i) {
		Fraction c1 = new Fraction();
		for (int j = 0; j < n; ++j) {
		    Fraction c0 = a[i][j].abs();
		    if (c0.isGreaterThan(c1))
			c1 = c0;
		}
		c[i] = c1;
	    }

	    // Search the pivoting element from each column
	    int k = 0;
	    for (int j = 0; j < n - 1; ++j) {
		Fraction pi1 = new Fraction();
		for (int i = j; i < n; ++i) {
		    Fraction pi0 = a[index[i]][j].abs();
		    pi0 = pi0.divide(c[index[i]]);
		    if (pi0.isGreaterThan(pi1)) {
			pi1 = pi0;
			k = i;
		    }
		}

		// Interchange rows according to the pivoting order
		int itmp = index[j];
		index[j] = index[k];
		index[k] = itmp;
		for (int i = j + 1; i < n; ++i) {
		    Fraction pj = a[index[i]][j].divide(a[index[j]][j]);

		    // Record pivoting ratios below the diagonal
		    a[index[i]][j] = pj;

		    // Modify other elements accordingly
		    for (int l = j + 1; l < n; ++l)
			a[index[i]][l] = a[index[i]][l].subtract(pj.multiply(a[index[j]][l]));
		}
	    }
	}

	public FractionMatrix subtract(FractionMatrix q) {
	    Fraction[][] result = new Fraction[matrix.length][matrix.length];
	    for (int j = 0; j < matrix.length; j++) {
		for (int k = 0; k < matrix.length; k++) {
		    result[j][k] = matrix[j][k].subtract(q.matrix[j][k]);
		}
	    }
	    return new FractionMatrix(result);
	}

	public int size() {
	    return matrix.length;
	}

	public Fraction[] getRow(int index) {
	    return matrix[index];
	}

    }

    public static void main(String[] args) {
	int[][] mOne = { { 0, 2, 1, 0, 0 }, { 0, 0, 0, 3, 4 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 },
		{ 0, 0, 0, 0, 0 } };
	int[][] mTwo = { { 0, 1, 0, 0, 0, 1 }, { 4, 0, 0, 3, 2, 0 }, { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 },
		{ 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 } };
	int[] outputOne = { 7, 6, 8, 21 };
	int[] outputTwo = { 0, 3, 2, 9, 14 };

	assert Arrays.equals(solution(mOne), outputOne);
	assert Arrays.equals(solution(mTwo), outputTwo);
    }

}