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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class User extends Player implements ActionListener {

	private static final long serialVersionUID = 8351327165640425764L;

	private JTextField textField;
	private boolean waiting = true;
	private String input;
	private boolean isUserTurn;

	public User(Board playerBoard, Board guessBoard, JTextField field) throws RemoteException{
		super(playerBoard, guessBoard);
		setTextField(field);
	}

	@Override
	public synchronized String getGuess() {
		String guess = getInput();
		if(!getUnguessedCells().contains(guess)){
			JOptionPane.showMessageDialog(getTextField().getTopLevelAncestor(), guess + " is not a valid unused guess.");
			return getGuess();
		}
		String result = getGuessBoard().checkGuess(guess);
		getGuessedCells().add(getUnguessedCells().remove(getUnguessedCells().indexOf(guess)));
		return guess + "\n" + result;
	}

	@Override
	public synchronized void placeShips() {
		textField.addActionListener(this);
		for (int i = 0; i < 5; i++) {
			try {
				getPlayerBoard().placeShip(Ship.NAMES[i],
						getPlacement(Ship.NAMES[i], Ship.SIZES[i]));
			} catch (IllegalArgumentException ex) {
				JOptionPane.showMessageDialog(getTextField().getTopLevelAncestor(), ex.getMessage());
				i--;
			} catch (NullPointerException ex){
				System.exit(0);
			}
		}
	}

	public ArrayList<String> getPlacement(String shipName, int shipSize) throws NullPointerException {
		String placement = JOptionPane.showInputDialog(
				getTextField().getTopLevelAncestor(), "Enter coordinates for "
						+ shipName + " (size " + shipSize + ")");
		if (placement.startsWith("r"))
			return getPlayerBoard().randomPlacement(shipSize);
		ArrayList<String> output = new ArrayList<String>();
		for (String cell : placement.split(","))
			if(!output.contains(cell.trim()))
				output.add(cell.trim());
		return output;
	}

	/**
	 * @return the input
	 */
	public synchronized String getInput() {
		isUserTurn = true;
		while (waiting) {
			try {
				Thread.sleep(100);// don't hog cpu
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		waiting = true;
		isUserTurn = false;
		return input;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if(!isUserTurn)
			return;
		input = getTextField().getText().toLowerCase();
		waiting = false;
		getTextField().setText("");
		getTextField().requestFocus();
	}

	/**
	 * @return the textField
	 */
	public JTextField getTextField() {
		return textField;
	}

	/**
	 * @param textField the textField to set
	 */
	public synchronized void setTextField(JTextField textField) {
		this.textField = textField;
	}
}
