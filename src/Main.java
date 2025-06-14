/**
 * Names: Samuel Xu, Edison Cai, and Rocky Shi
 * Date: 6/13/2025
 * Description: A computer version of the Guess Who board game. Players can
 * play against AI and play different game modes.
 */


// Importing libraries used for code
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import javax.sound.sampled.*;

public class Main {

	// Arraylist to store all characters 
    static ArrayList<Character> characterList = new ArrayList<>();
	
	// Array for question and a boolean array for remaining questions to ask for the AI and the player
    static String[] questionBank = {"Is the person a male?", "Is the eye colour brown?", "Is the eye colour green?", "Is the eye colour blue?", "Does the person have a light skin tone?", "Is the hair colour black?", "Is the hair colour brown?", "Is the hair colour ginger?", "Is the hair colour blonde?", "Is the hair colour white?", "Does the person have facial hair?", "Is the person wearing glasses?", "Does the person have visible teeth?", "Is the person wearing a hat?", "Does the person have short hair?", "Does the person have their hair tied up?", "Does the person have long hair?", "Is the person bald?", "Does the person have an ear piercing?"};
	static boolean[] AIAskedQuestions = new boolean[19];
	static boolean[] playerAskedQuestions = new boolean[19];
	
	// Arraylists to track removed characters per player step, used for undo button
	static ArrayList<Character> removedCharacters = new ArrayList<>();
	static ArrayList<Integer> removedCharactersCount = new ArrayList<>();
	static int removedCharactersCountPerQuestion;
	static ArrayList<String> AIQuestions = new ArrayList<>();

	// Game mode
	static boolean mergeQuestionsMode = false;
	// Number of games played
	static int gameCount = 0;

	// Index of the most optimal question to ask
    static int bestIndex;

	// AI's chosen Mystery Character
	static Character aiChoice;

	// Ai's YES or NO response to user's question
	static String aiResponse = "";

	// Initialize and track music playing
	static boolean musicIsPlaying;
	static Clip clip;
	static long clipTime;

	// Track current score and game winner for leaderboard
	static int p1Score = 0, p2Score = 0;	// P1 = AI, P2 = player
	static File leaderboard = new File("Leaderboard.txt");
	static PrintWriter pw;

	static JFrame gameFrame;
	static JPanel actionBar;

	public static void main(String[] args) throws Exception {
		pw = new PrintWriter(leaderboard.getName());
		prepareLeaderboard();
		loadAudio("GuessWhoMusic.wav");

        readCharacterFile();
		buildStartMenuGUI();

//        System.out.println("Welcome to Guess Who, by Samuel, Rocky and Edison");
//        gameMode();
        
        //buildStartMenuGUI();
    }

