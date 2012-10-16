import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;

public class Solution {

	private static int player;
	private static int scotch_pos;
	private static int[] first_moves;
	private static int[] second_moves;
	private static int bid;
	
	private static final Random random = new Random();
	
	public static void calculate_bid() {
		bid = calculate_bid(player, scotch_pos, first_moves, second_moves);
	}
	
	public static final int FIRST_BID = 20;
	public static final double RATIO = 1.04;
	
	public static int calculate_bid(int player, int pos, int[] first_moves, int[] second_moves) {
		return calculate_bid(player, pos, first_moves, second_moves, FIRST_BID, RATIO);
	}
	
	// Algorithm 0: Advanced
	public static int calculate_bid(int player, int pos, int[] first_moves, int[] second_moves, int firstBid, double ratio) {
		int hisPlayer = (player == 1)? 2 : 1;
		int[] myMoves = (player == 1)? first_moves : second_moves;
		int[] hisMoves = (player == 1)? second_moves : first_moves;
		int myMoney = calcMoney(player, myMoves, hisMoves);
		int hisMoney = calcMoney(hisPlayer, hisMoves, myMoves);
		int myPos = (player == 1)? pos : 10 - pos;
		int hisPos = (player == 2)? pos : 10 - pos;
		int myOptimalBid = myMoney / myPos;
		int hisOptimalBid = hisMoney / hisPos;
		int advantage = calcAdvantage(first_moves, second_moves);
		boolean isFirstBid = (myMoney == 100 && hisMoney == 100);
		boolean applyRatio = false;
		int bid = 1;
		if (isFirstBid) {
			bid = firstBid; 
		} else if (myOptimalBid >= hisMoney) {
			bid = hisMoney; // bid his money (equal or lower to my optimal)
			applyRatio = true;
		} else if (myOptimalBid > hisOptimalBid && myOptimalBid < hisMoney) {
			bid = hisOptimalBid + (myOptimalBid - hisOptimalBid) / 2 + 1; // bid between his optimal and my optimal
			applyRatio = true;
		} else if (myOptimalBid == hisOptimalBid) {
			bid = (player == advantage)? myOptimalBid : myOptimalBid + 1; // bid my optimal
			applyRatio = true;
		} else if (myOptimalBid < hisOptimalBid && myMoney > hisOptimalBid) {
			bid = hisOptimalBid + 1; // bid 1 higher than his optimal (aggressive)
		} else if (myMoney <= hisOptimalBid) {
			bid = 1; // bid 1 to save money
		}
		if (applyRatio) {
			bid = (int) Math.round(bid * ratio);
		}
		return Math.max(Math.min(bid, myMoney), 1);
	}

	// Algorithm 1: Bid randomly within our available money
	public static int calculate_bid_1(int player, int pos, int[] first_moves, int[] second_moves) {
		int[] myMoves = (player == 1)? first_moves : second_moves;
		int[] hisMoves = (player == 1)? second_moves : first_moves;
		int myMoney = calcMoney(player, myMoves, hisMoves);
		int bid = random.nextInt(myMoney) + 1;
		return Math.max(Math.min(bid, myMoney), 1);
	}
	
	// Algorithm 2: Bid randomly around our optimal money
	public static int calculate_bid_2(int player, int pos, int[] first_moves, int[] second_moves) {
		int[] myMoves = (player == 1)? first_moves : second_moves;
		int[] hisMoves = (player == 1)? second_moves : first_moves;
		int myMoney = calcMoney(player, myMoves, hisMoves);
		int myPos = (player == 1)? pos : 10 - pos;
		int myOptimalBid = myMoney / myPos;
		int bid = 0;
		do {
			bid = random.nextInt(myOptimalBid + 1) + (myOptimalBid / 2);
		} while (bid > myMoney);
		return Math.max(Math.min(bid, myMoney), 1);
	}
	
	// Algorithm 3: Always bid our optimal money
	public static int calculate_bid_3(int player, int pos, int[] first_moves, int[] second_moves) {
		int[] myMoves = (player == 1)? first_moves : second_moves;
		int[] hisMoves = (player == 1)? second_moves : first_moves;
		int myMoney = calcMoney(player, myMoves, hisMoves);
		int myPos = (player == 1)? pos : 10 - pos;
		int myOptimalBid = myMoney / myPos;
		int bid = myOptimalBid;
		return Math.max(Math.min(bid, myMoney), 1);
	}	
	
	// Algorithm 4: Simulate judge bot
	public static int calculate_bid_4(int player, int pos, int[] first_moves, int[] second_moves) {
		int hisPlayer = (player == 1)? 2 : 1;
		int[] myMoves = (player == 1)? first_moves : second_moves;
		int[] hisMoves = (player == 1)? second_moves : first_moves;
		int myMoney = calcMoney(player, myMoves, hisMoves);
		int hisMoney = calcMoney(hisPlayer, hisMoves, myMoves);
		int myPos = (player == 1)? pos : 10 - pos;
		int myOptimalBid = myMoney / myPos;
		int advantage = calcAdvantage(first_moves, second_moves);
		boolean firstBid = (myMoney == 100 && hisMoney == 100);
		int bid = 1;
		if (firstBid)
			bid = (player == advantage)? 14 : 15; 
		else if (myMoney <= (hisMoney / 2))
			bid = 1; // Save money
		else if ((myMoney / 2) >= hisMoney)
			bid = hisMoney; // Bid aggressively
		else
			bid = myOptimalBid; // Bid optimally
		return Math.max(Math.min(bid, myMoney), 1);
	}
	
	private static int calcAdvantage(int[] moves1, int[] moves2) {
		int advantage = 1;
		for (int i = 0; (i < moves1.length) && (moves1[i] > 0); i++) {
			if (moves1[i] == moves2[i]) {
				advantage = (advantage == 1)? 2 : 1;
			}
		}
		return advantage;
	}

	private static int calcMoney(int player, int[] moves, int[] moves2) {
		int money = 100;
		int advantage = 1;
		for (int i = 0; (i < moves.length) && (moves[i] > 0); i++) {
			if (moves[i] > moves2[i]) {
				money -= moves[i];
			} else if (moves[i] == moves2[i]) {
				if (advantage == player) {
					money -= moves[i];
				}
				advantage = (advantage == 1)? 2 : 1;
			}
		}
		return money;
	}

	public static void main(String[] args) {
		readInput(System.in);
		calculate_bid();
		printOutput();
	}

	public static void readInput(InputStream input) {
		Scanner in = new Scanner(input);
		player = in.nextInt();
		scotch_pos = in.nextInt();
		first_moves = new int[100];
		second_moves = new int[100];
		in.useDelimiter("\n");
		String first_move = in.next();
		String[] move_1 = first_move.split(" ");
		String second_move = in.next();
		String[] move_2 = second_move.split(" ");
		if (first_move.equals("") == false) {
			for (int i = 0; i < move_1.length; i++) {
				first_moves[i] = Integer.parseInt(move_1[i]);
				second_moves[i] = Integer.parseInt(move_2[i]);
			}
		}
	}
	
	public static void printOutput() {
		System.out.print(bid);
	}
}
