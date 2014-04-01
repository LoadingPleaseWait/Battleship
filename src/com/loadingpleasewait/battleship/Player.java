package com.loadingpleasewait.battleship;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Player implements Serializable{
	
	private static final long serialVersionUID = -8494487276578313523L;
	
	private Board playerBoard;
	private Board guessBoard;
	private ArrayList<String> unguessedCells;
	private ArrayList<String> guessedCells;
	
	/**
	 * @param playerBoard
	 * @param guessBoard
	 */
	public Player(Board playerBoard, Board guessBoard) {
		this.playerBoard = playerBoard;
		this.guessBoard = guessBoard;
		//set unguessed cells
		unguessedCells = new ArrayList<String>();
		for(int i = 'a'; i < 'k';i++){
			for(int num = 1; num < 11;num++){
				unguessedCells.add(((char)(i)) + String.valueOf(num));
			}
		}
		setGuessedCells(new ArrayList<String>());
	}

	/**
	 * @return guess made by player
	 */
	public abstract String getGuess();
	
	public abstract void placeShips();

	/**
	 * @return the playerBoard
	 */
	public Board getPlayerBoard() {
		return playerBoard;
	}

	/**
	 * @param playerBoard the playerBoard to set
	 */
	public void setPlayerBoard(Board playerBoard) {
		this.playerBoard = playerBoard;
	}

	/**
	 * @return the guessBoard
	 */
	public Board getGuessBoard() {
		return guessBoard;
	}

	/**
	 * @param guessBoard the guessBoard to set
	 */
	public void setGuessBoard(Board guessBoard) {
		this.guessBoard = guessBoard;
	}

	/**
	 * @return the unguessedCells
	 */
	public ArrayList<String> getUnguessedCells() {
		return unguessedCells;
	}

	/**
	 * @return the guessedCells
	 */
	public ArrayList<String> getGuessedCells() {
		return guessedCells;
	}

	/**
	 * @param guessedCells the guessedCells to set
	 */
	public void setGuessedCells(ArrayList<String> guessedCells) {
		this.guessedCells = guessedCells;
	}
}
