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
	private int[] riversLength = new int[]{0,0,0,0};
	private MahjongPlayerInterface lastPlayer;
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
		System.out.println("[fin du startGame, ligne 108]");
		this.updateUI(false, "************"); // XXX
	}

	public void continueGame(boolean isMe) throws RemoteException {
		System.out.println("[début du continueGame, ligne 90] " + isMe);
		this.isPlaying = isMe;
		this.updateRiversLength(); // XXX chelou que ce soit là
		System.out.println("[fin du continueGame, ligne 108]");
		this.updateUI(false, "////////"); // XXX
	}

	/*
	 * Retirer une tuile de ma main et l'ajouter à ma rivière
	 */
	private void poseTuile(int index) {
		AbstractTuile t = this.hand.get(index);
		this.hand.remove(t);
		this.river.add(t);
	}

	/*
	 * Retourne la dernière tuile à avoir été posé par moi-même dans la rivière
	 */
	public AbstractTuile getLastTuile() throws RemoteException {
		AbstractTuile temp = this.river.get(this.river.size() - 1);
		return temp;
	}

	/*
	 * Supprime la dernière tuile à avoir été posé par moi-même dans la rivière
	 */
	public void removeLastTuile() throws RemoteException {
		this.river.remove(this.river.size() - 1);
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

	public void mainCycle(String input) {
		try {
			if (this.isJoueurCourant()) {
				this.updateUI(false, "Que voulez-vous faire ?");
				this.playCycleNormal(input);
			} else {
				this.updateUI(false, "Tapez une annonce si besoin (pon/kan/ron)");
				this.playAnnonce(input);
			}
		} catch (RemoteException e) {
			System.out.println("[erreur dans le cycle de jeu du plus haut niveau] " + e);
		}
	}

	private void playCycleNormal(String input) throws RemoteException {
		this.faireActionJeu(input);
		this.isPlaying = false; // XXX pas fiable, on devrait se reposer sur les vents je pense
		this.playerGauche.continueGame(false);
		this.playerFace.continueGame(false);
		this.updateUI(false, "[annonces invalides pour le moment (attente du joueur suivant)]");
		this.playerDroite.continueGame(true);
	}

	private void faireActionJeu(String input) {
		// 13 tuiles → on pioche → 14 donc → possibilité d'annoncer tsumo (ou kan ?), ou défausse
		// (ou défausse avec riichi) (→ si kan, repiocher) → joueur suivant
		int action_id = 0;
		// XXX kan ?
		String[] actions = {
			"Piocher",
			"Annoncer chii (utilisation de la tuile de l'adversaire pour compléter une suite de 3 tuiles)",
			"Annoncer pon (utilisation de la tuile de l'adversaire pour compléter un brelan)",
			"Annoncer kan (utilisation de la tuile de l'adversaire pour compléter un carré)",
			"Annoncer ron (utilisation de la tuile de l'adversaire pour compléter une main)"
		};
		this.updateUI(false, "Que faire ?" + input);
		action_id = this.askActionChoice(actions, false);

		if (action_id == 0) {
			this.playPioche();
		} else if (action_id == 1) {
			this.volLastTuile("chii");
		} else if (action_id == 2) {
			this.volLastTuile("pon");
		} else if (action_id == 3) {
			this.volLastTuile("kan");
			// XXX si on peut kan est-ce qu'on peut faire autre chose ?
			this.piocheTuile();
			Collections.sort(this.hand);
			// TODO update l'interface pour montrer la rivière modifiée
			// this.showChoices(this.getEmojiStrings(this.hand), true, false); // vraiment ? FIXME à vérifier
		} else if (action_id == 4) {
			this.volLastTuile("ron");
			// TODO ...et ? victoire
		}
	}

	private void playPioche() {
		int action_id = 0;
		int tuile_index = 0;
		this.piocheTuile();
		Collections.sort(this.hand);
		String[] actions = {
			"Se défausser d'une tuile", // XXX l'UX est améliorable en proposant directement les
			// numéros pour défausser et en dessous les actions tsumo et kan avec les numéros suivants
			// Ça simplifierait d'ailleurs le code
			"Annoncer tsumo (main complétée par la pioche)",
			"Annoncer kan (complétion d'un carré, il faudra repiocher)"
		};
		this.updateUI(false, "Que faire ?");
		action_id = this.askActionChoice(actions, false);
		if (action_id == 0) {
			tuile_index = this.askHandChoice();
			this.poseTuile(tuile_index);
		} else if (action_id == 1) {
			// fin théorique de la partie (à faire vérifier par le serveur ?)
		} else if (action_id == 2) {
			this.volLastTuile("kan");
			// XXX si on peut kan est-ce qu'on peut faire autre chose ?
			this.piocheTuile();
			Collections.sort(this.hand);
			// TODO update l'interface pour montrer la rivière modifiée
			// this.showChoices(this.getEmojiStrings(this.hand), true, false); // vraiment ? FIXME à vérifier
		}
	}

	private void playAnnonce(String input) {
		// 13 tuiles → on vole → 14 donc → possibilité d'annoncer ron, pon, kan (ou chii) → si kan,
		// repiocher [dans le mur mort, et la dernière tuile du mur est ajoutée au mur mort ??? faut
		// qu'il reste à 14 tuiles, et on révèle un nouvel indicateur de dora] → joueur suivant
		int action_id = 0;
		int tuile_index = 0;
		String[] actions = {"invalid", "pon", "kan", "ron"};
		this.updateUI(false, "Tapez une annonce si besoin (pon/kan/ron)");

		for(int i=1; i<4; i++) {
			if (input.equals(actions[i])) {
				action_id = i;
			}
		}
		System.out.println("action_id " + action_id);

		if (action_id > 0) {
			this.volLastTuile(actions[action_id]);
		} else {
			System.out.println("pas d'annonce lancée");
		}
		if (action_id == 2) {
			// cas du kan
			this.piocheTuile();
			Collections.sort(this.hand);
		}
		System.out.println("fin de playAnnonce");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Piocher une tuile DOIT faire appel au serveur, puisque c'est lui qui a la muraille.
	 */
	private void piocheTuile() {
		try {
			AbstractTuile t = this.server.pioche();
			this.hand.add(t);
		} catch (RemoteException e){
			System.out.println("[erreur client (pioche)] " + e);
		}
	}

	private void volLastTuile(String s) {
		System.out.println("annonce : " + s);
		try {
			AbstractTuile temp = this.lastPlayer.getLastTuile();
			switch(s) {
				case "ron":
					// victoire TODO
					break;
				case "chii":
					// suite TODO
					break;
				case "pon":
					// brelan TODO
					break;
				case "kan":
					// carré TODO
					// this.hand.remove(???);
					// this.combiShown.add(???);
					break;
				default:
					return; // annonce invalide
			}
			this.lastPlayer.removeLastTuile();
			this.hand.add(temp);
		} catch (Exception e) {
			// soit RemoteException soit NullPointerException
		}
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

	private int askActionChoice(String[] values, boolean horizontal) {
		this.showChoices(values, horizontal, true);
		return this.askIntInput(values.length);
	}

	private int askIntInput(int inputMax) {
		int ret = inputMax;
		while (ret >= inputMax) {
			Scanner keyboard = new Scanner(System.in); // XXX interrompt le gameflow
			try {
				ret = keyboard.nextInt();
			} catch (Exception e) {
				ret = inputMax;
			}
		}
		return ret;
	}

	// XXX TODO virer le withChoice plus tard
	private void updateUI(boolean withChoice, String prompt) {
		this.resetTerminal();
		this.printBoard();
		this.showChoices(this.getEmojiStrings(this.hand), true, withChoice); // cesser les clowenries
		// avec withChoice et faire une méthode dédiée à l'affichage horizontal, ce qui limitera
		// par ailleurs les nombre de boucles d'input TODO
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
		System.out.print(" Mahjong ");
		// System.out.print("[Vous : " + this.pseudo + " - " + this.vent + "] ");
		// System.out.print("[Joueur courant : " + /*TODO +*/ "] ");
		// this.printMurMort();
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

	private void updateRiversLength() {
		this.lastPlayer = null;
		try {
			this.updateRiverForPlayer(this, 0);
			this.updateRiverForPlayer(this.playerDroite, 1);
			this.updateRiverForPlayer(this.playerFace, 2);
			this.updateRiverForPlayer(this.playerGauche, 3);
		} catch (RemoteException e) {
			System.out.println("Impossible de trouver les tuiles jouées par un joueur : " + e);
		}
	}

	private void updateRiverForPlayer(MahjongPlayerInterface j, int index) throws RemoteException {
		int lastValueR = this.riversLength[index];
		this.riversLength[index] = j.getRiviere().length(); // ce sont des tailles de chaînes
		if (lastValueR != this.riversLength[index]) {
			this.lastPlayer = j;
			// TODO probablement d'autes choses à faire ici
		}
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////

