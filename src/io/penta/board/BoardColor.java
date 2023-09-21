package io.penta.board;

public enum BoardColor {
	
	WHITE, BLACK;
	
	public static BoardColor opposite(BoardColor color) {
		return color == WHITE ? BLACK : WHITE;
	}
	
}
