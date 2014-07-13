/*
 * Copyright (C) 2014  Michael Murphey
 * 
 * This file is part of Battleship LPW.
 * 
 * Battleship LPW is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Battleship LPW is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Battleship LPW. If not, see <http://www.gnu.org/licenses/>.
 */

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
