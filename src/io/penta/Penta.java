package io.penta;

import io.penta.board.Board;

public class Penta {
	
	public static void main(String[] args) {
		Board board = new Board();
		
		System.out.println(board.evaluateIteratively());
	}
	
}
