import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

public class Solution {
	
	private final InputStream in;
	private final PrintStream out;
	private int[] p;
	private int d;
	private int minl;
	private int[][] lines;

	public Solution(InputStream in, PrintStream out) {
		this.in = in;
		this.out = out;
	}

	public void calcLines() {
		minl = p.length + 1;
		ArrayList<int[]> linesList = new ArrayList<int[]>();
		linesList.add(new int[] { 1, p[0] } );
		calcLines(1, linesList);
	}
	
	private void calcLines(int x, ArrayList<int[]> linesList) {
		for (int i = x; i < p.length; i++) {
			if (linesList.size() >= minl - 1)
				continue;
			linesList.add(new int[] { i + 1, p[i] } );
			if (i < p.length - 1) {
				calcLines(i + 1, linesList);
			} else {
				int[][] linesx = linesList.toArray(new int[linesList.size()][2]);
				double dx = getMaxDistance(p, linesx);
				if (dx < (double) d) {
					minl = linesx.length;
					lines = linesx;
				}
			}
			linesList.remove(linesList.size() - 1);
		}
	}

	public double getMaxDistance() {
		return getMaxDistance(p, lines);
	}
	
	private double getMaxDistance(int[] p, int lines[][]) {
		double result = 0;
		for (int i = 1; i <= p.length; i++) {
			double px = 0; 
			for (int j = 0; j < lines.length - 1; j++) {
				if (lines[j][0] == i) {
					px = (double) lines[j][1];
				} else if (lines[j + 1][0] == i) {
					px = (double) lines[j + 1][1];
				} else if ((lines[j][0] < i) && (lines[j + 1][0] > i)) {
					px = (double) lines[j][1] + 
							(((double) i - (double) lines[j][0]) * 
							((double) lines[j + 1][1] - (double) lines[j][1]) / 
							((double) lines[j + 1][0] - (double) lines[j][0]));
				}
				if (px > 0) {
					break;
				}
			}
			double distance = Math.abs(p[i - 1] - px);
			result = Math.max(result, distance);
		}
		return result;
	}
		
	public void readInput() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = reader.readLine();
		String[] numbers = line.split(" ");
		int n = Integer.parseInt(numbers[0]);
		d = Integer.parseInt(numbers[1]);
		p = new int[n];
		for (int i = 0; i < n; i++) {
			String linex = reader.readLine();
			String[] numbersx = linex.split(" ");
			int k = Integer.parseInt(numbersx[0]);
			p[k - 1] = Integer.parseInt(numbersx[1]);
		}
	}

	public void printInput() {
		out.println("" + p.length + " " + d);
		for (int i = 0; i < p.length; i++)
			out.println("" + (i + 1) + " " + p[i]);
	}

	public void printOutput() {
		out.println("" + (lines.length - 1));
		for (int i = 0; i < lines.length; i++)
			out.println(lines[i][0] + " " + lines[i][1]);
	}
	
	public static void main(String args[]) throws Exception {
		Solution sol = new Solution(System.in, System.out);
		sol.readInput();
		sol.calcLines();
		sol.printOutput();
	}

}
