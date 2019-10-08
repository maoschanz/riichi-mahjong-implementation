package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.*;

import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.rmi.RemoteException;

////////////////////////////////////////////////////////////////////////////////////////////////////

public class MahjongPlayer {
	// private MahjongLobbyInterface lobby; // XXX useless to remember ?
	private MahjongRoundInterface server;
	private String pseudo;
	private String vent;
	private int playerId;
	private ArrayList<AbstractTuile> hand = new ArrayList<AbstractTuile>();
	// TODO la rivière devrait être là pour pouvoir être affichée et mise à jour
	private ArrayList<ArrayList<AbstractTuile>> river = new ArrayList<ArrayList<AbstractTuile>>();

	public MahjongPlayer (MahjongLobbyInterface lobby, MahjongRoundInterface server) {
		this.server = server;
		this.initPseudo();
		this.initRiver();
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
			if (true) { // if c'est mon tour
				this.playNormal();
			} else { // if c'est pas mon tour TODO
				this.playAnnonce();
			}
			// TODO joueur suivant
		}
	}

	//----------------------------------------------------------------------------------------------

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
			this.vent = "this.vent"; // TODO
		} else {
			System.out.println("Trop de joueurs sur le serveur");
		}
		return accepted;
	}

	private void initPseudo() {
		this.playerId = new Random().nextInt(); // TODO il y a des vrais UID dans java.rmi
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

	private void initRiver() {
		this.river.add(new ArrayList<AbstractTuile>());
		this.river.add(new ArrayList<AbstractTuile>());
		this.river.add(new ArrayList<AbstractTuile>());
		this.river.add(new ArrayList<AbstractTuile>());
	}

	//----------------------------------------------------------------------------------------------

	private void playNormal() {
		// 13 tuiles → on pioche → 14 donc → possibilité d'annoncer tsumo (ou kan ?), ou défausse
		// (ou défausse avec riichi) (→ si kan, repiocher) → joueur suivant
		int action_id = 0;
		int tuile_index = 0;
		this.piocheTuile();
		Collections.sort(this.hand);
		// XXX kan ?
		String[] actions = {"défausse", "tsumo", "kan"};
		this.updateUI(false, "Que faire ?");
		action_id = this.askChoice(actions, false);

		if (action_id == 0) {
			tuile_index = this.askHandChoice();
			System.out.println("tuile_index : " + tuile_index);
			this.poseTuile(tuile_index);
		} else if (action_id == 1) {
			// fin théorique de la partie (à faire vérifier par le serveur ?)
		} else if (action_id == 2) {
			// XXX si on peut kan est-ce qu'on peut faire autre chose ?
			// TODO envoyer le kan au serveur (ou aux pairs)
			// puis piocher ???? XXX
			this.piocheTuile();
			Collections.sort(this.hand);
			this.showChoices(this.getEmojiStrings(this.hand), true, false);
		}
	}

	private void playAnnonce() {
		// 13 tuiles → on vole → 14 donc → possibilité d'annoncer ron, pon, kan (ou chii) → si kan,
		// repiocher [dans le mur mort, et la dernière tuile du mur est ajoutée au mur mort ??? faut
		// qu'il reste à 14 tuiles, et on révèle un nouvel indicateur de dora] → joueur suivant
		int action_id = 0;
		int tuile_index = 0;
		String[] actions = {"pon", "kan", "chii", "ron"/*, "rien"*/};
		this.updateUI(false, "Que faire ?");
		action_id = this.askChoice(actions, false);
		this.voleTuile( actions[action_id] );
		if (action_id == 1) {
			// repiocher ???? XXX
		}
	}

	//----------------------------------------------------------------------------------------------

	private void resetTerminal() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	private void printMurMort() {
		String mur = "▉▉▉▉▉▉▉"; // TODO
		System.out.println("Indicateurs de dora : " + mur);
	}

	private void printBoard() {
		// TODO TODO TODO TODO TODO
		String annonces_opp = "[pon/kan/chii du vent opposé]";
		System.out.println(annonces_opp + "[symbole du vent opposé]");
		String[] river_opp = this.getEmojiStrings(this.river.get(3));
		System.out.println(river_opp + "\n");

		String annonces_drt = "[pon/kan/chii du vent droite]";
		System.out.println(annonces_drt + "[symbole du vent droite]");
		String[] river_drt = this.getEmojiStrings(this.river.get(2));
		System.out.println(river_drt + "\n");

		String annonces_gch = "[pon/kan/chii du vent gauche]";
		System.out.println(annonces_gch + "[symbole du vent gauche]");
		String[] river_gch = this.getEmojiStrings(this.river.get(1));
		System.out.println(river_gch + "\n");
		//String.substring(index1, index2) pour afficher ça mieux ? ou faire tuile par tuile

		String annonces_self = "[pon/kan/chii de nous]";
		System.out.println(annonces_self + " " + this.vent);
		String[] river_self = this.getEmojiStrings(this.river.get(0));
		System.out.println(river_self + "\n");
	}

	private int askHandChoice() {
		this.updateUI(true, "Choisissez une tuile à jeter :");
		return this.askIntInput();
	}

	private void showChoices(String[] values, boolean horizontal, boolean showNum) {
		if (horizontal) {
			String l1 = "";
			String l2 = "";
			for(int i=0; i < values.length; i++) {
				l1 += "[" + values[i] + "]	";
				l2 += "(" + i + ")	";
			}
			System.out.println(l1);
			if (showNum) {
				System.out.println(l2);
			}
		} else if (showNum) {
			for(int i=0; i < values.length; i++) {
				System.out.println("(" + i + ") - " + values[i]);
			}
		} else {
			for(int i=0; i < values.length; i++) {
				System.out.println(values[i]);
			}
		}
	}

	private int askChoice(String[] values, boolean horizontal) {
		this.showChoices(values, horizontal, true);
		return this.askIntInput();
	}

	private String[] getEmojiStrings(ArrayList<AbstractTuile> alist) {
		String[] handLabel = new String[alist.size()];
		for(int i=0; i < alist.size(); i++) {
			handLabel[i] = alist.get(i).getEmoji();
		}
		return handLabel;
	}

	private int askIntInput() {
		Scanner keyboard = new Scanner(System.in);
		try {
			return keyboard.nextInt();
		} catch (Exception e) {
			return this.askIntInput(); // honteux XXX
		}
	}

	private void updateUI(boolean withChoice, String prompt) {
		this.resetTerminal();
		System.out.println("[" + this.pseudo + " - " + this.vent + "]");
		System.out.println("[Joueur courant : ?????]");
		this.printMurMort();
		this.printBoard();
		this.showChoices(this.getEmojiStrings(this.hand), true, withChoice);
		System.out.println("\n" + prompt);
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
			System.out.println("pose = " + t.toString());
		} catch (RemoteException e){
			System.out.println("[erreur client (pose)] " + e);
		}
	}

	/*
	 * TODO pas besoin de faire appel au serveur pour ça normalement
	 */
	private void voleTuile(String annonce) {
		try {
			AbstractTuile t = this.server.annonceEtVol(annonce);
			System.out.println("vol = " + t.toString());
		} catch (RemoteException e){
			System.out.println("[erreur client (vol, 1)] " + e);
		} catch (Exception e){
			System.out.println("[erreur client (vol, 2)] " + e);
		}
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////

