package com.loadingpleasewait.battleship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ship implements Serializable {

	public static final String[] NAMES = new String[]{"patrol boat","destroyer","submarine","battleship","aircraft carrier"};
	public static final int[] SIZES = {2,3,3,4,5};
	
	private static final long serialVersionUID = 5173132690416842042L;

	private int size;// original size of the ship
	private String name;
	private ArrayList<String> cells;
	
	/**
	 * @param name String that contains a ship name
	 * @return size of the ship with the given name
	 * @throws IllegalArgumentException if no ship name was found in the String
	 */
	public static int sizeOf(String name) throws IllegalArgumentException{
		if(name.contains("patrol boat"))
			return 2;
		else if(name.contains("destroyer") || name.contains("submarine"))
			return 3;
		else if(name.contains("battleship"))
			return 4;
		else if(name.contains("aircraft carrier"))
			return 5;
		else
			throw new IllegalArgumentException("No valid ship name found");
	}

	// throws an exception if the given name is not valid
	public Ship(String shipName, List<String> locations)
			throws IllegalArgumentException {
		name = shipName;
		if (shipName.equals("patrol boat"))
			size = 2;
		else if (shipName.equals("destroyer") || shipName.equals("submarine"))
			size = 3;
		else if (shipName.equals("battleship"))
			size = 4;
		else if (shipName.equals("aircraft carrier"))
			size = 5;
		else
			throw new IllegalArgumentException("Not a valid ship name");
		if(locations.size() != size)
			throw new IllegalArgumentException("Incorrect number of coordinates");
		// test if ship is not in a line
		boolean broken = false;
		String similarity = locations.get(0).charAt(0) == locations.get(1)
				.charAt(0) ? String.valueOf(locations.get(0).charAt(0))
				: locations.get(0).substring(1);
		for (String cell : locations)
			broken |= !cell.contains(similarity);
		if (broken)
			throw new IllegalArgumentException(
					"Ships cannot be placed that way " + locations);
		cells = new ArrayList<String>(locations);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the original size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return the cells
	 */
	public synchronized ArrayList<String> getCells() {
		return cells;
	}

	/**
	 * @return if a cell hasn't been hit
	 */
	public boolean isHidden() {
		return cells.size() == size;
	}

	public synchronized String guessResult(String guess) {
		boolean damaged = cells.remove(guess);
		if (!damaged)
			return "miss";
		else if (cells.isEmpty())
			return "sunk " + name;
		else
			return "hit " + name;
	}

	@Override
	public String toString() {
		return name + ":" + cells.toString();
	}
}
