package com.loadingpleasewait.battleship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.MarshalledObject;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultCaret;

public class Battleship implements Serializable {

	public static final int WINDOW_HEIGHT = 900;
	public static final int WINDOW_WIDTH = 1400;
	
	public static final String JOINER = "rmi://127.0.0.1:1099/BattleshipJoiner";
	public static final String HOST = "rmi://127.0.0.1:1099/BattleshipHost";
	
	public static final String LAN_OPTION = "Network";
	public static final String LOCAL_OPTION = "Local";

	private static final long serialVersionUID = 2632923173759825918L;

	private JFrame frame;
	private Board board1;
	private Board board2;
	private Player player1;
	private Player player2;
	private JTextField textField;
	private JTextArea textArea;
	private boolean isPlayer2turn;
	private int turns;
	private boolean player1IsUser;
	private boolean player2IsUser;
	private boolean isRemoteGame;
	private String difficulty;
	private Board.BoardGraphics board1Graphics;
	private Board.BoardGraphics board2Graphics;
	private JScrollPane scroller;
	private DefaultCaret caret;

	private JProgressBar progressBar;
	private String address;
	private Registry registry;
	private RemotePlayer remoteOpponent;
	private RemoteUser localUser;
	private Thread updater1;
	private Thread updater2;
	private JTextArea turnPlayerText;
	private JPanel topPanel;
	private boolean lostConnection;

	public static void main(String[] args) {
		Battleship game = new Battleship();
		if (game.isRemoteGame) {
			game.setUpRemoteGame();
			while (game.winner().isEmpty()) {
				game.playTurn();
			}
			game.endGame();
		} else {
			do {
				game.setUpGame();
				while (game.winner().isEmpty()) {
					game.playTurn();
				}
				game.endGame();
			} while (game.wantsToPlayAgain());
		}
	}

	public Battleship() {
		frame = new JFrame("Battleship");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// ask user for type of game
		JOptionPane connectionOptions = new JOptionPane("Select game connection type");
		connectionOptions.setOptions(new String[] { LOCAL_OPTION, LAN_OPTION });
		connectionOptions.createDialog(frame, "Battleship Menu").setVisible(true);
		try {
			while (connectionOptions.getValue().toString() == null) {
				// wait for selection
				Thread.sleep(50);
			}
		} catch (InterruptedException ex) {

		} catch (NullPointerException ex) {
			System.exit(0);// user exited option pane
		}
		isRemoteGame = connectionOptions.getValue().toString().equals(LAN_OPTION);
	}

	public void setUpGame() {
		turns = 0;
		frame.getContentPane().removeAll();

		try {
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
				difficultyOptions.setOptions(new String[] { "Easy", "Medium", "Hard" });
				difficultyOptions.createDialog(getFrame(), "Difficulty").setVisible(true);
				while (difficultyOptions.getInputValue() == null)
					;// wait for user to choose
				difficulty = difficultyOptions.getValue().toString();
				// get turn order
				JOptionPane optionPane = new JOptionPane("Choose Turn order");
				optionPane.setOptions(new String[] { "First", "Second", "Random" });
				optionPane.createDialog(getFrame(), "Choose Turn Order").setVisible(true);
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
		} catch (NullPointerException ex) {
			System.exit(0);// if user exits while choosing options
		}

		setUpGUI();

		board1 = new Board();
		board2 = new Board();
		try {
			player1 = player1IsUser ? new User(board1, board2, getTextField()) : new BattleshipAI(board1, board2,
					difficulty);
			player2 = player2IsUser ? new User(board2, board1, getTextField()) : new BattleshipAI(board2, board1,
					difficulty);
		} catch (RemoteException ex) {
			showFatalError(ex);
		}

		board1Graphics = board1.new BoardGraphics(!player2IsUser);
		board2Graphics = board2.new BoardGraphics(!player1IsUser);

		getFrame().add(BorderLayout.WEST, board1Graphics);
		getFrame().add(BorderLayout.EAST, board2Graphics);

		startUpdaters();

		getFrame().setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
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

	public void setUpRemoteGame() {
		// set up GUI and connection
		setUpGUI();
		connect();

		//blank guess board when user is placing ships
		board2Graphics = new Board().new BoardGraphics(false);
		
		startUpdaters();

		board1 = localUser.getPlayerBoard();
		board1Graphics = board1.new BoardGraphics(true);

		if (localUser.isGoingFirst()) {
			getFrame().getContentPane().add(BorderLayout.EAST, board1Graphics);
			getFrame().getContentPane().add(BorderLayout.WEST, board2Graphics);
		} else {
			getFrame().getContentPane().add(BorderLayout.WEST, board1Graphics);
			getFrame().getContentPane().add(BorderLayout.EAST, board2Graphics);
		}

		getFrame().setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		getFrame().setVisible(true);

		localUser.placeShips();

		try {
			while (!localUser.isPlacedShips() || !remoteOpponent.isPlacedShips()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ex) {
					showFatalError(ex);
				}
			}
		} catch (RemoteException ex) {
			showFatalError(ex);
		}

		// set the guess board after opponent has placed ships
		try {
			localUser.setGuessBoard(new Board(remoteOpponent.getRemotePlayerBoard()));
			board2 = localUser.getGuessBoard();
			
			getFrame().getContentPane().remove(board2Graphics);
			board2Graphics = board2.new BoardGraphics(false);
			getFrame().getContentPane().add(board2Graphics);
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}


		textField.requestFocus();
	}

