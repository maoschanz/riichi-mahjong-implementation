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

public class MahjongPlayer extends UnicastRemoteObject implements MahjongPlayerInterface {
	private MahjongTableInterface server;
	private String pseudo;
	private String vent;
	private ArrayList<AbstractTuile> hand = new ArrayList<AbstractTuile>();
	private ArrayList<AbstractTuile> river = new ArrayList<AbstractTuile>();
	private ArrayList<AbstractTuile> combiShown = new ArrayList<AbstractTuile>();
	private MahjongPlayerInterface playerDroite; // shimocha
	private MahjongPlayerInterface playerFace; // toimen
	private MahjongPlayerInterface playerGauche; // kamicha
	private final static String START_COLO_TAG = "\033[30;106m";
	private final static String END_COLO_TAG = "\033[0m";
	private MahjongBackground bgThread;
	private boolean isPlaying = false;

	public MahjongPlayer(MahjongLobbyInterface lobby) throws RemoteException {
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Entrez votre pseudo :");
		try {
			this.pseudo = keyboard.nextLine();
			if (this.pseudo.length() < 1) {
				throw new Exception("Pseudo trop court");
			}
		} catch (Exception e) {
			this.pseudo = "Joueur n°" + new Random().nextInt();
		}

		this.bgThread = new MahjongBackground(this);
		this.bgThread.start();

		try {
			System.out.println("En attente des autres joueurs…");
			lobby.registerPlayer(this, this.pseudo);
		} catch (Exception e){
			System.out.println("[erreur à l'enregistrement du client] " + e);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Interactions with the server and the other players //////////////////////////////////////////

	// Initial interactions

	public void initHand(String vent) throws RemoteException {
		this.vent = vent;
		System.out.println("vent = " + vent);
		for (int i=0; i<13; i++) {
			this.piocheTuile();
		}
		// if (vent.equals("東")) { // XXX non parce qu'on commence playNormal par une pioche
		// 	this.piocheTuile();
		// }
		System.out.println("hand = " + this.hand);
		Collections.sort(this.hand);
	}

	public void initTable(MahjongTableInterface table) throws RemoteException {
		this.server = table;
	}

	public void discoverOther(MahjongPlayerInterface other) throws RemoteException {
		String ventCharacter = other.getVentChar();
		if (this.isSuivant(ventCharacter)) {
			this.playerDroite = other;
		} else if (this.isPrecedent(ventCharacter)) {
			this.playerGauche = other;
		} else {
			this.playerFace = other;
		}
	}

	// Gameplay-related interactions

	public void startGame(boolean isMe) throws RemoteException {
		this.isPlaying = isMe;
		if (isMe) { // if c'est mon tour
			this.playNormal();
			this.isPlaying = false;
			this.playerGauche.startGame(false);
			this.playerFace.startGame(false);
			this.playerDroite.startGame(true);
		} else { // if c'est pas mon tour
			this.updateUI(false, ""); // temporairement ?
			synchronized (this.bgThread) {
				this.bgThread.notify();
			}
			System.out.println("Fin du else, ligne 104");
		}
	}

	/*
	 * Piocher une tuile DOIT faire appel au serveur, puisque c'est lui qui a la muraille.
	 */
	private void piocheTuile() {
		try {
			AbstractTuile t = this.server.pioche();
			this.hand.add(t);
			// System.out.println("pioche = " + t.getName());
		} catch (RemoteException e){
			System.out.println("[erreur client (pioche)] " + e);
		}
	}

	public AbstractTuile volLastTuile(String s) throws RemoteException { // TODO
		System.out.println("annonce : " + s);
		switch(s) {
			case "chii": break; // suite TODO
			case "pon": break; // brelan TODO
			default: break; // case "kan" // carré TODO
		}
		// AbstractTuile temp = this.river.getLast();
		// this.river.removeLast();
		AbstractTuile temp = new TuileNombre('r', 3, 2);
		return temp;
	}

	/*
	 * Retirer une tuile de ma main et l'ajouter à ma rivière
	 */
	private void poseTuile(int index) {
		AbstractTuile t = this.hand.get(index);
		this.hand.remove(t);
		this.river.add(t);
	}

	// Other interactions

	public String getCombis() throws RemoteException {
		return this.combiShown.toString();
	}

	public String getRiviere() throws RemoteException {
		return this.river.toString();
	}

	public String getPseudo() throws RemoteException {
		return this.pseudo;
	}

	public String getVentChar() throws RemoteException {
		return this.vent;
	}

	public boolean isJoueurCourant() throws RemoteException {
		return this.isPlaying;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Gameplay loops //////////////////////////////////////////////////////////////////////////////

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
			this.poseTuile(tuile_index);
			this.updateUI(false, ""); // TODO en théorie plus tard il faut qu'on propose le vol
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

	public void playAnnonce() { // XXX "public"...
		// 13 tuiles → on vole → 14 donc → possibilité d'annoncer ron, pon, kan (ou chii) → si kan,
		// repiocher [dans le mur mort, et la dernière tuile du mur est ajoutée au mur mort ??? faut
		// qu'il reste à 14 tuiles, et on révèle un nouvel indicateur de dora] → joueur suivant
		int action_id = 0;
		int tuile_index = 0;
		String[] actions = {"pon", "kan", "chii", "ron"/*, "rien"*/};
		this.updateUI(false, "Que faire ?");
		action_id = this.askChoice(actions, false);
		System.out.println("action_id " + action_id);
		try {
			this.volLastTuile(actions[action_id]);
		} catch (Exception e) {
			System.out.println("erreur dans playAnnonce : " + e);
		}
		if (action_id == 1) {
			// repiocher ???? XXX
		}
		System.out.println("fin de playAnnonce");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Asking input and printing the UI ////////////////////////////////////////////////////////////

	private int askHandChoice() {
		this.updateUI(true, "Choisissez une tuile à jeter :");
		return this.askIntInput(this.hand.size());
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
		return this.askIntInput(values.length);
	}

	private int askIntInput(int inputMax) {
		int ret = inputMax;
		while (ret >= inputMax) {
			Scanner keyboard = new Scanner(System.in);
			try {
				ret = keyboard.nextInt();
			} catch (Exception e) {
				ret = inputMax;
			}
		}
		return ret;
	}

	private void updateUI(boolean withChoice, String prompt) {
		this.resetTerminal();
		this.printBoard();
		this.showChoices(this.getEmojiStrings(this.hand), true, withChoice);
		System.out.println("\n" + prompt);
	}

	private void resetTerminal() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	private void printMurMort() {
		// TODO pourrait être implémenté, mais honnêtement j'ai la flemme
		String mur = "▉▉▉▉▉▉▉";
		System.out.print("Indicateurs de dora : " + mur);
	}

	private void printJoueur(MahjongPlayerInterface j) {
		System.out.println("");
		try {
			String joueurStatus = "Joueur du vent " + j.getVentChar() + " (" + j.getPseudo() + ")";
			if (j.isJoueurCourant()) {
				joueurStatus += " " + START_COLO_TAG + "(en train de jouer)" + END_COLO_TAG;
			}
			if (j.equals(this)) {
				joueurStatus += " " + START_COLO_TAG + "(Vous)" + END_COLO_TAG;
			}
			System.out.println(joueurStatus);
			System.out.println("[Combinaisons annoncées] " + j.getCombis());
			System.out.println("[Tuiles défaussées] " + j.getRiviere());
		} catch (Exception e) {
			System.out.println("Joueur injoignable : " + e);
		}
	}

	private void printStatus() {
		System.out.print(START_COLO_TAG);
		// System.out.print("[Vous : " + this.pseudo + " - " + this.vent + "] ");
		// System.out.print("[Joueur courant : " + /*TODO +*/ "] ");
		this.printMurMort();
		System.out.println(END_COLO_TAG);
	}

	private void printBoard() {
		this.printStatus();
		this.printJoueur(this.playerDroite);
		this.printJoueur(this.playerFace);
		this.printJoueur(this.playerGauche);
		this.printJoueur(this);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Misc ////////////////////////////////////////////////////////////////////////////////////////

	private String[] getEmojiStrings(ArrayList<AbstractTuile> alist) {
		String[] handLabel = new String[alist.size()];
		for(int i=0; i < alist.size(); i++) {
			handLabel[i] = alist.get(i).toString();
		}
		return handLabel;
	}

	private boolean isSuivant(String autreVent) {
		switch (autreVent) {
			case "東":
				return this.vent.equals("南");
			case "南":
				return this.vent.equals("西");
			case "西":
				return this.vent.equals("北");
			case "北":
				return this.vent.equals("東");
		}
		return false;
	}

	private boolean isPrecedent(String autreVent) {
		switch (autreVent) {
			case "南":
				return this.vent.equals("東");
			case "西":
				return this.vent.equals("南");
			case "北":
				return this.vent.equals("西");
			case "東":
				return this.vent.equals("北");
		}
		return false;
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////

