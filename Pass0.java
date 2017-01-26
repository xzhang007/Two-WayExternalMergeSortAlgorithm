import java.io.*;

/**
*  This class is to do Pass0
*/
public class Pass0 {
	private static final int COMMA = 44;
	private static final int BYTESIZE = 4099; // for ,100,
	private static final int PAGESIZE = 4096;
	private static int currentFileNumber = 0;
	
	/**
	 * static function to do Pass0
	 * @param fileName
	 */
	static void pass0(String fileName) {
		separatePages(fileName);
		quickSort();
	}
	
	/**
	 * separate the given file into several pages
	 * @param fileName
	 */
	private static void separatePages(String fileName) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		try {
			fis = new FileInputStream(fileName);
			byte[] buffer = new byte[BYTESIZE];
			while (true) {
				int temp = fis.read(buffer, 0, PAGESIZE);
				if (temp == -1) {
					break;
				}
				while (buffer[temp - 1] != COMMA) {
					fis.read(buffer, temp++, 1);
				}
				fos = new FileOutputStream("temporary/PASS0/pass0-" + currentFileNumber++ + ".txt");
				fos.write(buffer, 0, temp);
				fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * do the quicksort for all of the pages in the directory
	 */
	private static void quickSort() {
		File dir = new File("temporary/PASS0");
		for (String file : dir.list()) {
			if (!file.matches("pass0-\\d+.txt")) {
				continue;
			}
			String fileName = "temporary/PASS0/" + file;
			quickSort(fileName);
		}
	}
	
	/**
	 * do the quicksort for each page
	 * @param fileName
	 */
	private static void quickSort(String fileName) {
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		
		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);
			
			String line = br.readLine();
			String[] strs = line.split(",");
			Integer[] ages = new Integer[strs.length];
			for (int i = 0; i < strs.length; i++) {
				ages[i] = Integer.parseInt(strs[i]);
			}
			QuickSort.quickSort(ages);
			fw = new FileWriter(fileName);
			bw = new BufferedWriter(fw);
			for (int num : ages) {
				String age = num + ",";
				bw.write(age);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				fr.close();
				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * get the total numbers of generated files in Pass0
	 * @return the number of generated files in Pass0
	 */
	public static int getCurrentFileNumber() {
		return currentFileNumber;
	}
}
