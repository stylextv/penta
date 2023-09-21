package io.penta.board;

import io.penta.board.evaluate.BoardOutcome;
import io.penta.board.evaluate.BoardScore;
import io.penta.board.evaluate.BoardScoreTable;

import java.util.Random;

public class Board {
	
	private static final int LENGTH = 6;
	private static final int LINE_LENGTH = 5;
	
	private static final int LEFT_DELTA_X = -1;
	private static final int RIGHT_DELTA_X = 1;
	private static final int NEUTRAL_DELTA_X = 0;
	private static final int UP_DELTA_Y = -1;
	private static final int DOWN_DELTA_Y = 1;
	private static final int NEUTRAL_DELTA_Y = 0;
	
	public static final int EMPTY_COLOR_AMOUNT = 0;
	public static final int FULL_COLOR_AMOUNT = LENGTH * LENGTH;
	
	public static final int DEFAULT_EVALUATION_DEPTH = 5;
	public static final int BOTTOM_EVALUATION_DEPTH = 0;
	
	private static final int[][] EVALUATION_MOVE_POSITIONS = new int[][] {
			{ 1, 1 }, { 1, 4 }, { 4, 1 }, { 4, 4 }, { 1, 2 }, { 1, 3 },
			{ 2, 1 }, { 2, 4 }, { 3, 1 }, { 3, 4 }, { 4, 2 }, { 4, 3 },
			{ 0, 1 }, { 0, 4 }, { 1, 0 }, { 1, 5 }, { 4, 0 }, { 4, 5 },
			{ 5, 1 }, { 5, 4 }, { 2, 2 }, { 2, 3 }, { 3, 2 }, { 3, 3 },
			{ 0, 0 }, { 0, 2 }, { 0, 3 }, { 0, 5 }, { 2, 0 }, { 2, 5 },
			{ 3, 0 }, { 3, 5 }, { 5, 0 }, { 5, 2 }, { 5, 3 }, { 5, 5 }
	};
	private static final int POSITION_X_INDEX = 0;
	private static final int POSITION_Y_INDEX = 1;
	
	public static final int SCORE_ESTIMATION_WINDOW = BoardScore.POSITION;
	
	private static final long WHITE_KEY;
	private static final long BLACK_KEY;
	private static final long[][] WHITE_POSITION_KEYS = new long[LENGTH][LENGTH];
	private static final long[][] BLACK_POSITION_KEYS = new long[LENGTH][LENGTH];
	
	static {
		Random random = new Random();
		
		WHITE_KEY = random.nextLong();
		BLACK_KEY = random.nextLong();
		
		for(int x = 0; x < LENGTH; x++) {
			for(int y = 0; y < LENGTH; y++) {
				
				WHITE_POSITION_KEYS[x][y] = random.nextLong();
				BLACK_POSITION_KEYS[x][y] = random.nextLong();
			}
		}
	}
	
	private final BoardColor[][] colors = new BoardColor[LENGTH][LENGTH];
	private int colorAmount = EMPTY_COLOR_AMOUNT;
	
	private BoardColor color = BoardColor.WHITE;
	private BoardOutcome outcome = BoardOutcome.UNKNOWN;
	
	private long key = WHITE_KEY;
	
	public int evaluateIteratively() {
		return evaluateIteratively(DEFAULT_EVALUATION_DEPTH);
	}
	
	public int evaluateIteratively(int depth) {
		if(depth < BOTTOM_EVALUATION_DEPTH) return BoardScore.UNKNOWN;
		
		int score = BoardScore.DRAW;
		for(int d = BOTTOM_EVALUATION_DEPTH; d <= depth; d++) {
			int minScore = score - SCORE_ESTIMATION_WINDOW / 2;
			int maxScore = score + SCORE_ESTIMATION_WINDOW / 2;
			
			while(true) {
				
				int s = evaluate(d, minScore, maxScore);
				if(s <= minScore) {
					
					minScore -= SCORE_ESTIMATION_WINDOW;
					continue;
				}
				
				if(s >= maxScore) {
					
					maxScore += SCORE_ESTIMATION_WINDOW;
					continue;
				}
				
				score = s;
				break;
			}
		}
		
		return score;
	}
	
	private int evaluate(int depth) {
		return evaluate(depth, BoardScore.LOSE, BoardScore.WIN);
	}
	
	private int evaluate(int depth, int minScore, int maxScore) {
		if(isDecided()) return outcome.evaluate(color);
		
		BoardScoreTable scoreTable = BoardScoreTable.getInstance();
		int score = scoreTable.getScore(this, minScore, maxScore, depth);
		
		if(score != BoardScore.UNKNOWN) return score;
		if(depth == BOTTOM_EVALUATION_DEPTH) return evaluateStatically();
		
		score = minScore;
		if(BoardScore.isAdvantageousOrEqualTo(score, maxScore)) return score;
		
		for(int[] position : EVALUATION_MOVE_POSITIONS) {
			int x = position[POSITION_X_INDEX];
			int y = position[POSITION_Y_INDEX];
			
			if(!canPlayMove(x, y)) continue;
			
			playMove(x, y);
			int min = BoardScore.invert(maxScore);
			int max = BoardScore.invert(score);
			int s = evaluate(depth - 1, min, max);
			s = BoardScore.invert(s);
			undoMove(x, y);
			
			if(BoardScore.isAdvantageousTo(s, score)) score = s;
			if(BoardScore.isAdvantageousOrEqualTo(score, maxScore)) {
				
				scoreTable.putScore(this, score, minScore, maxScore, depth);
				return score;
			}
		}
		
		scoreTable.putScore(this, score, minScore, maxScore, depth);
		return score;
	}
	
