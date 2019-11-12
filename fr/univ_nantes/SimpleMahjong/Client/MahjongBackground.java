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

	private void tryRunAnnonce() throws InterruptedException {
		synchronized (this) {
			System.out.println("Background thread pausing");
			wait();
		}
		System.out.println("Background thread running");
		this.player.playAnnonce();
	}

	public void run() {
		try {
			while(true) {
				this.tryRunAnnonce();

			}
		} catch (InterruptedException e) {
			System.out.println("Background thread stopped with an error: " + e);
		}
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////

