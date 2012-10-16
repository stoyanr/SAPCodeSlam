
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SolutionTest {

	private static final int MAX_GAMES = 100;
	private static final int MAX_GAMES_EVAL = 10;
	private static final int MAX_FIRST_BID = 50;
	private static final double MAX_RATIO = 1.20;
	private static final double MIN_RATIO = 0.80;
	
	private static final int REASON_NONE = 0;
	private static final int REASON_WRONG_BID = 1;
	private static final int REASON_POSITION = 2;
	private static final int REASON_OUT_OF_MONEY = 3;

	private final File file;
	private boolean verbose = false;
	
	public SolutionTest(File file) {
		this.file = file;
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { new File("input00.txt") } };
		return Arrays.asList(data);
	}

	@Test
	public void test1CalculateBid() throws Exception {
		System.out.println("testCalculateBid()");
		Solution.readInput(new FileInputStream(file));
		Solution.calculate_bid();
		Solution.printOutput();
		System.out.println();
	}
	
	@Test
	public void test2PlayGame() throws Exception {
		System.out.println("testPlayGame()");
		verbose = true;
		int wins1 = 0, wins2 = 0;
		for (int i = 0; i < MAX_GAMES; i++) {
			int winner = playGame(0, Solution.FIRST_BID, Solution.RATIO, 1, 0, 0.0);
			if (winner == 1) {
				wins1++;
			} else {
				wins2++;
			}
		}
		System.out.println("Result: " + wins1 + ":" + wins2);
	}

	@Test
	public void test3EvaluateFirstBidAndRatio() throws Exception {
		System.out.println("testEvaluateFirstBidAndRatio()");
		verbose = false;
		double maxResult = 0.0;	
		int bestFirstBid = 1;
		double bestRatio = 1.0;
		for (int firstBid = 1; firstBid < MAX_FIRST_BID; firstBid++) {
			System.out.println("Testing with first bid " + firstBid);
			for (double ratio = MIN_RATIO; ratio <= MAX_RATIO; ratio += 0.01) {
				double result = evaluateFirstBidAndRatio(firstBid, ratio);
				if (result > maxResult) {
					maxResult = result;
					bestFirstBid = firstBid;
					bestRatio = ratio;
					System.out.println("Found new best first bid / ratio (result): " + bestFirstBid + " / " + bestRatio + "(" + maxResult + ")");
				}
			}
		}
	}

	private double evaluateFirstBidAndRatio(int firstBid, double ratio) {
		int wins = 0, loses = 0;
		for (int firstBid2 = 1; firstBid2 < MAX_FIRST_BID; firstBid2++) {
			for (double ratio2 = MIN_RATIO; ratio2 <= MAX_RATIO; ratio2 += 0.01) {
				int wins1 = 0, wins2 = 0;
				for (int i = 0; i < MAX_GAMES_EVAL; i++) {
					int winner = playGame(0, firstBid, ratio, 0, firstBid2, ratio2);
					if (winner == 1) {
						wins1++;
					} else {
						wins2++;
					}
				}
				wins += wins1;
				loses += wins2;
			}
		}
		return (double) wins / (double) (loses + wins);
	}

	private int playGame(int alg1, int firstBid1, double ratio1, int alg2, int firstBid2, double ratio2) {
		int winner = 0;
		int pos = 5;
		int[] moves1 = new int[100], moves2 = new int[100];
		int money1 = 100, money2 = 100;
		int count = 0;
		int advantage = 1;
		int reason = REASON_NONE;
		while (true) {
			// Make bids
			int bid1 = calculateBid(alg1, 1, pos, moves1, moves2, firstBid1, ratio1);
			int bid2 = calculateBid(alg2, 2, pos, moves1, moves2, firstBid2, ratio2);
			// Update moves
			moves1[count] = bid1;
			moves2[count] = bid2;
			// Check for wrong bids
			boolean wrong1 = (bid1 < 1 || bid1 > money1), wrong2 = (bid2 < 1 || bid2 > money2);
			if (!wrong1 && wrong2) {
				winner = 1;
				reason = REASON_WRONG_BID;
				break;
			} else if (wrong1 && !wrong2) {
				winner = 2;
				reason = REASON_WRONG_BID;
				break;
			} else if (wrong1 && wrong2) {
				reason = REASON_WRONG_BID;
				break;
			}
			// Update money and position
			if (bid1 > bid2) {
				money1 -= bid1;
				pos--;
			} else if (bid2 > bid1) {
				money2 -= bid2;
				pos++;
			} else {
				if (advantage == 1) {
					money1 -= bid1;
					pos--;
					advantage = 2;
				} else {
					money2 -= bid2;
					pos++;
					advantage = 1;
				}
			}
			// Check winner
			if (pos == 0) {
				winner = 1;
				reason = REASON_POSITION;
				break;
			} else if (pos == 10) {
				winner = 2;
				reason = REASON_POSITION;
				break;
			} else if (money1 > 0 && money2 == 0) {
				winner = 1;
				reason = REASON_OUT_OF_MONEY;
				break;
			} else if (money1 == 0 && money2 > 0) {
				winner = 2;
				reason = REASON_OUT_OF_MONEY;
				break;
			} else if (money1 == 0 && money2 == 0) {
				reason = REASON_OUT_OF_MONEY;
				break;
			}
			// Update count
			count++;
		}
		if (verbose) {
			System.out.println("Winner: " + winner);
			printReason(reason);
			printMoves(1, moves1);
			printMoves(2, moves2);
			System.out.println("Position: " + pos);
			System.out.println("Player 1 money: " + money1);
			System.out.println("Player 2 money: " + money2);
		}
		return winner;
	}

	private int calculateBid(int alg, int player, int pos, int[] moves1, int[] moves2, int firstBid, double ratio) {
		int bid = 0;
		switch (alg) {
		case 0:
			bid = Solution.calculate_bid(player, pos, moves1, moves2, firstBid, ratio);
			break;
		case 1:
			bid = Solution.calculate_bid_1(player, pos, moves1, moves2);
			break;
		case 2:
			bid = Solution.calculate_bid_2(player, pos, moves1, moves2);
			break;
		case 3:
			bid = Solution.calculate_bid_3(player, pos, moves1, moves2);
			break;
		case 4:
			bid = Solution.calculate_bid_4(player, pos, moves1, moves2);
			break;
		}
		return bid;
	}

	private void printReason(int reason) {
		String s = "Unknown";
		switch (reason) {
		case REASON_WRONG_BID:
			s = "A player has placed a wrong bid";
			break;
		case REASON_POSITION:
			s = "The scotch has reached a final position";
			break;
		case REASON_OUT_OF_MONEY:
			s = "A player is out of money";
			break;
		}
		System.out.println("Reason: " + s);
	}

	private void printMoves(int player, int[] moves) {
		System.out.print("Player " + player + " moves: ");
		for (int i = 0; (i < moves.length) && (moves[i] > 0); i++) {
			System.out.print(moves[i] + " ");
		}
		System.out.println();
	}
}
