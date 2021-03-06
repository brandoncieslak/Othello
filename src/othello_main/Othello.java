package othello_main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Othello {

	private static boolean autogame;

	public static void main(String args[]) {
		autogame = false;
		double timePerMove = 5.0;
		Player player = new Player(timePerMove);
		boolean startGame = false;
		Scanner in = new Scanner(System.in);
		System.out
				.println("Run Experiment or Play Game?\n(Type experiment or game)");
		while (!startGame) {
			String command = in.nextLine();
			if (command.contains("experiment")) {
				startGame = true;
				runExperiment();
			} else if (command.contains("game")) {
				startGame = true;
				runGame(player, player);
			}
		}
	}

	public static void runExperiment() {
		autogame = true;
		int totalGames;
		double player1TimePerMove;
		double player2TimePerMove;
		String settingsDescription;

		Properties defaultProp = new Properties();
		try {
			defaultProp.load(new FileInputStream("default.properties"));
		} catch (FileNotFoundException e1) {
			System.err.println("config.properties not found.");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Properties userProp = new Properties(defaultProp);
		try {
			userProp.load(new FileInputStream("user.properties"));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Get the values from the properties file

		totalGames = Integer.parseInt(userProp.getProperty("totalgames"));
		player1TimePerMove = Double.parseDouble(userProp
				.getProperty("blktimepermove"));
		player2TimePerMove = Double.parseDouble(userProp
				.getProperty("whttimepermove"));
		settingsDescription = userProp.getProperty("settingsdescription");

		Player black = new Player(player1TimePerMove);
		Player white = new Player(player2TimePerMove);
		int blacksum = 0;
		int firstties = 0;
		int win;
		for (int i = 0; i < (totalGames / 2); i++) {
			System.out.println("\nFirst half: Game number " + (i + 1));
			win = runGame(black, white);
			if (win == Board.BLACK) {
				blacksum += 1;
			} else if (win == Board.VACANT) {
				firstties += 1;
			}
		}

		black = new Player(player2TimePerMove);
		white = new Player(player1TimePerMove);
		int whitesum = 0;
		int secondties = 0;
		for (int i = 0; i < (totalGames / 2); i++) {
			System.out.println("\nSecond half: Game number " + (i + 1));
			win = runGame(black, white);
			if (win == Board.WHITE) {
				whitesum += 1;
			} else if (win == Board.VACANT) {
				secondties += 1;
			}
		}

		System.out.println();
		System.out.println("Description:\n");
		System.out.println(settingsDescription + "\n");
		System.out.println("First " + (totalGames / 2) + " games:");
		System.out.println("Black won " + blacksum + " games");
		System.out.println("Ties: " + firstties);
		System.out.println("\nSecond " + (totalGames / 2) + " games:");
		System.out.println("White won " + whitesum + " games");
		System.out.println("Ties: " + secondties);

		try {
			File file = new File("results.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream outStream = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(outStream);
			out.write("Description:\n" + settingsDescription + "\n");
			out.write("\nFirst " + (totalGames / 2) + " games:");
			out.write("\nBlack won " + blacksum + " games");
			out.write("\nTies: " + firstties);
			out.write("\nSecond " + (totalGames / 2) + " games:");
			out.write("\nWhite won " + whitesum + " games");
			out.write("\nTies: " + secondties);
			out.close();
		} catch (Exception e) {
		}
	}

	public static int runGame(Player black, Player white) {
		Board board = new Board();
		List<Integer> blackTurnPlayouts = new ArrayList<Integer>();
		List<Integer> whiteTurnPlayouts = new ArrayList<Integer>();
		String command;
		Scanner in = new Scanner(System.in);
		StringTokenizer token;
		while (!board.gameOver()) {
			if (!autogame) {
				command = in.nextLine();
			} else {
				command = "genmove";
			}
			if (command.contains("autogame")) {
				autogame = true;
			} else if (command.contains("play ")) {
				token = new StringTokenizer(command, " ");
				token.nextToken();
				if (!board.play(Board.stringToIndex(token.nextToken()))) {
					System.out.println("Invalid move.\n");
				}
			} else if(command.contains("settime ")) {
				token = new StringTokenizer(command, " ");
				token.nextToken();
				double time = Double.parseDouble(token.nextToken());
				if (black.setTimePerMove(time) && white.setTimePerMove(time)) {
					System.out.println("Time per move set to " + time
							+ " seconds.");
				} else {
					System.out.println("Invalid time.");
				}
			} else if (command.contains("showboard")) {
				System.out.println(board);
				System.out.println("\n"
						+ Board.colorToString(board.getColorToPlay())
						+ "'s turn:");
			} else if (command.contains("genmove")) {
				int playerMove;
				int turnPlayout;
				boolean showTree = false;
				if (command.contains("showtree")) {
					showTree = true;
				}
				long startTime = System.nanoTime();
				if (board.getColorToPlay() == Board.BLACK) {
					playerMove = black.getBestMove(board, showTree);
					turnPlayout = black.getPlayouts();
					blackTurnPlayouts.add(turnPlayout);
				} else {
					playerMove = white.getBestMove(board, showTree);
					turnPlayout = white.getPlayouts();
					whiteTurnPlayouts.add(turnPlayout);
				}
				int turnTime = (int) ((System.nanoTime() - startTime) / 1000000.0);
				board.play(playerMove);
				System.out.println("Player played at "
						+ Board.indexToString(playerMove));
				System.out.println("Player did " + turnPlayout
						+ " playouts in " + turnTime + " ms.\n");
			} else if (command.contains("quit")) {
				System.exit(0);
			} else {
				System.out.println("Invalid command.");
			}
		}
		System.out.println(board);
		int winner = board.getWinner();
		if (winner == Board.BLACK) {
			System.out.println("Black won!");
		} else if (winner == Board.WHITE) {
			System.out.println("White won!");
		} else {
			System.out.println("It's a tie!");
		}
		
		System.out.println("Black points: " + board.getScore(Board.BLACK));
		System.out.println("White points: " + board.getScore(Board.WHITE));

		double blackPlayoutSum = 0;
		double whitePlayoutSum = 0;
		for (Integer playouts : blackTurnPlayouts) {
			blackPlayoutSum += playouts;
		}
		for (Integer playouts : whiteTurnPlayouts) {
			whitePlayoutSum += playouts;
		}
		System.out.println("Average black playouts per turn: "
				+ (int) (blackPlayoutSum / blackTurnPlayouts.size()));
		System.out.println("Average white playouts per turn: "
				+ (int) (whitePlayoutSum / whiteTurnPlayouts.size()));
		return winner;
	}

}
