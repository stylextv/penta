package io.penta.board.evaluate;

import io.penta.board.Board;

public class BoardScoreTable {
	
	private static final int DEFAULT_SIZE = 1000000;
	
	private static final BoardScoreTable INSTANCE = new BoardScoreTable();
	
	private final int size;
	
	private final long[] boardKeys;
	private final int[] boardScores;
	private final int[] boardMinimalScores;
	private final int[] boardMaximalScores;
	private final int[] boardScoreWindows;
	private final int[] boardDepths;
	
	public BoardScoreTable() {
		this(DEFAULT_SIZE);
	}
	
	public BoardScoreTable(int size) {
		this.size = size;
		this.boardKeys = new long[size];
		this.boardScores = new int[size];
		this.boardMinimalScores = new int[size];
		this.boardMaximalScores = new int[size];
		this.boardScoreWindows = new int[size];
		this.boardDepths = new int[size];
	}
	
	public void putScore(Board board, int score, int minScore, int maxScore, int depth) {
		long key = board.getKey();
		int index = index(key);
		int scoreWindow = maxScore - minScore;
		
		if(depth < boardDepths[index]) return;
		if(depth == boardDepths[index] && scoreWindow < boardScoreWindows[index]) return;
		
		boardKeys[index] = key;
		boardScores[index] = score;
		boardMinimalScores[index] = minScore;
		boardMaximalScores[index] = maxScore;
		boardScoreWindows[index] = scoreWindow;
		boardDepths[index] = depth;
	}
	
	public int getScore(Board board, int minScore, int maxScore, int depth) {
		long key = board.getKey();
		int index = index(key);
		
		if(key != boardKeys[index] || depth > boardDepths[index]) return BoardScore.UNKNOWN;
		
		int score = boardScores[index];
		int min = boardMinimalScores[index];
		int max = boardMaximalScores[index];
		
		if(score <= min) return min <= minScore ? score : BoardScore.UNKNOWN;
		if(score >= max) return max >= maxScore ? score : BoardScore.UNKNOWN;
		return score;
	}
	
	private int index(long key) {
		int index = (int) (key % size);
		
		return index < 0 ? -index : index;
	}
	
	public int getSize() {
		return size;
	}
	
	public static BoardScoreTable getInstance() {
		return INSTANCE;
	}
	
}
