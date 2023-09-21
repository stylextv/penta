package io.penta.board.evaluate;

import io.penta.board.BoardColor;

public enum BoardOutcome {
	
	WHITE_WIN, BLACK_WIN, DRAW, UNKNOWN;
	
	public int evaluate(BoardColor color) {
		if(this == WHITE_WIN) return color == BoardColor.WHITE ? BoardScore.WIN : BoardScore.LOSE;
		if(this == BLACK_WIN) return color == BoardColor.WHITE ? BoardScore.LOSE : BoardScore.WIN;
		if(this == DRAW) return BoardScore.DRAW;
		
		return BoardScore.UNKNOWN;
	}
	
	public static BoardOutcome win(BoardColor color) {
		return color == BoardColor.WHITE ? WHITE_WIN : BLACK_WIN;
	}
	
}
