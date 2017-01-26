import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
*  This class is to do the Two-Way external merge sort.
*/
public class TwoWayExternalMergeSort {
	private static final int COMMA = 44;
	private static final int BYTESIZE = 4099; // for ,100,
	private static final int PAGESIZE = 4096;
	private static final int WAYNUM = 2;
	private static final int DATALENGTH = 3;
	private static final Charset UTF8_CHARSET = Charset.forName("UTF8");
	private static final Pattern PATTERN = Pattern.compile("\\d+");
	private static final byte[] outputBuffer = new byte[PAGESIZE];
	private static int outputIndex = 0;
	private static final byte[][] inputBuffer = new byte[WAYNUM][BYTESIZE];
	private static final int[] inputIndex = new int[WAYNUM];
	private static final FileInputStream[] fis = new FileInputStream[WAYNUM];
	private static FileOutputStream fos = null;
	private static final int[] fileIndex = new int[WAYNUM];
	private static final byte[][] compareNum = new byte[WAYNUM][DATALENGTH];
	private static final int[] compareNumIndex = new int[WAYNUM];
	private static final int[] temp = new int[WAYNUM];  // the number of byte read from the specific file
	private static final int[] num = new int[WAYNUM]; // the number that the reference point to
	private static final boolean[] read = new boolean[WAYNUM];
	private static int previousFileNumber = 0;
	private static int currentFileNumber = 0;
	private static int previousPassNum = -1;
	private static int currentPassNum = 0;
	
	/**
	 *  public static function to do the external Merge Sort
	 *  @param fileName is name of the large file need to be sorted
	*/
	public static void externalMergeSort(String fileName) {
		makeDir("temporary");
		makePassDir(currentPassNum);
		Pass0.pass0(fileName);
		currentFileNumber = Pass0.getCurrentFileNumber();
		normalPass();
	}
	
	/**
	 *  private static core function to deal with the passes except Pass0
	*/
	private static void normalPass() {
		while (previousFileNumber != 1) {
			doBeforeEachPass();
			while (fileIndex[0] < previousFileNumber) {
				dealWithEachOutputFile();
			} // end while
		} // end while
	}
	
	/**
	 *  private static core function to do the external Merge Sort to generate each output file
	*/
	private static void dealWithEachOutputFile() {
		try {
			for (int i = 0; i < WAYNUM; i++) {
				fis[i] = new FileInputStream("temporary/PASS" + previousPassNum + "/pass" + previousPassNum + "-" + fileIndex[i] + ".txt");
			}
			fos = new FileOutputStream("temporary/PASS" + currentPassNum + "/pass" + currentPassNum + "-" + currentFileNumber++ + ".txt");
			// clear from the last merge
			cleanBeforeEachMerge();
			mergeTwoFiles();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				for (int i = 0; i < WAYNUM; i++) {
					fis[i].close();
				}
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < WAYNUM; i++) {
			fileIndex[i] += WAYNUM;
		}
	}
	
	/**
	 *  private static core function to merge two files
	 *  @exception IOException - thrown for any IOException
	*/
	private static void mergeTwoFiles() throws IOException {
		while (temp[0] != -1 && temp[1] != -1) {  // two merged files are all need to be merged
			for (int i = 0; i < WAYNUM; i++) {
				updateInputBuffer(i);
			}
			mergeTwoPages();
		}  // end while

		if (fileIndex[0] == previousFileNumber - 1) {  // the second file is an empty file
			copyTheLeft(0);
		} else if (inputIndex[0] >= temp[0]) {  // file1 has been finished
			copyOneFile(1);
		} else {  // file[1] has been finished
			copyOneFile(0);
		}
	}
	
