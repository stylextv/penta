package io.penta.board.evaluate;

public class BoardScore {
	
	public static final int WIN = 100000;
	public static final int LOSE = -100000;
	public static final int DRAW = 0;
	public static final int UNKNOWN = Integer.MIN_VALUE;
	
	public static final int TEMPO = 15;
	public static final int POSITION = 10;
	private static final int[][] POSITIONS = new int[][] {
			{  5, 10,  5,  5, 10,  5 },
			{ 10, 15, 12, 12, 15, 10 },
			{  5, 12,  7,  7, 12,  5 },
			{  5, 12,  7,  7, 12,  5 },
			{ 10, 15, 12, 12, 15, 10 },
			{  5, 10,  5,  5, 10,  5 }
	};
	public static final int POSITION_CONNECTION = 3;
	
	public static boolean isAdvantageousOrEqualTo(int score1, int score2) {
		return score1 >= score2;
	}
	
	public static boolean isAdvantageousTo(int score1, int score2) {
		return score1 > score2;
	}
	
	public static int add(int score1, int score2) {
		return score1 + score2;
	}
	
	public static int invert(int score) {
		return -score;
	}
	
	public static int position(int x, int y) {
		return POSITIONS[x][y];
	}
	
}
