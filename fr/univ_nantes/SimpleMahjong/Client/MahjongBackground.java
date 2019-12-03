package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.*;
import fr.univ_nantes.SimpleMahjong.Tuile.*;

import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

////////////////////////////////////////////////////////////////////////////////////////////////////

public class MahjongBackground extends Thread {
	private MahjongPlayer player;

	public MahjongBackground(MahjongPlayer player) {
		super();
		this.player = player;
	}

	/*  */
	private void tryRun1Cycle() throws InterruptedException {
		// System.out.println("Background thread running");
		String input = this.askRawInput();
		this.player.mainCycle(input);
	}

	/*  */
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

