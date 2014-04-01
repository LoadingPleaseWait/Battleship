package com.loadingpleasewait.battleship;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

public class Battleship implements Serializable {

	private static final long serialVersionUID = 2632923173759825918L;

	private JFrame frame;
	private Board board1;
	private Board board2;
	private Player player1;
	private Player player2;
	private JTextField textField;
	private JTextArea textArea;
	private boolean player2turn;
	private int turns;
	private boolean player1IsUser;
	private boolean player2IsUser;
	private String difficulty;
	private Board.BoardGraphics board1Graphics;
	private Board.BoardGraphics board2Graphics;
	private JScrollPane scroller;

	public static void main(String[] args) {
		Battleship game = new Battleship();
		do {
			game.setUpGame();
			while (game.winner().isEmpty()) {
				game.play();
			}
			game.endGame();
		} while (game.wantsToPlayAgain());
		//System.exit(0);
	}

	public void setUpGame() {
		turns = 0;
		setFrame(new JFrame("Battleship"));
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try{
		// ask user for type of game
		switch (JOptionPane.showInputDialog(getFrame(), "Number of players")) {
		case "0":
		case "zero":
			player1IsUser = false;
			player2IsUser = false;
			difficulty = "Hard";
			break;
		case "2":
		case "two":
			player1IsUser = true;
			player2IsUser = true;
			break;
		default:
			// set difficulty
			JOptionPane difficultyOptions = new JOptionPane("Select difficulty");
			difficultyOptions.setOptions(new String[] { "Easy", "Medium",
					"Hard" });
			difficultyOptions.createDialog(getFrame(), "Difficulty")
					.setVisible(true);
			while (difficultyOptions.getInputValue() == null)
				;// wait for user to choose
			difficulty = difficultyOptions.getValue().toString();
			// get turn order
			JOptionPane optionPane = new JOptionPane("Choose Turn order");
			optionPane.setOptions(new String[] { "First", "Second", "Random" });
			optionPane.createDialog(getFrame(), "Choose Turn Order")
					.setVisible(true);
			while (optionPane.getInputValue() == null)
				;// wait for user to choose
			switch (optionPane.getValue().toString()) {
			case "First":
				player1IsUser = true;
				break;
			case "Second":
				player1IsUser = false;
				break;
			case "Random":
				player1IsUser = Math.random() < 0.5;
			}
			player2IsUser = !player1IsUser;
		}
		}catch(NullPointerException ex){
			System.exit(0);//if user exits while choosing options
		}

		textField = new JTextField();
		textArea = new JTextArea(8, 50);
		textArea.setWrapStyleWord(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		((DefaultCaret) textArea.getCaret())
				.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		scroller = new JScrollPane(textArea);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setAutoscrolls(true);

		JPanel text = new JPanel();
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		text.add(scroller);
		text.add(textField);

		getFrame().add(BorderLayout.SOUTH, text);

		board1 = new Board();
		board2 = new Board();
		player1 = player1IsUser ? new User(board1, board2, getTextField())
				: new BattleshipAI(board1, board2, difficulty);
		player2 = player2IsUser ? new User(board2, board1, getTextField())
				: new BattleshipAI(board2, board1, difficulty);

		board1Graphics = board1.new BoardGraphics(!player2IsUser);
		board2Graphics = board2.new BoardGraphics(!player1IsUser);

		getFrame().add(BorderLayout.WEST, board1Graphics);
		getFrame().add(BorderLayout.EAST, board2Graphics);
		// constantly update
		new Thread(() -> {
			while (true) {
				board1Graphics.repaint();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();// dat lambda expression

		new Thread(() -> {
			while (true) {
				board2Graphics.repaint();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();// dat second lambda expression

		getFrame().setSize(Board.BOARD_WIDTH * 2, 850);
		getFrame().setVisible(true);
		player1.placeShips();
		player2.placeShips();
		while (board1.getShips().size() != 5 || board2.getShips().size() != 5) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		textField.requestFocus();
	}

	// goes through a turn
	public void play() {
		Player currentPlayer = player2turn ? player2 : player1;
		String playerName = player2turn ? "Player 2: " : "Player 1: ";
		textArea.append(playerName + currentPlayer.getGuess() + "\n");
		if (player2turn)
			turns++;
		player2turn = !player2turn;
	}

	public void endGame() {
		assert (!winner().isEmpty()) : "Game ended early";
		board1Graphics.setShowShips(true);
		board2Graphics.setShowShips(true);
		textArea.append(winner() + "\n");
		textArea.append(turns + " turns\n");
	}

	/**
	 * @return Player 1 or 2 has won or empty string if there is no winner
	 */
	public String winner() {
		if (board1.isGameOver())
			return "Player 2 has won";
		else if (board2.isGameOver())
			return "Player 1 has won";
		else
			return "";
	}

	public boolean wantsToPlayAgain() {
		int confirm = JOptionPane.showConfirmDialog(frame,
				"Would you like to play again?", "Rematch",
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION)
			frame.dispose();
		return confirm == JOptionPane.YES_OPTION;
	}

	/**
	 * @return the frame
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * @param frame
	 *            the frame to set
	 */
	protected void setFrame(JFrame frame) {
		this.frame = frame;
	}

	/**
	 * @return the board1
	 */
	public Board getBoard1() {
		return board1;
	}

	/**
	 * @return the board2
	 */
	public Board getBoard2() {
		return board2;
	}

	/**
	 * @return the textField
	 */
	public synchronized JTextField getTextField() {
		return textField;
	}

	/**
	 * @return the turns
	 */
	public int getTurns() {
		return turns;
	}

}