	/**
	 * This method reads in player information from a text file and stores them
	 * in a global arraylist to be used by the program.
	 * @throws FileNotFoundException
	 */
    public static void readCharacterFile() throws FileNotFoundException {
        // Get file objects from file path
        File file = new File("playerFile.txt");
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String name = scanner.nextLine();
            String gender = scanner.nextLine();
            String eyeColour = scanner.nextLine();
            String skinTone = scanner.nextLine();
            String hairColour = scanner.nextLine();
            boolean hasFacialHair = scanner.nextBoolean();
            boolean hasGlasses = scanner.nextBoolean();
            boolean hasVisibleTeeth = scanner.nextBoolean();
            boolean hasHat = scanner.nextBoolean();
            scanner.nextLine();
            String hairLength = scanner.nextLine();
            boolean hasPiercings = scanner.nextBoolean();
            scanner.nextLine();
            scanner.nextLine();

            // System.out.printf("%s %s %s %s %s %b %b %b %b %s %b\n",name,gender,eyeColour,skinTone,hairColour,hasFacialHair,hasGlasses,hasVisibleTeeth,hasHat,hairLength,hasPiercings);

            characterList.add(new Character(name,gender,eyeColour,skinTone,hairColour,hasFacialHair,hasGlasses,hasVisibleTeeth,hasHat,hairLength,hasPiercings));
        }
        scanner.close();
    }

	/**
	 * This method initializes and starts the music player.
	 * @param filePath	The file location of the .wav audio file.
	 * @throws UnsupportedAudioFileException	Exception handling.
	 * @throws LineUnavailableException			Exception handling.
	 */
	public static void loadAudio(String filePath) throws UnsupportedAudioFileException, LineUnavailableException {
		try {
			File audioFile = new File(filePath);
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			musicIsPlaying = true;
		} catch (Exception e) {
			System.out.println("Audio Error");
			e.printStackTrace();
		}

	}

	/**
	 * This method toggles/pauses the music for the user.
	 */
	public static void toggleMusic () {
		/*
		if (clipTime == clip.getMicrosecondLength()) {
			musicIsPlaying = false;
			clip.setMicrosecondPosition(0);
		}
		 */

		if (musicIsPlaying) {
			clipTime = clip.getMicrosecondPosition();
			clip.stop();
		} else {
			clip.setMicrosecondPosition(clipTime);
			clip.start();
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		}
		musicIsPlaying = !musicIsPlaying;
	}

	/**
	 * This method randomly determines which player goes first since AI does not have an age.
	 */
	public static void playerVsAI() {
    	// Randomly decide who goes first
    	if ((int) (Math.random()*2) == 0) {
    		buildQuestionDialog();
    	} else {
    		AIGuessing();
    	}

    }

	/**
	 * This method determines the action taken based on a question asked by the AI and the players' response. If the player
	 * makes a guess, end the game and determine the winner. If the player is responding to a question, eliminate
	 * the appropriate players based on their answer.
	 * @param question	Question asked by the player, could be an in-game question asked by the AI or a guess by the player.
	 * @param response	The response to the question asked by the player, could be YES or NO or a guessed character.
	 */
	public static void playerAnswer(String question, String response) { // AI removes character that the AI asked (Processes Player Answer)
		// Player makes a guess
		if (question.contains("Is your character")) {
			if (response.equals("yes")) {
				buildEndingGUI(false, "The opponent guessed your character.");
				p1Score++;
				writeToLeaderBoard("AI");
				gameFrame.dispose();
			} else {
				// We assume the AI is always correct and the player lied
				buildEndingGUI(false, "You made a mistake.");
				writeToLeaderBoard("ERROR");
				gameFrame.dispose();
			}
			gameCount++;
		}
		
		removedCharactersCountPerQuestion = 0;	// maintain the players removed by the AI this move
		// remove characters based on players' response to question asked by the AI
		switch (question) {
			case "Is the person a male?":
				if (response.equals("yes")) {
					removeCharacters("gender", "female");
				}
				else {
					removeCharacters("gender", "male");
				}
				break;
			case "Is the eye colour brown?":
				if (response.equals("yes")) {
					removeCharacters("eye colour", "green");
					removeCharacters("eye colour", "blue");
				}
				else {
					removeCharacters("eye colour", "brown");
				}
				break;
			case "Is the eye colour green?":
				if (response.equals("yes")) {
					removeCharacters("eye colour", "brown");
					removeCharacters("eye colour", "blue");
				}
				else {
					removeCharacters("eye colour", "green");
				}
				break;
			case "Is the eye colour blue?":
				if (response.equals("yes")) {
					removeCharacters("eye colour", "brown");
					removeCharacters("eye colour", "green");
				}
				else {
					removeCharacters("eye colour", "blue");
				}
				break;
			case "Does the person have a light skin tone?":
				if (response.equals("yes")) {
					removeCharacters("skin tone", "dark");
				}
				else {
					removeCharacters("skin tone", "light");
				}
				break;
			case "Is the hair colour black?":
				if (response.equals("yes")) {
					removeCharacters("hair colour", "brown");
					removeCharacters("hair colour", "ginger");
					removeCharacters("hair colour", "blonde");
					removeCharacters("hair colour", "white");
				}
				else {
					removeCharacters("hair colour", "black");
				}
				break;
			case "Is the hair colour brown?":
				if (response.equals("yes")) {
					removeCharacters("hair colour", "black");
					removeCharacters("hair colour", "ginger");
					removeCharacters("hair colour", "blonde");
					removeCharacters("hair colour", "white");
				}
				else {
					removeCharacters("hair colour", "brown");
				}
				break;
			case "Is the hair colour ginger?":
				if (response.equals("yes")) {
					removeCharacters("hair colour", "brown");
					removeCharacters("hair colour", "black");
					removeCharacters("hair colour", "blonde");
					removeCharacters("hair colour", "white");
				}
				else {
					removeCharacters("hair colour", "ginger");
				}
				break;
			case "Is the hair colour blonde?":
				if (response.equals("yes")) {
					removeCharacters("hair colour", "brown");
					removeCharacters("hair colour", "ginger");
					removeCharacters("hair colour", "black");
					removeCharacters("hair colour", "white");
				}
				else {
					removeCharacters("hair colour", "blonde");
				}
				break;
			case "Is the hair colour white?":
				if (response.equals("yes")) {
					removeCharacters("hair colour", "brown");
					removeCharacters("hair colour", "ginger");
					removeCharacters("hair colour", "blonde");
					removeCharacters("hair colour", "black");
				}
				else {
					removeCharacters("hair colour", "white");
				}
				break;
			case "Does the person have facial hair?":
				if (response.equals("yes")) {
					removeCharacters("facial hair", "false");
				}
				else {
					removeCharacters("hair colour", "true");
				}
				break;
			case "Is the person wearing glasses?":
				if (response.equals("yes")) {
					removeCharacters("glasses", "false");
				}
				else {
					removeCharacters("glasses", "true");
				}
				break;
			case "Does the person have visible teeth?":
				if (response.equals("yes")) {
					removeCharacters("visibility of teeth", "false");
				}
				else {
					removeCharacters("visibility of teeth", "true");
				}
				break;
			case "Is the person wearing a hat?":
				if (response.equals("yes")) {
					removeCharacters("wearing of hat", "false");
				}
				else {
					removeCharacters("wearing of hat", "true");
				}
				break;
			case "Does the person have short hair?":
				if (response.equals("yes")) {
					removeCharacters("hair length", "tied");
					removeCharacters("hair length", "long");
					removeCharacters("hair length", "bald");
				}
				else {
					removeCharacters("hair length", "short");
				}
				break;
			case "Does the person have their hair tied up?":
				if (response.equals("yes")) {
					removeCharacters("hair length", "short");
					removeCharacters("hair length", "long");
					removeCharacters("hair length", "bald");
				}
				else {
					removeCharacters("hair length", "tied");
				}
				break;
			case "Does the person have long hair?":
				if (response.equals("yes")) {
					removeCharacters("hair length", "short");
					removeCharacters("hair length", "tied");
					removeCharacters("hair length", "bald");
				}
				else {
					removeCharacters("hair length", "long");
				}
				break;
			case "Is the person bald?":
				if (response.equals("yes")) {
					removeCharacters("hair length", "short");
					removeCharacters("hair length", "tied");
					removeCharacters("hair length", "long");
				}
				else {
					removeCharacters("hair length", "bald");
				}
				break;
			case "Does the person have an ear piercing?":
				if (response.equals("yes")) {
					removeCharacters("piercings", "false");
				}
				else {
					removeCharacters("piercings", "true");
				}
				break;
		}
		
		removedCharactersCount.add(removedCharactersCountPerQuestion);
	}

	/**
	 * When the player asks a question, this is where the AI responds
	 * Goes through every single question, and compares the attribute with the AI's chosen attribute
	 * Responds with yes/no accordingly
	 *
	 * @param playerQuestion		the question that the player asks
	 */

	public static void getPlayerQuestions(String playerQuestion) { // AI responds with yes/no, player asks question
		if (playerQuestion.contains("Is your character")) { // If character asks to guess
			if (playerQuestion.contains(aiChoice.getName())) {
				// Win
				buildEndingGUI(true, "You guessed the opponents character!");
				p2Score++;
				writeToLeaderBoard("Player");
				gameFrame.dispose();
			} else {
				// Lose
				buildEndingGUI(false, "You didn't guess the opponents character");
				p1Score++;
				writeToLeaderBoard("AI");
				gameFrame.dispose();
			}
		} else {
			switch (playerQuestion) { // Check all questions, and respond accordingly
				case "Is the person a male?":
					if (aiChoice.getGender().equals("male")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the eye colour brown?":
					if (aiChoice.getEyeColour().equals("brown")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the eye colour green?":
					if (aiChoice.getEyeColour().equals("green")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the eye colour blue?":
					if (aiChoice.getEyeColour().equals("blue")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Does the person have a light skin tone?":
					if (aiChoice.getSkinTone().equals("light")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the hair colour black?":
					if (aiChoice.getHairColour().equals("black")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the hair colour brown?":
					if (aiChoice.getHairColour().equals("brown")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the hair colour ginger?":
					if (aiChoice.getHairColour().equals("ginger")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the hair colour blonde?":
					if (aiChoice.getHairColour().equals("blonde")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the hair colour white?":
					if (aiChoice.getHairColour().equals("white")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Does the person have facial hair?":
					if (aiChoice.getFacialHair()) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the person wearing glasses?":
					if (aiChoice.getGlasses()) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Does the person have visible teeth?":
					if (aiChoice.getVisibilityOfTeeth()) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the person wearing a hat?":
					if (aiChoice.getWearingOfHat()) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Does the person have short hair?":
					if (aiChoice.getHairLength().equals("short")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Does the person have their hair tied up?":
					if (aiChoice.getHairLength().equals("tied")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Does the person have long hair?":
					if (aiChoice.getHairLength().equals("long")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Is the person bald?":
					if (aiChoice.getHairLength().equals("bald")) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
				case "Does the person have an ear piercing?":
					if (aiChoice.getPiercings()) {
						aiResponse = "yes";
					} else {
						aiResponse = "no";
					}
					break;
			}
		}
	}

	/**
	 * This method initializes the header for the leaderboard and writes it
	 * to a text file.
	 */
	public static void prepareLeaderboard () {
		pw.println("Here is the leaderboard");
		pw.println("------------------------------------------------");
	}

	/**
	 * This method appends the winner of the current game to the leader
	 * text file.
	 * @param winner	The person who won the current game, either player or AI
	 */
	public static void writeToLeaderBoard (String winner) {
		if (!winner.equals("ERROR")) {
			pw.println("Game " + gameCount + " won by " + winner);
		} else {
			pw.println("Game " + gameCount + " resulted in an error");
		}
	}

	/**
	 * This method prints the final statistics between the matches played. This method
	 * is called when the player wishes the exit the game completely.
	 */
	public static void generateFinalLeaderboard () {
		// where P1 = AI, P2 = player
		if (p1Score > p2Score) {
			pw.println("AI won " + p1Score + " to " + p2Score);
		} else if (p2Score > p1Score) {
			pw.println("Player won " + p2Score + " to " + p1Score);
		} else {
			pw.println("Player and AI tied with " + p1Score);
		}
		pw.println("AI W/L ratio " + p1Score + ":" + p2Score);
		pw.println("Player W/L ratio " + p2Score + ":" + p1Score);
		pw.println("Thanks for playing Guess Who");

		pw.close();
	}

	/**
	 * This is where our AI chooses the best question to ask. This is how it works:
	 * 1. First, we need to understand how to get the best questions. We need to split the questions as evenly as possible, just like binary search.
	 * So we split half each time, and we will have the best questions to ask
	 * 2. That is essentially what our algorithm does. Loop through every question, see how many people follow and don't follow this requirement,
	 * then pick the one that is closest to half and half. We do the check with the absolute difference, seeing which one is closest to zero.
	 * 3. Before any of that, we first check how many people are valid. If there is only one character remaining, then we just ask if that is their character
	 */

	public static void AIGuessing() { // AI picks the most optimal question to ask
		int validCount = 24;
		String lastRemaining = "";
		for (int i = 0; i < characterList.size(); i++) {
			if (!characterList.get(i).getVisibility()) {
				validCount--;
			}
			else {
				lastRemaining = characterList.get(i).getName();
			}
		}

		if (validCount == 1) { // Only one character left
			buildAnswerDialog("Is your character:" + lastRemaining);
		}
		else {
			String bestGuess = "";
			int bestCount = Integer.MAX_VALUE;
			for (int i = 0; i < questionBank.length; i++) {
				int countYes = 0, countNo = 0;
				String question = questionBank[i];
				if (AIAskedQuestions[i]) {
					continue;
				}
				switch (question) {
					case "Is the person a male?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getGender().equals("male")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the eye colour brown?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getEyeColour().equals("brown")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the eye colour green?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getEyeColour().equals("green")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the eye colour blue?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getEyeColour().equals("blue")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Does the person have a light skin tone?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getSkinTone().equals("light")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the hair colour black?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getHairColour().equals("black")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the hair colour brown?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getHairColour().equals("brown")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the hair colour ginger?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getHairColour().equals("ginger")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the hair colour blonde?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getHairColour().equals("blonde")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the hair colour white?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getHairColour().equals("white")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Does the person have facial hair?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getFacialHair()) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the person wearing glasses?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getGlasses()) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Does the person have visible teeth?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getVisibilityOfTeeth()) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the person wearing a hat?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getWearingOfHat()) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Does the person have short hair?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getHairLength().equals("short")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Does the person have their hair tied up?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getHairLength().equals("tied")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Does the person have long hair?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getHairLength().equals("long")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Is the person bald?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getHairLength().equals("bald")) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
					case "Does the person have an ear piercing?":
						for (int j = 0; j < characterList.size(); j++) {
							if (characterList.get(j).getVisibility()) {
								if (characterList.get(j).getPiercings()) {
									countYes++;
								} else {
									countNo++;
								}
							}
						}
						break;
				}

				if (Math.abs(countYes - countNo) < bestCount) {
					bestCount = Math.abs(countYes - countNo);
					bestGuess = question;
					bestIndex = i;
				}
			}

			AIAskedQuestions[bestIndex] = true;
			if (mergeQuestionsMode) playerAskedQuestions[bestIndex] = true;
			AIQuestions.add(bestGuess);
			buildAnswerDialog(bestGuess);
		}
    }

	/**
	 * This method processes the user's response to the AI's question, and tracks the removed characters
	 * removedCharacters gets the removed characters added, and at the end the counter is added to removedCharactersCount
	 * Uses case switch statements to find which question this matches up to
	 * @param attribute			the attribute of the character that we are looking for
	 * @param specifics			the specific attribute that will be removed
	 */
	public static void removeCharacters(String attribute, String specifics) {
        switch (attribute) {
            case "gender":
                for (int i = 0; i < characterList.size(); i++) {
        			if (characterList.get(i).getVisibility() && characterList.get(i).getGender().equals(specifics)) {
        				characterList.get(i).toggleVisibility();
        				removedCharacters.add(characterList.get(i));
        				removedCharactersCountPerQuestion++;
        			}
        		}
                break;
        	case "eye colour":
        	    for (int i = 0; i < characterList.size(); i++) {
        			if (characterList.get(i).getVisibility() && characterList.get(i).getEyeColour().equals(specifics)) {
        				characterList.get(i).toggleVisibility();
        				removedCharacters.add(characterList.get(i));
        				removedCharactersCountPerQuestion++;
        			}
        		}
        	    break;
        	case "skin tone":
        	    for (int i = 0; i < characterList.size(); i++) {
        			if (characterList.get(i).getVisibility() && characterList.get(i).getSkinTone().equals(specifics)) {
        				characterList.get(i).toggleVisibility();
        				removedCharacters.add(characterList.get(i));
        				removedCharactersCountPerQuestion++;
        			}
        		}
        	    break;
        	case "hair colour":
        	    for (int i = 0; i < characterList.size(); i++) {
        			if (characterList.get(i).getVisibility() && characterList.get(i).getHairColour().equals(specifics)) {
        				characterList.get(i).toggleVisibility();
        				removedCharacters.add(characterList.get(i));
        				removedCharactersCountPerQuestion++;
        			}
        		}
        	    break;
        	case "facial hair":
        	    for (int i = 0; i < characterList.size(); i++) {
        	    	if (specifics.equals("true")) {
        	    		if (characterList.get(i).getVisibility() && characterList.get(i).getFacialHair()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        	    	else {
        	    		if (characterList.get(i).getVisibility() && !characterList.get(i).getFacialHair()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        		}
        	    break;
        	case "glasses":
        		for (int i = 0; i < characterList.size(); i++) {
        	    	if (specifics.equals("true")) {
        	    		if (characterList.get(i).getVisibility() && characterList.get(i).getGlasses()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        	    	else {
        	    		if (characterList.get(i).getVisibility() && !characterList.get(i).getGlasses()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        		}
        		break;
        	case "visibility of teeth":
        		for (int i = 0; i < characterList.size(); i++) {
        	    	if (specifics.equals("true")) {
        	    		if (characterList.get(i).getVisibility() && characterList.get(i).getVisibilityOfTeeth()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        	    	else {
        	    		if (characterList.get(i).getVisibility() && !characterList.get(i).getVisibilityOfTeeth()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        		}
        		break;
        	case "wearing of hat":
        		for (int i = 0; i < characterList.size(); i++) {
        	    	if (specifics.equals("true")) {
        	    		if (characterList.get(i).getVisibility() && characterList.get(i).getWearingOfHat()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        	    	else {
        	    		if (characterList.get(i).getVisibility() && !characterList.get(i).getWearingOfHat()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        		}
        		break;
        	case "hair length":
        	    for (int i = 0; i < characterList.size(); i++) {
        			if (characterList.get(i).getVisibility() && characterList.get(i).getHairLength().equals(specifics)) {
        				characterList.get(i).toggleVisibility();
        				removedCharacters.add(characterList.get(i));
        				removedCharactersCountPerQuestion++;
        			}
        		}
        	    break;
        	case "piercings":
        		for (int i = 0; i < characterList.size(); i++) {
        	    	if (specifics.equals("true")) {
        	    		if (characterList.get(i).getVisibility() && characterList.get(i).getPiercings()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        	    	else {
        	    		if (characterList.get(i).getVisibility() && !characterList.get(i).getPiercings()) {
            				characterList.get(i).toggleVisibility();
            				removedCharacters.add(characterList.get(i));
            				removedCharactersCountPerQuestion++;
            			}
        	    	}
        		}
        		break;
        }
    }

	/**
	 * This method is used for undoing moves that the player messed up on
	 * How it works:
	 * 1. Removes last value from removedCharactersCount. This gives the code how many characters were removed on the last step
	 * 2. Remove that many characters from the removedCharacters arraylist.
	 * 3. Display the AI's last asked question, using AiQuestions
	 */

	public static void undoMove() {
    	if (removedCharactersCount.isEmpty()) {
    		System.out.println("You can't do this!");
    		return;
    	}
    	
    	int lastQuestionRemovedValues = removedCharactersCount.get(removedCharactersCount.size() - 1);
    	removedCharactersCount.remove(removedCharactersCount.size() - 1);
    	while (lastQuestionRemovedValues > 0) {
    		Character removedCharacter = removedCharacters.get(removedCharacters.size() - 1);
    		removedCharacters.remove(removedCharacters.size() - 1);
    		removedCharacter.resetVisibility();
    		lastQuestionRemovedValues--;
    	}
    	
    	// Display last AI asked question
    	String lastQuestion = AIQuestions.get(AIQuestions.size() - 1);
    	// display last question on GUI
    	aiResponse = "undo";
    	
    	buildAnswerDialog(lastQuestion);
    }

	/**
	 * This method builds the main menu for the game. It includes the play button, instructions, settings, and an exit
	 * button. You can also select the "merge questions" mode, which will change certain rules of the game.
 	 */
	public static void buildStartMenuGUI() {

        // Frame which holds all components
        JFrame welcomeFrame = new JFrame();
        welcomeFrame.setTitle("Guess Who Assignment 2025");
        welcomeFrame.setSize(1920, 1080);
        welcomeFrame.setLayout(new GridBagLayout());
        welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();

        // PreTitle Label
        JLabel preTitle = new JLabel();
        preTitle.setText("Welcome to Samuel, Edison & Rocky's");
        preTitle.setFont(new Font("Roboto", Font.PLAIN, 48));
        preTitle.setHorizontalAlignment(JLabel.CENTER);
        preTitle.setVerticalAlignment(JLabel.CENTER);
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        welcomeFrame.add(preTitle, gbc);

        // Title Label
        JLabel title = new JLabel();
        title.setText("Guess Who Assignment 2025");
        title.setFont(new Font("Roboto", Font.BOLD, 60));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setVerticalAlignment(JLabel.CENTER);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        welcomeFrame.add(title, gbc);

		// Play Button
        JButton playButton = new JButton();
		playButton.setText("Play");
		playButton.setFont(new Font("Roboto", Font.PLAIN, 32));
		playButton.setMargin(new Insets(10,10,10,10));
		playButton.setHorizontalAlignment(JLabel.CENTER);
		playButton.setVerticalAlignment(JLabel.CENTER);
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(60,0,0,0);

		playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				// Randomly choose character for AI
				int chosenOne = (int) (Math.random() * 24);
				aiChoice = characterList.get(chosenOne);

				// Reset all gameplay variables
				for (int i = 0; i < playerAskedQuestions.length; i++) {
					playerAskedQuestions[i] = false;
					AIAskedQuestions[i] = false;
				}
				removedCharacters.clear();
				removedCharactersCount.clear();
				AIQuestions.clear();

				for (int i = 0; i < characterList.size(); i++) {
					characterList.get(i).resetVisibility();
				}

				aiResponse = "";

				// Start game
				buildGameGUI();
				playerVsAI();

				welcomeFrame.dispose();
            }
        } );

        welcomeFrame.add(playButton, gbc);

		// Game Modifier Checkboxes
		JPanel gameModifiers = new JPanel(new FlowLayout());
		JLabel mergeQuestionsLabel = new JLabel();
		mergeQuestionsLabel.setText("Merge Questions Mode");
		mergeQuestionsLabel.setFont(new Font("Roboto", Font.PLAIN, 24));
		gameModifiers.add(mergeQuestionsLabel);
		JCheckBox mergeQuestionsCheckBox = new JCheckBox();
		mergeQuestionsCheckBox.setSize(30,30);
		mergeQuestionsCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// Toggle merge question mode based on checkbox selection
                mergeQuestionsMode = e.getStateChange() == ItemEvent.SELECTED;
			}
		});
		gameModifiers.add(mergeQuestionsCheckBox);

		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0,0,0,0);
		welcomeFrame.add(gameModifiers, gbc);

		// Settings Button
		JButton settingsButton = new JButton();
		settingsButton.setText("Settings");
		settingsButton.setFont(new Font("Roboto", Font.PLAIN, 32));
		settingsButton.setMargin(new Insets(10,10,10,10));
		settingsButton.setHorizontalAlignment(JLabel.CENTER);
		settingsButton.setVerticalAlignment(JLabel.CENTER);
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(60,0,0,0);
		settingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buildSettingsGUI();
				welcomeFrame.dispose();
			}
		} );
		welcomeFrame.add(settingsButton, gbc);

		// Instructions Button
		JButton instructionsButton = new JButton();
		instructionsButton.setText("How To Play");
		instructionsButton.setFont(new Font("Roboto", Font.PLAIN, 32));
		instructionsButton.setMargin(new Insets(10,10,10,10));
		instructionsButton.setHorizontalAlignment(JLabel.CENTER);
		instructionsButton.setVerticalAlignment(JLabel.CENTER);
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(60,0,0,0);
		instructionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buildInstructions();

				welcomeFrame.dispose();
			}
		} );
		welcomeFrame.add(instructionsButton, gbc);

        // Exit Button
        JButton exitButton = new JButton();
        exitButton.setText("Exit Program");
        exitButton.setFont(new Font("Roboto", Font.PLAIN, 32));
        exitButton.setMargin(new Insets(10,10,10,10));
        exitButton.setHorizontalAlignment(JLabel.CENTER);
        exitButton.setVerticalAlignment(JLabel.CENTER);
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(60,0,0,0);
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateFinalLeaderboard();
				System.exit(0); // Terminates the program
            }
        } );
        welcomeFrame.add(exitButton, gbc);

        welcomeFrame.setVisible(true);
    }

	/**
	 * This method builds the main game GUI. This shows the character cards, as well as the action bar at the bottom.
	 */
    public static void buildGameGUI() {

		// Main game frame
		gameFrame = new JFrame();
    	gameFrame.setLayout(new BorderLayout());
    	gameFrame.setTitle("Guess Who Assignment 2025");
    	gameFrame.setSize(1920, 1080);
    	gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	GridBagConstraints gbc = new GridBagConstraints();

    	JPanel gridPanel = new JPanel();
    	gridPanel.setLayout(new GridLayout(4,6));

		// Build all character cards in a grid layout
    	for (int i = 0; i < characterList.size(); i++) {
    		CharacterCard charCard = new CharacterCard(characterList.get(i));

    		gridPanel.add(charCard.buildCharacterCardGUI());
    	}
    	gameFrame.add(gridPanel,BorderLayout.CENTER);

		// Action Bar
		actionBar = new JPanel();
		actionBar.setLayout(new GridBagLayout());
		gameFrame.add(actionBar,BorderLayout.SOUTH);

		gameFrame.pack();
    	gameFrame.setVisible(true);
    }

	/**
	 *  This method builds the GUI for the question dialog. This allows the character to ask questions about the opponents
	 *  character from a dropdown, as well as submitting a final guess. There is also an undo button if the user makes
	 *  a mistake answering one of the questions.
	 */
	public static void buildQuestionDialog() {
		actionBar.removeAll(); // Reset action bar

		GridBagConstraints gbc = new GridBagConstraints();

		// Undo Button
		JPanel undoPanel = new JPanel(new FlowLayout());

		JButton undoButton = new JButton();
		undoButton.setText("Undo");
		undoButton.setFont(new Font("Roboto", Font.PLAIN, 16));
		undoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undoMove();
			}
		} );
		undoPanel.add(undoButton);
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0,60,0,60);
		actionBar.add(undoPanel,gbc);

		// Guess Character Form
		JPanel guessForm = new JPanel(new FlowLayout());
		// Convert ArrayList to String[]
		String[] validCharacters = new String[characterList.size()];
		for (int i = 0; i < characterList.size(); i++) {
			validCharacters[i] = characterList.get(i).getName();
		}

		// Guess Dropdown
		JComboBox guessDropDown = new JComboBox(validCharacters); // questionBank -> validQuestions
		guessDropDown.setFont(new Font("Roboto", Font.BOLD, 16));
		guessForm.add(guessDropDown);

		// Guess Submit Button
		JButton submitGuessButton = new JButton();
		submitGuessButton.setText("SUBMIT GUESS");
		submitGuessButton.setFont(new Font("Roboto", Font.PLAIN, 16));
		submitGuessButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String playerGuess = String.valueOf(guessDropDown.getSelectedItem());

				getPlayerQuestions("Is your character " + playerGuess);
			}
		} );
		guessForm.add(submitGuessButton);

		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0,60,0,60);
		actionBar.add(guessForm,gbc);

		// Ask Question Form
		JPanel questionForm = new JPanel(new FlowLayout());

		// Get questions that haven't been asked yet
		int validQuestionCount = 0;
		for (int i = 0; i < playerAskedQuestions.length; i++) {
			if (!playerAskedQuestions[i]) {
				validQuestionCount++;
			}
		}
		String[] validQuestions = new String[validQuestionCount];
		int idx = 0;
		for (int i = 0; i < questionBank.length; i++) {
			if (!playerAskedQuestions[i]) {
				validQuestions[idx] = questionBank[i];
				idx++;
			}
		}

		// Question Dropdown
		JComboBox questionDropDown = new JComboBox(validQuestions); // questionBank -> validQuestions
		questionDropDown.setFont(new Font("Roboto", Font.BOLD, 16));
		questionForm.add(questionDropDown);

		// Submit Question Button
		JButton submitQuestionButton = new JButton();
		submitQuestionButton.setText("SUBMIT");
		submitQuestionButton.setFont(new Font("Roboto", Font.PLAIN, 16));
		submitQuestionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String playerQuestion = String.valueOf(questionDropDown.getSelectedItem()); // Get selected question

				// Computer player's question
				getPlayerQuestions(playerQuestion);

				// Set question as asked
				for (int i = 0; i < questionBank.length; i++) {
					if (questionBank[i].equals(playerQuestion)) {
						playerAskedQuestions[i] = true;
						if (mergeQuestionsMode) AIAskedQuestions[i] = true;
						break;
					}
				}

				// AI's turn
				AIGuessing();
			}
		} );
		questionForm.add(submitQuestionButton);

		gbc.gridx = 2;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0,60,0,60);
		actionBar.add(questionForm,gbc);
		actionBar.revalidate();
		actionBar.repaint();
	}

	/**
	 * This method builds the GUI for the answer dialog. This allows the player to answer the AI's question about their
	 * character. It also contains the response from the AI, so the user can disable any characters on the game grid.
	 *
	 * @param question - The question the AI asks
	 */
	public static void buildAnswerDialog(String question) {
		actionBar.removeAll(); // Reset action bar

		GridBagConstraints gbc0 = new GridBagConstraints();

		// Response Form
		JPanel responseForm = new JPanel();
		JLabel responseLabel = new JLabel();
		responseLabel.setText(aiResponse.toUpperCase());
		responseLabel.setFont(new Font("Roboto", Font.BOLD, 16));
		responseLabel.setBorder(new EmptyBorder(10,0,10,0));

		responseForm.add(responseLabel);

		gbc0.gridx = 0;
		gbc0.gridwidth = 1;
		gbc0.insets = new Insets(0,60,0,60);
		actionBar.add(responseForm,gbc0);

		// Answer Form
		JPanel answerForm = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		// Question Label
		JLabel questionLabel = new JLabel();
		questionLabel.setText(question);
		questionLabel.setFont(new Font("Roboto", Font.PLAIN, 16));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 8;
		gbc.anchor = GridBagConstraints.CENTER;
		answerForm.add(questionLabel,gbc);

		// Yes Button
		JButton yesButton = new JButton();
		yesButton.setText("YES");
		yesButton.setFont(new Font("Roboto", Font.PLAIN, 16));
		yesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playerAnswer(question,"yes");

				buildQuestionDialog();
			}
		} );
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		answerForm.add(yesButton,gbc);

		// No Button
		JButton noButton = new JButton();
		noButton.setText("NO");
		noButton.setFont(new Font("Roboto", Font.PLAIN, 16));
		noButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playerAnswer(question,"no");

				buildQuestionDialog();
			}
		} );
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		answerForm.add(noButton,gbc);

		gbc0.gridx = 1;
		gbc0.gridwidth = 3;
		gbc0.insets = new Insets(0,60,0,60);
		actionBar.add(answerForm,gbc0);
		actionBar.revalidate();
		actionBar.repaint();
	}

	/**
	 * This method builds the instructions frame for the player. It includes all necessary information to play our game.
	 */
	public static void buildInstructions () {

		// Main Frame
		JFrame instructionsFrame = new JFrame();
		instructionsFrame.setLayout(new BorderLayout());
		instructionsFrame.setTitle("Instructions");
		instructionsFrame.setSize(1920, 1080);
		instructionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GridBagConstraints gbc = new GridBagConstraints();

		// Title Label
		JLabel title = new JLabel();
		title.setText("Guess Who Instructions");
		title.setFont(new Font("Roboto", Font.BOLD, 40));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setVerticalAlignment(JLabel.TOP);
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		instructionsFrame.add(title, BorderLayout.NORTH);

		// Instructions Label
		JTextArea instructionsPanel = new JTextArea();
		instructionsPanel.setText(	"Your objective is to guess the Mystery Person on your\n" +
									"opponent's card. To start select your own mystery person\n" +
									"and keep their attributes in mind!\n" +
									"------------------------------------------------------------\n" +
									"Until you're ready to guess who the Mystery Person is,\n" +
									"ask your opponent one YES or NO question per turn.\n" +
									"Questions are available in the drop down menu on the\n" +
									"right. After your opponent answers, you may be able to\n" +
									"eliminate one or more gameboard faces by clicking on\n" +
									"the characters. You will then be asked a question by your\n" +
									"opponent. Answer YES or NO based on your chosen character.\n" +
									"------------------------------------------------------------\n" +
									"When you're ready to guess who the Mystery Person is, make\n" +
									"your guess in the left drop down menu on your turn.\n" +
									"------------------------------------------------------------\n" +
									"Players alternate turns asking questions until one player\n" +
									"makes a guess. If you guess correctly or your opponent\n" +
									"guesses incorrectly you win the game!\n\n" +
									"------------------------------------------------------------\n" +
									"MERGE MODE (ONLY AVAILABLE ON ERS COPIES OF GUESS WHO)\n" +
									"The exact same rules for standard Guess Who apply.\n" +
									"In merge mode, question options between both players\n" +
									"are merged into one pool. Each question from the question\n" +
									"bank can only be asked once throughout the game. This\n" +
									"means if you ask a question, your opponent cannot ask you\n" +
									"that question and vice-versa. Select your questions wisely!");
		instructionsPanel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
		instructionsPanel.setOpaque(false);
		instructionsPanel.setForeground(Color.black);
		instructionsPanel.setEditable(false);
		instructionsPanel.setHighlighter(null);
		instructionsFrame.add(instructionsPanel, BorderLayout.CENTER);

		// Main Menu Button
		JButton mainMenuButton = new JButton();
		mainMenuButton.setText("Main Menu");
		mainMenuButton.setFont(new Font("Roboto", Font.PLAIN, 32));
		mainMenuButton.setMargin(new Insets(10,10,10,10));
		mainMenuButton.setHorizontalAlignment(JLabel.CENTER);
		mainMenuButton.setVerticalAlignment(JLabel.CENTER);
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(60,0,0,0);

		mainMenuButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buildStartMenuGUI(); // Return to main menu

				instructionsFrame.dispose();
			}
		} );

		instructionsFrame.add(mainMenuButton, BorderLayout.SOUTH);
		instructionsFrame.pack();
		instructionsFrame.setVisible(true);
	}

	/**
	 * This method builds the final end screen when the game finishes. It includes a final message and the user can return
	 * back to the main menu.
	 *
	 * @param win - Whether the player won the game
	 * @param endMessage - End message; Reason why the user won or lost
	 */
	public static void buildEndingGUI(boolean win, String endMessage) {

		// Main Frame
		JFrame endFrame = new JFrame();
		endFrame.setLayout(new GridBagLayout());
		endFrame.setTitle(win ? "YOU WON!" : "YOU LOST!");
		endFrame.setSize(1920, 1080);
		endFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GridBagConstraints gbc = new GridBagConstraints();

		// Title Label
		JLabel title = new JLabel();
		title.setText(win ? "YOU WON!" : "YOU LOST!");
		title.setFont(new Font("Roboto", Font.BOLD, 60));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setVerticalAlignment(JLabel.CENTER);
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		endFrame.add(title, gbc);

		// End Message Label
		JLabel subtitle = new JLabel();
		subtitle.setText(endMessage);
		subtitle.setFont(new Font("Roboto", Font.PLAIN, 32));
		subtitle.setHorizontalAlignment(JLabel.CENTER);
		subtitle.setVerticalAlignment(JLabel.CENTER);
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		endFrame.add(subtitle, gbc);

		// Main Menu Button
		JButton mainMenuButton = new JButton();
		mainMenuButton.setText("Main Menu");
		mainMenuButton.setFont(new Font("Roboto", Font.PLAIN, 32));
		mainMenuButton.setMargin(new Insets(10,10,10,10));
		mainMenuButton.setHorizontalAlignment(JLabel.CENTER);
		mainMenuButton.setVerticalAlignment(JLabel.CENTER);
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(60,0,0,0);

		mainMenuButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buildStartMenuGUI(); // Return to main menu

				endFrame.dispose();
			}
		} );

		endFrame.add(mainMenuButton, gbc);

		endFrame.setVisible(true);
	}

	/**
	 * This method builds the settings GUI so the user can toggle on/off music
	 */
	public static void buildSettingsGUI() {

		// Main Frame
		JFrame settingsFrame = new JFrame();
		settingsFrame.setLayout(new GridBagLayout());
		settingsFrame.setTitle("Settings");
		settingsFrame.setSize(1920, 1080);
		settingsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GridBagConstraints gbc = new GridBagConstraints();

		// Title Label
		JLabel title = new JLabel();
		title.setText("Settings");
		title.setFont(new Font("Roboto", Font.BOLD, 60));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setVerticalAlignment(JLabel.CENTER);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		settingsFrame.add(title, gbc);

		// Mute Label & Button
		JLabel muteLabel = new JLabel();
		muteLabel.setText("Mute");
		muteLabel.setFont(new Font("Roboto", Font.PLAIN, 32));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20,0,20,0);
		settingsFrame.add(muteLabel, gbc);
		JCheckBox muteCheckBox = new JCheckBox();
		muteCheckBox.setSelected(!musicIsPlaying);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		settingsFrame.add(muteCheckBox, gbc);
		muteCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				toggleMusic();
			}
		});

		// Main Menu Button
		JButton mainMenuButton = new JButton();
		mainMenuButton.setText("Main Menu");
		mainMenuButton.setFont(new Font("Roboto", Font.PLAIN, 32));
		mainMenuButton.setMargin(new Insets(10,10,10,10));
		mainMenuButton.setHorizontalAlignment(JLabel.CENTER);
		mainMenuButton.setVerticalAlignment(JLabel.CENTER);

		mainMenuButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buildStartMenuGUI(); // Return to main menu

				settingsFrame.dispose();
			}
		} );
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		settingsFrame.add(mainMenuButton,gbc);

		settingsFrame.setVisible(true);
	}
}