import java.util.Random;

/**
*  This class is to do the quick sort for pass0 pages
*/
@SuppressWarnings("rawtypes")
public class QuickSort {
	
	public static void quickSort(Comparable[] a) {
		shuffle(a);  // avoid the worst case array a has already been sorted
		quickSort(a, 0, a.length - 1);
	}
	
	
	private static void quickSort(Comparable[] a, int lo, int hi) {
		if (hi <= lo) return;
		int j = partition(a, lo, hi);
		quickSort(a, lo, j - 1);
		quickSort(a, j + 1, hi);  // This is important: j + 1
	}
	
	private static int partition(Comparable[] a, int lo, int hi) {
		// Partition into a[lo..i-1], a[i], a[i+1..hi].
		Comparable v = a[lo];
		int i = lo, j = hi + 1;		// This is important: + 1
		while (true) {
			while (less(a[++i], v) && i < hi) {}
			while (less(v, a[--j]) && j > lo) {}
			if (i >= j) break;
				exch(a, i, j);
		}
		if (j != lo)
			exch(a, lo, j);
						
		return j;
	}
	
	private static void shuffle(Comparable[] a) {
		Random random = new Random();
		int size = a.length;
		for (int i = 0; i < size; i++) {
			exch(a, i, random.nextInt(size));
		}
	}
	
	private static boolean less(Comparable v, Comparable w) {
		@SuppressWarnings("unchecked")	// important
		boolean result = v.compareTo(w) < 0;
		return result; 
	}
	
	private static void exch(Comparable[] a, int i, int j) {
		Comparable t = a[j]; a[j] = a[i]; a[i] = t;
	}
}
