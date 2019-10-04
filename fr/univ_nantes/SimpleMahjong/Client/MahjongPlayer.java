package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.*;

import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.rmi.RemoteException;

public class MahjongPlayer {
	// private MahjongLobbyInterface lobby; // XXX useless to remember ?
	private MahjongRoundInterface server;
	private String pseudo;
	private int playerId;
	private ArrayList<AbstractTuile> hand = new ArrayList<AbstractTuile>();
	// TODO la rivière devrait être là pour pouvoir être affichée et mise à jour

	public MahjongPlayer (MahjongLobbyInterface lobby, MahjongRoundInterface server) {
		this.server = server;
		this.initPseudo();
		if (!this.tryConnection(lobby)) { return; }

		// le serveur devrait désigner les vents, et en particulier un vent d'est
		// en attendant on fait comme ça mdr XXX
		for (int i=0; i<13; i++) {
			this.piocheTuile();
		}
		System.out.println("hand = " + hand);
		Collections.sort(this.hand);

		while (true) { // celui là ok (on sortira par des exceptions)
			// TODO je propose des threads qui attendent le serveur sur le modèle immonde du lobby
			// TODO ou alors le délire des pointeurs longs mais ça je ne sais pas encore faire
			this.piocheTuile();
			Collections.sort(this.hand);
			this.printHand2();
			this.poseTuile(this.askIntInput());
		}
	}

	private boolean tryConnection(MahjongLobbyInterface lobby) {
		boolean accepted = false;
		try {
			System.out.println("En attente des autres joueurs…");
			accepted = lobby.registerPlayer(this.playerId, this.pseudo);
		} catch (Exception e){
			System.out.println("[erreur à l'enregistrement du client] " + e);
		}
		if (accepted) {
			System.out.println("Connecté : " + accepted);
		} else {
			System.out.println("Trop de joueurs sur le serveur");
		}
		return accepted;
	}

	private void initPseudo() {
		this.playerId = new Random().nextInt();
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Entrez votre pseudo :");
		try {
			this.pseudo = keyboard.nextLine();
			if (this.pseudo.length() < 1) {
				this.pseudo = "Joueur n°" + this.playerId;
			}
		} catch (Exception e) {
			this.pseudo = "Joueur n°" + this.playerId;
		}
		// System.out.println(this.pseudo + ' ' + this.playerId);
	}

	//----------------------------------------------------------------------------------------------

	private void printHand() {
		System.out.println("\nChoisissez une tuile à jeter : ");
		for(int i=0; i<this.hand.size(); i++) {
			System.out.println("[" + i + "] - " + this.hand.get(i).getEmoji());
		}
	}

	private void printHand2() {
		System.out.println("\nChoisissez une tuile à jeter : ");
		String out = "";
		for(int i=0; i<this.hand.size(); i++) {
			out += "[" + this.hand.get(i).getEmoji() + "]	";
		}
		System.out.println(out);
		out = "";
		for(int i=0; i<this.hand.size(); i++) {
			out += "(" + i + ")	";
		}
		System.out.println(out);
	}

	private int askIntInput() {
		Scanner keyboard = new Scanner(System.in);
		try {
			return keyboard.nextInt();
		} catch (Exception e) {
			return this.askIntInput(); // honteux XXX
		}
	}

	//----------------------------------------------------------------------------------------------

	/*
	 * Piocher une tuile DOIT faire appel au serveur, puisque c'est lui qui a la muraille.
	 */
	private void piocheTuile() {
		try {
			AbstractTuile t = this.server.pioche();
			this.hand.add(t);
			// System.out.println("pioche = " + t.toString());
		} catch (RemoteException e){
			System.out.println("[erreur client (pioche)] " + e);
		}
	}

	/*
	 * TODO pas besoin de faire appel au serveur pour ça normalement
	 */
	private void poseTuile(int index) {
		try {
			AbstractTuile t = this.hand.get(index);
			this.hand.remove(t);
			this.server.pose(t);
			// System.out.println("pose = " + t.toString());
		} catch (RemoteException e){
			System.out.println("[erreur client (pose)] " + e);
		}
	}

}