	/**
	 *  private static core function to merge two pages
	 *  @exception IOException - thrown for any IOException
	*/
	private static void mergeTwoPages() throws IOException {
		while (inputIndex[0] < temp[0] && inputIndex[1] < temp[1]) {
			for (int i = 0; i < WAYNUM; i++) {
				readNextInteger(i);
			}

			if (num[0] <= num[1]) {
				putIntoOutputBuffer(0);
			} else { // num[0] > num[1]
				putIntoOutputBuffer(1);
			}
		}  // end while
	}
	
	/**
	 *  private static function to add the number to outputBuffer
	 *  @param index is 0 or 1
	*/
	private static void addToOutputBuffer(int index) {
		for (int i = 0; i < compareNumIndex[index]; i++) {
			outputBuffer[outputIndex++] = compareNum[index][i];
		}
		outputBuffer[outputIndex++] = 44; // add ','
	}
	
	/**
	 *  private static function to add the number to outputBuffer
	 *  @param index is 0 or 1
	 *  @param start
	 *  @param end
	*/
	private static int addToOutputBuffer(int index, int start, int end) {
		for (int i = start; i < end; i++) {
			outputBuffer[outputIndex++] = inputBuffer[index][i];
		}
		return end; // inputIndex[index]
	}
	
	/**
	 * clear outputBuffer
	 */
	private static void clearOutputBuffer() {
		outputIndex = 0; // important!
		Arrays.fill(outputBuffer, (byte) 0);  // important!
	}
	
	/**
	 * private function to clear compareNumIndex[i] array and compareNumIndex[i]
	 * @param index is 0 or 1
	 * @return the current compareNumIndex[i]
	 */
	private static int clearPointer(int index) {
		compareNumIndex[index] = 0;
		Arrays.fill(compareNum[index], (byte) 0);
		return 0;  // index should be 0
	}
	
	/**
	 * private function to clear inputBuffer and inputIndex
	 * @param index is 0 or 1
	 */
	private static void clearInputBuffer(int index) {
		inputIndex[index] = 0;
		Arrays.fill(inputBuffer[index], (byte) 0);
	}
	
	/**
	 * private function to do some cleanup before each pass
	 */
	private static void doBeforeEachPass() {
		previousFileNumber = currentFileNumber;
		currentFileNumber = 0;
		previousPassNum++;
		currentPassNum++;
		makeupEvenFiles();
		for (int i = 0; i < WAYNUM; i++) {
			fileIndex[i] = i;
		}
		makePassDir(currentPassNum);
	}
	
	/**
	 * private function to do some cleanup before each merge
	 */
	private static void cleanBeforeEachMerge() {
		clearOutputBuffer();
		for (int i = 0; i < WAYNUM; i++) {
			compareNumIndex[i] = clearPointer(i);
			temp[i] = 0;
			read[i] = true; // very important! don't use iterator for-loop!
			clearInputBuffer(i);
		}
	}
	
	/**
	 * read the next bytes of number from inputBuffer to compareNum
	 * @param index is 0 or 1
	 * @return the index of that bytes
	 */
	private static int readNext(int index) {
		while (inputBuffer[index][inputIndex[index]] != COMMA) {
			compareNum[index][compareNumIndex[index]++] = inputBuffer[index][inputIndex[index]++];
		}
		inputIndex[index]++; // pass ','
		return compareNumIndex[index];
	}
	
	/**
	 * read the next integer from inputBuffer to compareNum
	 * @param index is 0 or 1
	 * @return true for need to read, false for no need to read
	 */
	private static boolean readNextInteger(int index) {
		if (!read[index]) {
			return false;  // don't need to read
		}
		compareNumIndex[index] = clearPointer(index);  // important
		compareNumIndex[index] = readNext(index);
		num[index] = getNumber(index);
		read[index] = false;
		return true;
	}
	
	/**
	 * get the integer number from UTF8 bytes
	 * @param index is 0 or 1
	 * @return the integer number
	 */
	private static int getNumber(int index) {
		int num = 0;
		String s = new String(compareNum[index], UTF8_CHARSET);
		Matcher matcher = PATTERN.matcher(s);
		if (matcher.find()) {
			num = Integer.parseInt(matcher.group(0));
		}
		return num;
	}
	
