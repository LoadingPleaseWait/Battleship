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

import java.rmi.RemoteException;
import java.util.ArrayList;

public class BattleshipAI extends Player {

	private static final long serialVersionUID = 3023977351407249209L;

	private String difficulty;
	private String lastHit;
	private String lastResult;
	private ArrayList<String> possiblePlacement;
	private ArrayList<String> foundShipCells;
	private ArrayList<String> foundShips;
	private ArrayList<String> recentHits;

	public BattleshipAI(Board playerBoard, Board opponentBoard) throws RemoteException{
		super(playerBoard, opponentBoard);
	}

	public BattleshipAI(Board playerBoard, Board opponentBoard,
			String difficultySetting) throws RemoteException{
		this(playerBoard, opponentBoard);
		setDifficulty(difficultySetting);
		foundShipCells = new ArrayList<String>();
		foundShips = new ArrayList<String>();
		recentHits = new ArrayList<String>();
	}

	@Override
	public synchronized String getGuess() {
		String guess = null;
		if (possiblePlacement != null) {
			if (getDifficulty().equals("Hard")) {
				// guess a square based on how many ways a ship could be placed there
				int mostPlacements = -1;
				ArrayList<String> guessChoices = new ArrayList<String>();
				for (String cell : possiblePlacement) {
					if (getPossiblePlacements(cell, getGuessBoard().smallestShipSize()).size() > mostPlacements) {
						mostPlacements = getPossiblePlacements(cell, getGuessBoard().smallestShipSize()).size();
						guessChoices.clear();
						guessChoices.add(cell);
					} else if (getPossiblePlacements(cell, getGuessBoard().smallestShipSize()).size() == mostPlacements)
						guessChoices.add(cell);
				}
				guess = guessChoices.get((int)(Math.random() * guessChoices.size()));
			} else
				guess = possiblePlacement.get((int) (Math.random() * possiblePlacement.size()));
		} else
			guess = randomGuess();
		String result = getGuessBoard().checkGuess(guess);
		assert (getUnguessedCells().contains(guess)) : "unguessed cells did not contain guess: " + guess;
		getGuessedCells().add(getUnguessedCells().remove(getUnguessedCells().indexOf(guess)));

		// what do to after finding a ship
		if (!getDifficulty().equals("Easy")) {
			if (result.contains("hit") && lastHit == null) {
				lastHit = guess;
				recentHits = new ArrayList<String>();
				recentHits.add(lastHit);
				lastResult = result;
				ArrayList<ArrayList<String>> placements = getPossiblePlacements(
						lastHit, Ship.sizeOf(result));
				possiblePlacement = placements.get(bestIndex(placements));

			} else if (result.contains("miss") && lastHit != null) {
				// if AI misses after finding a ship
				ArrayList<ArrayList<String>> placements = recentHits.size() > 1 ? findPlacements()
						: getPossiblePlacements(lastHit,
								Ship.sizeOf(lastResult));
				assert (!placements.isEmpty()) : getGuessBoard() + " guess:"
						+ guess + " result:" + result + " lastHit:" + lastHit
						+ " lastResult:" + lastResult
						+ " foundShips and cells:" + foundShips
						+ foundShipCells;
				possiblePlacement = placements.get(bestIndex(placements));
			} else if (result.contains("sunk")
					&& !lastResult.contains(result.substring(result
							.indexOf(" ")))) {
				// if a found ship is accidently sunk
				ArrayList<ArrayList<String>> placements = recentHits.size() > 1 ? findPlacements()
						: getPossiblePlacements(lastHit,
								Ship.sizeOf(lastResult));
				assert (!placements.isEmpty()) : "placements is empty";
				possiblePlacement = placements.get(bestIndex(placements));
				while (foundShips.contains(result.replace("sunk", "hit"))) {
					foundShipCells.remove(foundShips.indexOf(result.replace(
							"sunk", "hit")));
					foundShips.remove(result.replace("sunk", "hit"));
				}
			} else if (result.contains("sunk")) {
				// reset things after a ship has been sunk
				recentHits.clear();
				if (foundShipCells.isEmpty()) {
					lastHit = null;
					lastResult = null;
					possiblePlacement = null;
				} else {
					// move to sinking a ship that was accidently found
					lastHit = foundShipCells.remove(0);
					lastResult = foundShips.remove(0);
					recentHits.add(lastHit);
					while (foundShips.contains(lastResult)) {
						recentHits.add(foundShipCells.remove(foundShips
								.indexOf(lastResult)));
						foundShips.remove(lastResult);
					}
					ArrayList<ArrayList<String>> placements = getPossiblePlacements(
							lastHit, Ship.sizeOf(lastResult));
					possiblePlacement = placements.get(bestIndex(placements));
				}
			} else if (result.contains("hit") && !result.equals(lastResult)) {
				// AI has found a ship while it was sinking another one
				foundShipCells.add(guess);
				foundShips.add(result);
				ArrayList<ArrayList<String>> placements = getPossiblePlacements(
						lastHit, Ship.sizeOf(lastResult));
				possiblePlacement = placements.get(bestIndex(placements));
			} else if (result.contains("hit")) {
				// AI is taking down a ship that it has found
				recentHits.add(guess);
				possiblePlacement = findPlacements().get(
						(int) (Math.random() * findPlacements().size()));
			}
		}

		return guess + "\n" + result;
	}

