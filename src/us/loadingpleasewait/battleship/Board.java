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

package us.loadingpleasewait.battleship;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;

public class Board implements Serializable {

	private static final long serialVersionUID = -6709094610066164422L;

	public static final int BOARD_WIDTH = 700;
	public static final int BOARD_HEIGHT = 700;
	public static final int CELL_SIZE = 54;

	private ArrayList<Ship> ships;
	private ArrayList<String> guesses;
	private ArrayList<String> hits;
	private ArrayList<String> misses;

	public Board() {
		setShips(new ArrayList<Ship>());
		guesses = new ArrayList<String>();
		setHits(new ArrayList<String>());
		setMisses(new ArrayList<String>());
	}
	
	public Board(Board otherBoard){
		ships = new ArrayList<Ship>(otherBoard.ships);
		guesses = new ArrayList<String>();
		setHits(new ArrayList<String>());
		setMisses(new ArrayList<String>());
	}

	/**
	 * @return the ships
	 */
	public synchronized ArrayList<Ship> getShips() {
		return ships;
	}

	/**
	 * @param ships
	 *            the ships to set
	 */
	public synchronized void setShips(ArrayList<Ship> ships) {
		this.ships = ships;
	}

	/**
	 * @param name
	 *            name of the ship
	 * @param cells
	 *            cells in the ship
	 */
	public synchronized void placeShip(String name, ArrayList<String> cells)
			throws IllegalArgumentException {
		// test if any cell has already been used
		for(String cell : cells){
			if(!isValid(cell))
				throw new IllegalArgumentException(cell + " Cell is out of bounds");
			for(Ship ship : ships){
				if(ship.getCells().contains(cell))
					throw new IllegalArgumentException(cell + " Overlapping ships");
			}
		}
		ships.add(new Ship(name, cells));
	}

	/**
	 * @param shipSize
	 *            of the ship to be placed
	 * @return a random ArrayList of locations for a ship
	 */
	public ArrayList<String> randomPlacement(int shipSize) {
		boolean horizontal = Math.random() < 0.5;
		ArrayList<String> output = null;
		boolean invalid = false;
		do {
			output = new ArrayList<String>();
			int longSide = (int) (Math.random() * (10 - shipSize)) + 1;
			int shortSide = (int) (Math.random() * 10) + 1;
			for (int i = longSide; i < shipSize + longSide; i++) {
				if (horizontal)
					output.add((char) (i + 'a') + String.valueOf(shortSide));
				else
					output.add((char) (shortSide + 'a' - 1) + String.valueOf(i));
			}
			invalid = false;
			for (Ship ship : getShips()) {
				for (String cell : ship.getCells())
					invalid |= output.contains(cell);
			}
		} while (invalid);
		return output;
	}

	/**
	 * @param cell
	 * @return wether or not cell is in bounds
	 */
	public boolean isValid(String cell) {
		return cell.length() < 4 && cell.length() > 1 && cell.charAt(0) >= 'a'
				&& cell.charAt(0) < 'k'
				&& Integer.parseInt(cell.substring(1)) > 0
				&& Integer.parseInt(cell.substring(1)) < 11;
	}
	
	/**
	 * @return the size of the smallest ship that has not been hit
	 */
	public int smallestShipSize(){
		int output = 5;
		for(Ship ship : ships){
			if(ship.isHidden())
				output = Math.min(output, ship.getSize());
		}
		return output;
	}
	
	/**
	 * @return the size of the largest ship that has not been hit
	 */
	public int largestShipSize(){
		int output = 2;
		for(Ship ship : ships){
			if(ship.isHidden())
				output = Math.max(output, ship.getSize());
		}
		return output;
	}

	/**
	 * @param guess
	 * @return if a ship was hit
	 */
	public synchronized String checkGuess(String guess) {
		guesses.add(guess);
		for (Ship ship : getShips()) {
			String result = ship.guessResult(guess);
			if (!result.contains("miss")) {
				getHits().add(guess);
				return result;
			}
		}
		getMisses().add(guess);
		return "miss";
	}

