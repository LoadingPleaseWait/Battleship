package com.loadingpleasewait.battleship;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class User extends Player implements ActionListener {

	private static final long serialVersionUID = 8351327165640425764L;

	private JTextField textField;
	private boolean waiting = true;
	private String input;
	private boolean isUserTurn;

	public User(Board playerBoard, Board guessBoard, JTextField field) {
		super(playerBoard, guessBoard);
		textField = field;
	}

	@Override
	public synchronized String getGuess() {
		String guess = getInput();
		if(!getUnguessedCells().contains(guess)){
			int confirm = JOptionPane.showConfirmDialog(textField.getTopLevelAncestor(), guess + " is not a valid unused guess. Are you sure you want to use that?");
			if(confirm != JOptionPane.YES_OPTION)
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
				JOptionPane.showMessageDialog(textField.getTopLevelAncestor(), ex.getMessage());
				i--;
			}
		}
	}

	public ArrayList<String> getPlacement(String shipName, int shipSize) {
		String placement = JOptionPane.showInputDialog(
				textField.getTopLevelAncestor(), "Enter coordinates for "
						+ shipName + " (size " + shipSize + ")");
		if (placement.equals("r"))
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
		input = textField.getText().toLowerCase();
		waiting = false;
		textField.setText("");
		textField.requestFocus();
	}
}