	private int evaluateStatically() {
		int score = BoardScore.add(BoardScore.DRAW, BoardScore.TEMPO);
		
		for(int x = 0; x < LENGTH; x++) {
			for(int y = 0; y < LENGTH; y++) {
				
				int s = evaluatePositionStatically(x, y);
				score = BoardScore.add(score, s);
			}
		}
		
		return score;
	}
	
	private int evaluatePositionStatically(int x, int y) {
		if(colors[x][y] == null) return BoardScore.DRAW;
		
		int score = BoardScore.position(x, y);
		score = BoardScore.add(score, evaluatePositionConnectionStatically(x, y, NEUTRAL_DELTA_X, UP_DELTA_Y));
		score = BoardScore.add(score, evaluatePositionConnectionStatically(x, y, RIGHT_DELTA_X, UP_DELTA_Y));
		score = BoardScore.add(score, evaluatePositionConnectionStatically(x, y, RIGHT_DELTA_X, NEUTRAL_DELTA_Y));
		score = BoardScore.add(score, evaluatePositionConnectionStatically(x, y, RIGHT_DELTA_X, DOWN_DELTA_Y));
		score = BoardScore.add(score, evaluatePositionConnectionStatically(x, y, NEUTRAL_DELTA_X, DOWN_DELTA_Y));
		score = BoardScore.add(score, evaluatePositionConnectionStatically(x, y, LEFT_DELTA_X, DOWN_DELTA_Y));
		score = BoardScore.add(score, evaluatePositionConnectionStatically(x, y, LEFT_DELTA_X, NEUTRAL_DELTA_Y));
		score = BoardScore.add(score, evaluatePositionConnectionStatically(x, y, LEFT_DELTA_X, UP_DELTA_Y));
		
		return colors[x][y] == color ? score : BoardScore.invert(score);
	}
	
	private int evaluatePositionConnectionStatically(int x, int y, int deltaX, int deltaY) {
		int x2 = x + deltaX;
		int y2 = y + deltaY;
		
		if(!isValidPosition(x2, y2) || colors[x2][y2] != colors[x][y]) return BoardScore.DRAW;
		return BoardScore.POSITION_CONNECTION;
	}
	
	public boolean canPlayMove(int x, int y) {
		return colors[x][y] == null;
	}
	
	public void playMove(int x, int y) {
		colors[x][y] = color;
		colorAmount++;
		outcome = computeOutcome(x, y);
		
		key ^= WHITE_KEY;
		key ^= BLACK_KEY;
		key ^= color == BoardColor.WHITE ? WHITE_POSITION_KEYS[x][y] : BLACK_POSITION_KEYS[x][y];
		
		color = BoardColor.opposite(color);
	}
	
	public void undoMove(int x, int y) {
		colors[x][y] = null;
		colorAmount--;
		outcome = BoardOutcome.UNKNOWN;
		color = BoardColor.opposite(color);
		
		key ^= WHITE_KEY;
		key ^= BLACK_KEY;
		key ^= color == BoardColor.WHITE ? WHITE_POSITION_KEYS[x][y] : BLACK_POSITION_KEYS[x][y];
	}
	
	private BoardOutcome computeOutcome(int x, int y) {
		if(containsLine(x, y)) return BoardOutcome.win(color);
		if(isFull()) return BoardOutcome.DRAW;
		
		return BoardOutcome.UNKNOWN;
	}
	
	private boolean containsLine(int x, int y) {
		if(containsLine(x, y, NEUTRAL_DELTA_X, UP_DELTA_Y)) return true;
		if(containsLine(x, y, RIGHT_DELTA_X, NEUTRAL_DELTA_Y)) return true;
		if(containsLine(x, y, RIGHT_DELTA_X, UP_DELTA_Y)) return true;
		if(containsLine(x, y, RIGHT_DELTA_X, DOWN_DELTA_Y)) return true;
		
		return false;
	}
	
	private boolean containsLine(int x, int y, int deltaX, int deltaY) {
		BoardColor color = colors[x][y];
		int length = 1;
		
		int x2 = x + deltaX;
		int y2 = y + deltaY;
		
		while(isValidPosition(x2, y2) && colors[x2][y2] == color) {
			
			length++;
			x2 += deltaX;
			y2 += deltaY;
		}
		
		x2 = x - deltaX;
		y2 = y - deltaY;
		
		while(isValidPosition(x2, y2) && colors[x2][y2] == color) {
			
			length++;
			x2 -= deltaX;
			y2 -= deltaY;
		}
		
		return length >= LINE_LENGTH;
	}
	
	private boolean isValidPosition(int x, int y) {
		return x >= 0 && y >= 0 && x < LENGTH && y < LENGTH;
	}
	
	public boolean isFull() {
		return colorAmount == FULL_COLOR_AMOUNT;
	}
	
	public boolean isEmpty() {
		return colorAmount == EMPTY_COLOR_AMOUNT;
	}
	
	public boolean isDecided() {
		return outcome != BoardOutcome.UNKNOWN;
	}
	
	public BoardColor getColor() {
		return color;
	}
	
	public BoardOutcome getOutcome() {
		return outcome;
	}
	
	public long getKey() {
		return key;
	}
	
}