	/**
	 * @param placements
	 *            ArrayList of placements that the index should be from
	 * @return best placement based on eliminating other possibilites or random
	 *         index if difficulty is not hard
	 */
	public int bestIndex(ArrayList<ArrayList<String>> placements) {
		assert (!placements.isEmpty()) : "placements is empty";
		if (!getDifficulty().equals("Hard"))
			return (int) (Math.random() * placements.size());
		int output = 0;
		int greatestSum = -1;
		for (int i = 0; i < placements.size(); i++) {
			int placementSum = 0;
			for (String cell : placements.get(i)) {
				placementSum += getPossiblePlacements(cell,
						Ship.sizeOf(lastResult)).size();
			}
			if (placementSum > greatestSum
					|| (placementSum == greatestSum && Math.random() * 3 < 1)) {
				greatestSum = placementSum;
				output = i;
			}
		}
		return output;
	}

	/**
	 * @param cell
	 *            that is contained in the placement
	 * @param shipSize
	 *            size of placement
	 * @return the possible placements for a ship
	 */
	public ArrayList<ArrayList<String>> getPossiblePlacements(String cell,
			int shipSize) {
		ArrayList<ArrayList<String>> output = new ArrayList<ArrayList<String>>();
		for (int horizontal = 0; horizontal < 2; horizontal++) {
			for (int place = 0; place < shipSize; place++) {
				ArrayList<String> placement = new ArrayList<String>();
				for (int i = 0; i < shipSize; i++) {
					String addition = null;
					if (horizontal == 0)
						addition = (char) (cell.charAt(0) + place - i)
								+ cell.substring(1);
					else
						addition = cell.charAt(0)
								+ String.valueOf(Integer.parseInt(cell
										.substring(1)) + place - i);
					placement.add(addition);
					if (!getUnguessedCells().contains(addition)) {
						if (recentHits.contains(addition))
							placement.remove(addition);
						else {
							placement.clear();
							break;
						}
					}
				}
				if (!placement.isEmpty())
					output.add(placement);
			}
		}
		return output;
	}

	/**
	 * @return placement of ship using recent hits
	 */
	public ArrayList<ArrayList<String>> findPlacements() {
		ArrayList<ArrayList<String>> output = new ArrayList<ArrayList<String>>();
		output.addAll(getPossiblePlacements(lastHit, Ship.sizeOf(lastResult)));
		for (int i = 0; i < output.size(); i++) {
			boolean broken = false;
			String similarity = recentHits.get(0).charAt(0) == recentHits
					.get(1).charAt(0) ? String.valueOf(recentHits.get(0)
					.charAt(0)) : recentHits.get(0).substring(1);
			for (String cell : recentHits)
				broken |= !cell.contains(similarity);
			if (broken
					|| Ship.sizeOf(lastResult) != output.get(i).size()
							+ recentHits.size())
				output.remove(i--);
		}
		return output;
	}

	/**
	 * @return random guess that has not been used of if difficulty is hard
	 *         spaced out guess
	 */
	public String randomGuess() {
		if (!getDifficulty().equals("Hard"))
			return getUnguessedCells().get(
					(int) (Math.random() * getUnguessedCells().size()));
		double mostPlacements = -1;
		ArrayList<String> goodCells = new ArrayList<String>();
		for (String cell : getUnguessedCells()) {
			assert (cell != null) : "an unguessed cell was null";
			if (getPossiblePlacements(cell, getGuessBoard().largestShipSize())
					.size() > mostPlacements) {
				mostPlacements = getPossiblePlacements(cell,
						getGuessBoard().largestShipSize()).size();
				goodCells.clear();
				goodCells.add(cell);
			} else if (getPossiblePlacements(cell,
					getGuessBoard().largestShipSize()).size() == mostPlacements)
				goodCells.add(cell);
		}
		assert(getUnguessedCells().containsAll(goodCells)) : "unguessed cells did not contain a possible guess";
		return goodCells.get((int) (Math.random() * goodCells.size()));
	}

	@Override
	public synchronized void placeShips() {
		// places ships randomly
		getPlayerBoard().placeShip("patrol boat",
				getPlayerBoard().randomPlacement(2));
		getPlayerBoard().placeShip("destroyer",
				getPlayerBoard().randomPlacement(3));
		getPlayerBoard().placeShip("submarine",
				getPlayerBoard().randomPlacement(3));
		getPlayerBoard().placeShip("battleship",
				getPlayerBoard().randomPlacement(4));
		getPlayerBoard().placeShip("aircraft carrier",
				getPlayerBoard().randomPlacement(5));
	}

	/**
	 * @return the difficulty
	 */
	public String getDifficulty() {
		return difficulty;
	}

	/**
	 * @param difficulty
	 *            the difficulty to set
	 */
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

}