	/**
	 * directly copy the file to the outputFile by inputBuffer or outputBuffer
	 * @param index is 0 or 1
	 * @throws IOException
	 */
	private static void copyOneFile(int index)
		throws IOException {
		if (checkIfOutputBufferIsFull(compareNumIndex[index])) {  // outputBuffer full // important +1 and not <=
			writeToDisk();
		} 
		addToOutputBuffer(index);
		copyTheLeft(index);
	}
	
	/**
	 * directly copy the file to the outputFile by inputBuffer or outputBuffer
	 * @param index is 0 or 1
	 * @throws IOException
	 */
	private static void copyTheLeft(int index) 
		throws IOException {
		while (temp[index] != -1) {
			updateInputBuffer(index);
		
			int leftFromInputBuffer = temp[index] - inputIndex[index];
			int leftFromOutputBuffer = PAGESIZE - outputIndex;
			if (leftFromOutputBuffer < leftFromInputBuffer) {  // outputBuffer could not hold the left
				inputIndex[index] = addToOutputBuffer(index, inputIndex[index], inputIndex[index] + leftFromOutputBuffer);
				writeToDisk();
			}
			inputIndex[index] = addToOutputBuffer(index, inputIndex[index], temp[index]);
			writeToDisk();
		}
	}
	
	/**
	 * check if the outputBuffer is full
	 * @param compareIndex
	 * @return true or false
	 */
	private static boolean checkIfOutputBufferIsFull(int compareIndex) {
		return (outputIndex + compareIndex + 1) >= PAGESIZE;
	}
	
	/**
	 * write the outputBuffer to disk
	 * @throws IOException
	 */
	private static void writeToDisk() throws IOException {
		fos.write(outputBuffer, 0, outputIndex);
		clearOutputBuffer();
	}
	
	/**
	 * put the number into outputBuffer
	 * @param index is 0 or 1
	 * @throws IOException
	 */
	private static void putIntoOutputBuffer(int index) throws IOException {
		if (checkIfOutputBufferIsFull(compareNumIndex[index])) {  // outputBuffer full // important +1 and not <=
			writeToDisk();
		} 
		addToOutputBuffer(index);
		compareNumIndex[index] = clearPointer(index);
		read[index] = true;
	}
	
	/**
	 * update inputBuffer
	 * @param index is 0 or 1
	 * @return true or false
	 * @throws IOException
	 */
	private static boolean updateInputBuffer(int index) throws IOException {
		if (inputIndex[index] < temp[index]) {
			return false;
		}
		clearInputBuffer(index);
		temp[index] = fis[index].read(inputBuffer[index], 0, PAGESIZE);
		while (temp[index] >= PAGESIZE && inputBuffer[index][temp[index] - 1] != COMMA) {  // important >= not ==
			fis[index].read(inputBuffer[index], temp[index]++, 1);
		}
		return true;
	}
	
	/**
	 * add one additional empty file if the total previous pass file number is odd
	 * @return true or false
	 */
	private static boolean makeupEvenFiles() {
		if (previousFileNumber % WAYNUM == 0) { // if even number of files
			return false;  // don't need to add an additional file
		}
		try {
			// add an even number for merge
			File file = new File("temporary/PASS" + previousPassNum + "/pass" + previousPassNum + "-" + previousFileNumber + ".txt");
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;  // need to add an additional file
	}
	
	/**
	 * make the directory for temporary files
	 * @param passNum
	 */
	private static void makePassDir(int passNum) {
		makeDir("temporary/PASS" + passNum);
	}
	
	/**
	 * make the directory for temporary files
	 * @param path
	 */
	private static void makeDir(String path) {
		File dir = new File(path);
		try {
			dir.mkdir();
		} catch (SecurityException se) {
			se.printStackTrace();
		}
	}
}