	/**
	 * The repaint() methods of board1Graphics and board2Graphics will be called
	 * every 50 milliseconds
	 */
	public void startUpdaters() {
		// constantly repaint boards with lambda expressions
		updater1 = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(60);
					board1Graphics.repaint();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} catch (NullPointerException ex){
					
				}
			}
		});// dat lambda expression

		updater2 = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(50);
					board2Graphics.repaint();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} catch (NullPointerException ex){
					
				}
			}
		});// dat second lambda expression

		updater1.start();
		updater2.start();
	}

	public void connect() {
		localUser = null;
		board1 = new Board();
		try {
			localUser = new RemoteUser(getBoard1(), getTextField());
		} catch (RemoteException ex1) {
			JOptionPane.showMessageDialog(frame, ex1);
			System.exit(1);
		}
		remoteOpponent = null;

		JFrame connectingFrame = new JFrame("Connecting");
		connectingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel label = new JLabel("", SwingConstants.CENTER);

		// ask user if they want to host
		JOptionPane connectionOptions = new JOptionPane("Create or join game");
		connectionOptions.setOptions(new String[] { "Host", "Join" });
		connectionOptions.createDialog(frame, "Battleship").setVisible(true);
		try {
			while (connectionOptions.getValue().toString() == null) {
				// wait for selection
				Thread.sleep(50);
			}
		} catch (NullPointerException ex) {
			System.exit(0);
		} catch (InterruptedException ex) {

		}

		if (connectionOptions.getValue().toString().equals("Host")) {

			// setup registry and bind object
			try {
				registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
				registry.bind(HOST, localUser);
				registry.bind(JOINER, new RemotePlaceHolder());
			} catch (RemoteException ex) {
				showFatalError(ex);
			} catch (AlreadyBoundException ex) {
				showFatalError("Error: The registry contains another host");
			}

			label.setText("Waiting for opponent to join");
			connectingFrame.add(label);
			connectingFrame.setSize(300, 300);
			connectingFrame.setVisible(true);

			// wait for opponent to join and get the remote object
			while (remoteOpponent == null) {
				try {
					remoteOpponent = (RemotePlayer) registry.lookup(JOINER);
					Thread.sleep(50);
				} catch (AccessException ex) {
					showFatalError(ex);
				} catch (RemoteException ex) {
					showFatalError(ex);
				} catch (NotBoundException ex) {

				} catch (InterruptedException ex) {

				} catch (ClassCastException ex) {
					// place holder is still there and user has not connected
				}
			}
			label.setText("Setting turn order");
			setTurnOrder();

		} else {

			// find the address of the host
			try {
				address = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException ex) {
				showFatalError(ex.getMessage());
			}
			address = address.substring(0, address.lastIndexOf(".") + 1);
			ArrayList<String> addresses = new ArrayList<String>();

			// add numbers 0 to 255 inclusive to list
			for (int i = 0; i < 256; i++) {
				addresses.add(address + i);
			}

			address = "not found";

			// make progress bar for connecting to host
			progressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 256 * 4);
			label.setText("Connecting to host");
			connectingFrame.add(BorderLayout.CENTER, label);
			connectingFrame.add(BorderLayout.SOUTH, progressBar);
			connectingFrame.setSize(300, 300);
			connectingFrame.setVisible(true);

			/*
			 * RMI is not consistent in choosing which address to use on
			 * different machines therefore this is used to find which address
			 * is being used testing asynchronously testing if each one is valid
			 * by testing it's socket and then if no valid address is found go
			 * through each and try getting a registry from the address
			 */
			addresses.forEach(possibleAddress -> {
				new Thread(() -> {
					if (address.equals("not found")) {
						try {
							// the thread will give up after 30 seconds
						new ThreadStopper(Thread.currentThread(), 30000).start();
						Socket socket = new Socket(possibleAddress, Registry.REGISTRY_PORT);
						Remote test = LocateRegistry.getRegistry(possibleAddress).lookup(JOINER);
						if (test instanceof PlaceHolder) {
							moveProgressBar(300);
							address = possibleAddress;
						}
						socket.close();
					} catch (IOException ex) {

					} catch (NotBoundException ex) {

					}
					moveProgressBar(3);
				}
			}	).start();
			});

			if (address.equals("not found")) {
				addresses.parallelStream().forEach(possibleAddress -> {
					if (addresses.equals("not found")) {
						moveProgressBar(1);
						try {
							remoteOpponent = (RemoteUser) LocateRegistry.getRegistry(possibleAddress).lookup(HOST);
							Remote test = LocateRegistry.getRegistry(possibleAddress).lookup(JOINER);
							if (test instanceof PlaceHolder) {
								moveProgressBar(200);
								address = possibleAddress;
							}
						} catch (IOException ex) {

						} catch (NotBoundException ex) {

						}
					}
				});
			}

			// wait for serverFinder to finish
			while (address.equals("not found") && progressBar.getValue() < progressBar.getMaximum() - 1) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}

			// if no valid server address is found exit
			if (address.equals("not found")) {
				JOptionPane.showMessageDialog(frame, "No host was found");
				System.exit(1);
			}

			// bind and look up objects
			try {
				remoteOpponent = (RemotePlayer) LocateRegistry.getRegistry(address).lookup(HOST);
				PlaceHolder placeHolder = (PlaceHolder) LocateRegistry.getRegistry(address).lookup(JOINER);
				placeHolder.proxyRebind(new MarshalledObject<RemoteUser>(localUser));

				connectingFrame.remove(progressBar);
				label.setText("Setting turn order");

				localUser.guessNumber();
				localUser.isGoingFirst();// wait for turn order to get set

			} catch (RemoteException | NotBoundException ex) {
				showFatalError(ex);
			} catch (ClassCastException ex) {
				showFatalError("Invalid data recieved");
			} catch (ClassNotFoundException | IOException ex) {
				showFatalError(ex);
			}

		}
		connectingFrame.dispose();
	}

	/**
	 * @param increment
	 *            amount to move the bar forward by
	 */
	public synchronized void moveProgressBar(int increment) {
		progressBar.setValue(Math.min(progressBar.getValue() + increment, progressBar.getMaximum()));
	}

	public class ThreadStopper extends Thread {

		private Thread thread;
		private int delay;

		/**
		 * @param thread
		 *            thread to be stopped
		 * @param delay
		 *            milliseconds to wait before stopping the thread
		 */
		public ThreadStopper(Thread thread, int delay) {
			this.thread = thread;
			this.delay = delay;
		}

		@Override
		public void run() {
			// sleep for delay then interrupt the thread
			try {
				sleep(delay);
			} catch (InterruptedException ex) {

			}
			thread.interrupt();
			moveProgressBar(3);
		}

	}

	public void setUpGUI() {
		//widget for displaying who's turn it is at top of GUI
		if (isRemoteGame) {
			topPanel = new JPanel(new GridBagLayout());
			turnPlayerText = new JTextArea();
			turnPlayerText.setEditable(false);
			turnPlayerText.setAlignmentX(Component.CENTER_ALIGNMENT);
			turnPlayerText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
			topPanel.add(turnPlayerText);
			getFrame().add(BorderLayout.NORTH, topPanel);
		}
		
		// make text components
		textField = new JTextField();
		textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);

		// automatically scroll down
		if(textArea.getCaret() instanceof DefaultCaret){
			caret = (DefaultCaret) textArea.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			textArea.setCaret(caret);
		}
		
		scroller = new JScrollPane(textArea);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setAutoscrolls(true);
		scroller.setPreferredSize(new Dimension(WINDOW_WIDTH - 50, 115));

		JPanel text = new JPanel();
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		text.add(scroller);
		text.add(textField);
		text.setPreferredSize(new Dimension(WINDOW_WIDTH - 50, 135));

		getFrame().add(BorderLayout.SOUTH, text);
	}

	public void setTurnOrder() {
		int correctNumber = (int) (Math.random() * (RemoteUser.MAX_GUESS + 1));

		// ask user to guess a number
		try {
			localUser.guessNumber();
		} catch (NullPointerException ex) {
			// user exited window
			try {
				remoteOpponent.notifyOpponentExit();
			} catch (RemoteException ex1) {

			}
			System.exit(0);
		}

		try {
			// if they guess the same number they will have to guess again
			while (Math.abs(correctNumber - localUser.getNumberGuess()) == Math.abs(correctNumber
					- remoteOpponent.getNumberGuess())) {
				localUser.requestGuessNumber();
				remoteOpponent.requestGuessNumber();
			}
			
			// whoever guesses closer to the actual number goes first
			boolean localGoesFirst = Math.abs(correctNumber - localUser.getNumberGuess()) < Math.abs(correctNumber
					- remoteOpponent.getNumberGuess());
			localUser.setGoingFirst(localGoesFirst);
			remoteOpponent.setGoingFirst(!localGoesFirst);
			
			// show both players the correct number at the same time
			new Thread(() -> {
				try{
					remoteOpponent.showNumber(correctNumber);
				}catch (RemoteException ex){

				}
			}).start();
			localUser.showNumber(correctNumber);

		} catch (RemoteException ex) {
			showFatalError(ex);
		}

	}

	/**
	 * Opens a JOptionPane that tells the user the error then exits
	 * 
	 * @param error
	 *            object containing information for the error message
	 */
	protected void showFatalError(Object error) {
		JOptionPane.showMessageDialog(getFrame(), error);
		System.exit(0);
	}

	// goes through a turn
	public void playTurn() {
		if (isRemoteGame) {
			//scroll down
			caret.setDot(textArea.getDocument().getLength() + 3);
			try {
				// call guess on correct object
				if (localUser.isGoingFirst() == isPlayer2turn) {
					// remote opponent's turn
					turnPlayerText.setText("Opponent's turn");
					turnPlayerText.setBackground(Color.RED);
					topPanel.setBackground(Color.RED);
					String opponentGuess = remoteOpponent.getRemoteGuess();
					board1.checkGuess(opponentGuess.substring(0,opponentGuess.indexOf("\n")));//update board
					textArea.append("Opponent: " + opponentGuess + "\n");
				} else {
					// local user's turn
					turnPlayerText.setText("Your turn");
					turnPlayerText.setBackground(Color.GREEN);
					topPanel.setBackground(Color.GREEN);
					textArea.append("Me: " + localUser.getGuess() + "\n");
				}
				textArea.append("        \n");//help with scrolling down
			} catch (RemoteException ex) {
				JOptionPane.showMessageDialog(getFrame(), "Lost connection to opponent");
				lostConnection = true;
			}
		} else {
			Player currentPlayer = isPlayer2turn ? player2 : player1;
			String playerName = isPlayer2turn ? "Player 2: " : "Player 1: ";
			textArea.append(playerName + currentPlayer.getGuess() + "\n");
		}
		
		if (isPlayer2turn)
			turns++;
		isPlayer2turn = !isPlayer2turn;
	}

	public void endGame() {
		assert (!winner().isEmpty()) : "Game ended early";
		board1Graphics.setShowShips(true);
		board2Graphics.setShowShips(true);
		textArea.append(winner() + "\n");
		textArea.append(turns + " turns\n");
		
		if(isRemoteGame){
			turnPlayerText.setText("GAME OVER");
		}
	}

	/**
	 * @return Player 1 or 2 has won or empty string if there is no winner
	 */
	public String winner() {
		if (isRemoteGame) {
			try {
				if (localUser.lost())
					return "Opponent has won.";
				else if (lostConnection || getBoard2().isGameOver())
					return "You win!";
				else
					return "";
			} catch (RemoteException ex) {
				JOptionPane.showMessageDialog(getFrame(), "Could not recieve data from opponent");
				return "";
			}
		} else {
			if (board1.isGameOver())
				return "Player 2 has won";
			else if (board2.isGameOver())
				return "Player 1 has won";
			else
				return "";
		}
	}

	public boolean wantsToPlayAgain() {
		int confirm = JOptionPane.showConfirmDialog(frame, "Would you like to play again?", "Rematch",
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
