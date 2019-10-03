package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.*;

import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.rmi.RemoteException;

public class MahjongPlayer {
	// private MahjongLobbyInterface lobby; // XXX useless to remember ?
	private MahjongRoundInterface server;
	private String pseudo;
	private int playerId;
	private ArrayList<MahjongTuile> hand = new ArrayList<MahjongTuile>();
	// TODO la rivière devrait être là pour pouvoir être affichée et mise à jour

	public MahjongPlayer (MahjongLobbyInterface lobby, MahjongRoundInterface server) {
		this.server = server;
		this.initPseudo();
		boolean accepted = false;
		try {
			accepted = lobby.registerPlayer(this.playerId, this.pseudo);
			System.out.println("Connecté : " + accepted);
		} catch (Exception e){
			System.out.println("[erreur à l'enregistrement du client] " + e);
		}
		if (!accepted) {
			System.out.println("Trop de joueurs sur le serveur");
			return;
		}

		// donc là dans l'idéal il faudrait ATTENDRE QUE LE SERVEUR AIENT 4 JOUEURS !!!
		// mdr, attendre
		// et au terme de cette attente, le serveur nous donne une main, et désigne un vent d'est
		// en attendant on fait comme ça mdr XXX
		for (int i=0; i<13; i++) {
			this.piocheTuile();
		}
		System.out.println("hand = " + hand);

		while(true){ // celui là ok (on sortira par des exceptions)
			// TODO
			// while(true){} // celui là non mdr, là on attend notre tour plutôt
			// XXX pas tout de suite, le client peut vouloir annoncer plutôt
			this.piocheTuile();
			this.poseTuile(this.askIntInput());
		}
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

	private int askIntInput() {
		Scanner keyboard = new Scanner(System.in);
		System.out.println("\nChoisissez une tuile à jeter");
		for(int i=0; i<this.hand.size(); i++) {
			System.out.println("[" + i + "] - " + this.hand.get(i));
		}
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
			MahjongTuile t = this.server.pioche();
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
			MahjongTuile t = this.hand.get(index);
			this.hand.remove(t);
			this.server.pose(t);
			// System.out.println("pose = " + t.toString());
		} catch (RemoteException e){
			System.out.println("[erreur client (pose)] " + e);
		}
	}

}

