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
	private int[] riversLength = new int[]{2, 2, 2, 2}; // car on traite la rivière comme une chaîne
	private MahjongPlayerInterface lastPlayer;

	private MahjongPlayerInterface playerDroite; // shimocha
	private MahjongPlayerInterface playerFace; // toimen
	private MahjongPlayerInterface playerGauche; // kamicha

	private String pseudo;
	private String vent;
	private boolean isPlaying = false;
	private ArrayList<AbstractTuile> hand = new ArrayList<AbstractTuile>();
	private ArrayList<AbstractTuile> river = new ArrayList<AbstractTuile>();
	private ArrayList<AbstractTuile> combiShown = new ArrayList<AbstractTuile>();

	private final static String START_COLOR = "\033[30;106m";
	private final static String END_COLOR = "\033[0m";
	private MahjongBackground bgThread;

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
		this.continueGame(isMe);
	}

	public void continueGame(boolean isMe) throws RemoteException {
		// System.out.println("[début du continueGame, ligne 90] " + isMe);
		this.isPlaying = isMe;
		this.updateRiversLength();
		// System.out.println("[fin du continueGame, ligne 108]");
		if (isMe) {
			this.updateUI(false, "Appuyez sur entrée");
			// XXX on pourrait peut-être directement actualiser l'interface pour montrer les choix ?
		} else {
			this.updateUI(false, "Tapez une annonce si besoin (pon/kan/ron)");
		}
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
			this.updatePlayers();
		} catch (RemoteException e) {
			System.out.println("[erreur dans le cycle de jeu du plus haut niveau] " + e);
		}
	}

	private void updatePlayers() throws RemoteException {
		this.isPlaying = false; // XXX pas très fiable je trouve
		this.playerDroite.continueGame(true);
		this.playerGauche.continueGame(false);
		this.playerFace.continueGame(false);
		this.continueGame(false);
	}

	private void playCycleNormal(String input) {
		// 13 tuiles → on pioche → 14 donc → possibilité d'annoncer tsumo (ou kan ?), ou défausse
		// (ou défausse avec riichi) (→ si kan, repiocher) → joueur suivant
		int action_id = 0;
		if (this.lastPlayer != null) {
			String[] actions = {
				"Piocher",
				"Annoncer chii (utilisation de la tuile de l'adversaire pour compléter une suite de 3 tuiles)",
				"Annoncer pon (utilisation de la tuile de l'adversaire pour compléter un brelan)",
				"Annoncer kan (utilisation de la tuile de l'adversaire pour compléter un carré)",
				"Annoncer ron (utilisation de la tuile de l'adversaire pour compléter une main)"
			};
			this.updateUI(false, "Que faire ?" + input);
			action_id = this.askActionChoice(actions, false);
		} // else on ne peut que piocher donc on ne s'embarrasse pas de la 1ère question

		if (action_id == 0) {
			this.playPioche();
		} else if (action_id == 1) {
			this.volLastTuile("chii");
		} else if (action_id == 2) {
			this.volLastTuile("pon");
		} else if (action_id == 3) {
			this.volLastTuile("kan");
			this.piocheTuile();
			Collections.sort(this.hand);
			// TODO update l'interface pour montrer la rivière modifiée
			// this.showHorizontalChoices(this.getEmojiStrings(this.hand), false); // vraiment ? XXX
		} else if (action_id == 4) {
			this.volLastTuile("ron");
			// TODO ...et ? victoire
		}
	}

	private void playPioche() {
		int action_id = 0;
		this.piocheTuile();
		String[] actions = {
			// "Se défausser d'une tuile", = choix 0 à 13 ; les actions suivantes étant 14 et 15
			"Annoncer tsumo (main complétée par la pioche)",
			"Annoncer kan (complétion d'un carré, il faudra repiocher)"
		};
		int hsize1 = this.hand.size() - 1;
		String str1 = String.format("Vous avez pioché [%s] ; que faire ?", this.hand.get(hsize1));
		String str2 = String.format(" (0 à %d = se défausser de la tuile)", hsize1);
		Collections.sort(this.hand);
		this.updateUI(true, str1 + str2);
		action_id = this.askActionChoice(actions, true);
		if (action_id <= hsize1) {
			this.poseTuile(action_id);
		} else if (action_id == hsize1 + 1) {
			// fin théorique de la partie (à faire vérifier TODO)
		} else if (action_id == hsize1 + 2) {
			this.volLastTuile("kan");
			this.piocheTuile();
			Collections.sort(this.hand);
			// TODO update l'interface pour montrer la rivière modifiée ?
		}
	}

	private void playAnnonce(String input) throws RemoteException {
		// 13 tuiles → on vole → 14 donc → possibilité d'annoncer ron, pon, kan (ou chii) → si kan,
		// repiocher [dans le mur mort, et la dernière tuile du mur est ajoutée au mur mort ??? faut
		// qu'il reste à 14 tuiles, et on révèle un nouvel indicateur de dora] → joueur suivant
		int action_id = 0;
		String[] actions = {"invalid", "pon", "kan", "ron"};
		for(int i=1; i<4; i++) {
			if (input.equals(actions[i])) {
				action_id = i;
			}
		}
		if (action_id == 0) {
			return; // ignorer les annonces invalides
		}
		this.isPlaying = true;
		this.playerDroite.continueGame(false);
		this.playerGauche.continueGame(false);
		this.playerFace.continueGame(false);
		// this.continueGame(false);
		this.updateUI(false, "Tapez une annonce si besoin (pon/kan/ron)");

		// System.out.println("action_id " + action_id);
		if (action_id > 0) {
			this.volLastTuile(actions[action_id]);
		// } else {
		// 	System.out.println("pas d'annonce lancée");
		}
		if (action_id == 2) {
			// cas du kan
			this.piocheTuile();
			Collections.sort(this.hand);
		}
		// System.out.println("fin de playAnnonce");
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
		// System.out.println("annonce : " + s);
		ArrayList<AbstractTuile> removed = new ArrayList<AbstractTuile>();
		try {
			AbstractTuile temp = this.lastPlayer.getLastTuile();
			switch(s) {
				case "ron": // victoire par vol
					// TODO
					break;
				case "chii": // suite
					removed = this.getTuilesPourSuite(temp.getSortId());
					break;
				case "pon": // brelan
					removed = this.getTuilesWithId(temp.getSortId(), 2);
					break;
				case "kan": // carré
					removed = this.getTuilesWithId(temp.getSortId(), 3);
					break;
				default:
					return; // annonce invalide
			}
			this.lastPlayer.removeLastTuile();
			this.combiShown.add(temp);
			for(int i=0; i < removed.size(); i++) {
				this.hand.remove(removed.get(i));
				this.combiShown.add(removed.get(i));
			}
		} catch (Exception e) {
			// soit RemoteException soit NullPointerException soit Exception
			System.out.println("Erreur lors du vol d'une tuile : " + e);
		}
	}

	private ArrayList<AbstractTuile> getTuilesPourSuite(int stolenId) throws Exception {
		ArrayList<AbstractTuile> removed = new ArrayList<AbstractTuile>();
		boolean stolenIsLower = false;
		try {
			stolenIsLower = (idExistsInHand(stolenId + 1) && idExistsInHand(stolenId + 2));
		} catch (Exception e) {}
		boolean stolenIsMiddle = false;
		try {
			stolenIsMiddle = (idExistsInHand(stolenId + 1) && idExistsInHand(stolenId - 1));
		} catch (Exception e) {}
		boolean stolenIsUpper = false;
		try {
			stolenIsUpper = (idExistsInHand(stolenId - 1) && idExistsInHand(stolenId - 2));
		} catch (Exception e) {
			// On n'affiche pas d'erreur ici car on s'attend totalement à ce que des exceptions
			// soient levées ici lors d'une exécution normale.
		}

		if (stolenIsLower && !stolenIsMiddle && !stolenIsUpper) {
			removed = this.getTuilesWithId(stolenId + 1, 1);
			removed.addAll(this.getTuilesWithId(stolenId + 2, 1));
		} else if (!stolenIsLower && stolenIsMiddle && !stolenIsUpper) {
			removed = this.getTuilesWithId(stolenId + 1, 1);
			removed.addAll(this.getTuilesWithId(stolenId - 1, 1));
		} else if (!stolenIsLower && !stolenIsMiddle && stolenIsUpper) {
			removed = this.getTuilesWithId(stolenId - 1, 1);
			removed.addAll(this.getTuilesWithId(stolenId - 2, 1));
		} else {
			// TODO le problème est compliqué parce qu'on aura souvent plusieurs possibilités de
			// suites, genre 3456 peut être 345+6 ou 3+456. Il faudrait donc demander au joueur de
			// désigner quelle suite il veut faire parmi les possibilités valides.
			throw new Exception("getTuilesPourSuite : interaction à demander");
		}
		return removed;
	}

	private boolean idExistsInHand(int tuileId) throws Exception {
		return (this.getTuilesWithId(tuileId, 1).size() > 0);
	}

	private ArrayList<AbstractTuile> getTuilesWithId(int sortId, int number) throws Exception {
		ArrayList<AbstractTuile> removed = new ArrayList<AbstractTuile>();
		for(int i=0; i < this.hand.size(); i++) {
			if (removed.size() < number && this.hand.get(i).getSortId() == sortId) {
				removed.add(this.hand.get(i));
			}
		}
		if (removed.size() != number) {
			throw new Exception("Combinaison invalide");
		}
		return removed;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Asking input and printing the UI ////////////////////////////////////////////////////////////

	private void showHorizontalChoices(String[] values, boolean showNum) {
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
	}

	private int askActionChoice(String[] values, boolean withHand) {
		int minInput = 0;
		if (withHand) {
			minInput += this.hand.size();
		}
		for(int i=0; i < values.length; i++) {
			System.out.println("(" + (i + minInput) + ") - " + values[i]);
		}
		return this.askIntInput(minInput + values.length);
	}

	private int askIntInput(int inputMax) {
		int ret = inputMax;
		while (ret >= inputMax || ret < 0) {
			// XXX le scanner bloque le thread du client, mais bon perso je trouve ça souhaitable à
			// ce stade de la boucle de gameplay
			Scanner keyboard = new Scanner(System.in);
			try {
				ret = keyboard.nextInt();
			} catch (Exception e) {
				ret = inputMax;
			}
		}
		return ret;
	}

	// XXX virer le withChoice plus tard ?
	private void updateUI(boolean withChoice, String prompt) {
		this.resetTerminal();
		this.printBoard();
		this.showHorizontalChoices(this.getEmojiStrings(this.hand), withChoice);
		System.out.println("\n" + prompt);
	}

	private void resetTerminal() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	// private void printMurMort() {
		// XXX pourrait être implémenté, mais honnêtement ya plus prioritaire
	// 	String mur = "▉▉▉▉▉▉▉";
	// 	System.out.print("Indicateurs de dora : " + mur);
	// }

	private void printJoueur(MahjongPlayerInterface j) {
		System.out.println("");
		try {
			String joueurStatus = "Joueur du vent " + j.getVentChar() + " (" + j.getPseudo() + ")";
			if (j.isJoueurCourant()) {
				joueurStatus += " " + START_COLOR + "(en train de jouer)" + END_COLOR;
			}
			if (j.equals(this)) {
				joueurStatus += " " + START_COLOR + "(Vous)" + END_COLOR;
			}
			System.out.println(joueurStatus);
			System.out.println("[Combinaisons annoncées] " + j.getCombis());
			if (j.equals(this.lastPlayer)) {
				// FIXME bonne idée mais en pratique pas au point : il n'y a pas toujours des tuiles à
				// voler, même quand la rivière n'est pas vide.
				System.out.println("[Tuiles défaussées] " + START_COLOR + j.getRiviere() + END_COLOR);
			} else {
				System.out.println("[Tuiles défaussées] " + j.getRiviere());
			}
		} catch (Exception e) {
			System.out.println("Joueur injoignable : " + e);
		}
	}

	private void printStatus() {
		System.out.print(START_COLOR);
		System.out.print(" Mahjong ");
		// this.printMurMort();
		System.out.println(END_COLOR);
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
		// FIXME pas fiable du tout, si une annonce foireuse survient ça sera null notamment
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
			// probablement d'autes choses à faire ici
		}
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////

