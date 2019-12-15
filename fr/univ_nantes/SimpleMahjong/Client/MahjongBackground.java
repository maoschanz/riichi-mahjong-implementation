package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.*;

import java.util.Scanner;

////////////////////////////////////////////////////////////////////////////////////////////////////

public class MahjongBackground extends Thread {
	private MahjongPlayer player;

	public MahjongBackground(MahjongPlayer player) {
		super();
		this.player = player;
	}

	/*
	 * Attends la dernière ligne tapée par l'utilisateur (`this.askRawInput()`) et la transmet à
	 * l'objet `this.player`, où elle sera traitée différemment en fonction de l'état courant du
	 * joueur.
	 */
	private void tryRun1Cycle() throws InterruptedException {
		// System.out.println("Background thread running");
		String input = this.askRawInput();
		this.player.mainCycle(input);
	}

	/* Retourne la dernière ligne tapée par l'utilisateur */
	private String askRawInput() {
		String ret = "";
		Scanner keyboard = new Scanner(System.in);
		try {
			ret = keyboard.nextLine();
		} catch (Exception e) {
			ret = "";
		}
		return ret;
	}

	public void run() {
		try {
			while(true) {
				this.tryRun1Cycle();
			}
		} catch (InterruptedException e) {
			System.out.println("Background thread stopped with an error: " + e);
		}
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////