	/**
	 * @return if all of the ships have been sunk
	 */
	public boolean isGameOver() {
		for (Ship ship : getShips())
			if (!ship.getCells().isEmpty())
				return false;
		return true;
	}

	/**
	 * @return the cells in a 2d array of strings
	 */
	public String[][] cellArray() {
		String[][] output = new String[10][10];

		for (Ship ship : getShips()) {
			for (String cell : ship.getCells()) {
				output[Integer.parseInt(cell.substring(1))][cell.charAt(0) - 'a'] = "ship";
			}
		}
		for (String cell : getHits()) {
			output[Integer.parseInt(cell.substring(1))][cell.charAt(0) - 'a'] = "hit";
		}
		for (String cell : getMisses()) {
			output[Integer.parseInt(cell.substring(1))][cell.charAt(0) - 'a'] = "miss";
		}

		return output;
	}

	@Override
	public String toString() {
		return "Board [ships=" + ships + ", guesses=" + guesses + ", hits="
				+ hits + ", misses=" + misses + "]";
	}

	/**
	 * @return the misses
	 */
	public synchronized ArrayList<String> getMisses() {
		return misses;
	}

	/**
	 * @param misses the misses to set
	 */
	public synchronized void setMisses(ArrayList<String> misses) {
		this.misses = misses;
	}

	/**
	 * @return the hits
	 */
	public synchronized ArrayList<String> getHits() {
		return hits;
	}

	/**
	 * @param hits the hits to set
	 */
	public synchronized void setHits(ArrayList<String> hits) {
		this.hits = hits;
	}

	public class BoardGraphics extends Canvas {

		private static final long serialVersionUID = 2498457968277641981L;

		private boolean showShips;

		/**
		 * @return the showShips
		 */
		public boolean isShowShips() {
			return showShips;
		}

		/**
		 * @param showShips the showShips to set
		 */
		public void setShowShips(boolean showShips) {
			this.showShips = showShips;
		}

		public BoardGraphics(boolean showShips) {
			this.setShowShips(showShips);
			setSize(BOARD_WIDTH - 5, BOARD_HEIGHT - 50);
			setBackground(Color.BLUE);
		}

		@Override
		public void paint(Graphics pen) {

			// draw grid
			for (int i = 0; i < 10; i++) {
				pen.setColor(Color.LIGHT_GRAY);
				pen.drawString(new Character((char) (i + 65)).toString(),
						i * 60 + 50, 10);
				pen.drawString(new Integer(i + 1).toString(), 9, i * 60 + 60);
				pen.setColor(Color.BLACK);
				pen.drawLine(25, i * 60 + 25, BOARD_WIDTH - 75, i * 60 + 25);
				pen.drawLine(i * 60 + 25, 25, i * 60 + 25, BOARD_HEIGHT - 75);
			}
			pen.drawLine(25, 625, BOARD_WIDTH - 75, 625);
			pen.drawLine(625, 25, 625, BOARD_HEIGHT - 75);

		}
		
		@Override
		public void update(Graphics pen) {
			pen.setColor(Color.BLACK);
			if (isShowShips()) {
				for (Ship ship : getShips()) {
					for (String cell : ship.getCells()) {
						pen.fillRect(
								(cell.charAt(0) - 97) * 60 + 28,
								(Integer.parseInt(cell.substring(1)) - 1) * 60 + 28,
								CELL_SIZE,CELL_SIZE);
					}
				}
			}
			pen.setColor(Color.RED);
			for (String cell : getHits()) {
				pen.fillRect((cell.charAt(0) - 97) * 60 + 28,
						(Integer.parseInt(cell.substring(1)) - 1) * 60 + 28,
						CELL_SIZE, CELL_SIZE);
			}
			pen.setColor(Color.WHITE);
			for (String cell : getMisses()) {
				try{
					pen.fillRect((cell.charAt(0) - 97) * 60 + 28,
							(Integer.parseInt(cell.substring(1)) - 1) * 60 + 28,
							CELL_SIZE, CELL_SIZE);
				}catch (NullPointerException ex){

				}
			}

		}
	}
}
