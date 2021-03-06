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
	private boolean canBeInterrupted = true;
	private ArrayList<AbstractTuile> hand = new ArrayList<AbstractTuile>();
	private ArrayList<AbstractTuile> river = new ArrayList<AbstractTuile>();
	private ArrayList<AbstractTuile> combiShown = new ArrayList<AbstractTuile>();

	private final static String START_COLOR = "\033[30;106m";
	private final static String END_COLOR = "\033[0m";
	private MahjongBackground bgThread;

	public MahjongPlayer(MahjongLobbyInterface lobby) throws RemoteException {
		this.printStatus();
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
		this.isPlaying = isMe;
		this.updateRiversLength();
		if (isMe) {
			this.updateUI(false, "Appuyez sur entrée");
			// ça aurait été bien de pouvoir directement actualiser l'interface pour montrer les choix.
			// mais flemme, pas utile
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
	 * Retourne la dernière tuile à avoir été posée par moi-même dans la rivière
	 */
	public AbstractTuile getLastTuile() throws RemoteException {
		AbstractTuile temp = this.river.get(this.river.size() - 1);
		return temp;
	}

	/*
	 * Supprime la dernière tuile à avoir été posée par moi-même dans la rivière
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

	public boolean getCanBeInterrupted() throws RemoteException {
		return this.canBeInterrupted;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Gameplay loops //////////////////////////////////////////////////////////////////////////////

	public void mainCycle(String input) {
		this.canBeInterrupted = true;
		try {
			boolean success = false;
			if (this.isJoueurCourant()) {
				this.updateUI(false, "Que voulez-vous faire ?");
				success = this.playCycleNormal(input);
			} else {
				this.updateUI(false, "Tapez une annonce si besoin (pon/kan/ron)");
				success = this.playAnnonce(input);
			}
			if (success) {
				this.updatePlayers();
			}
		} catch (RemoteException e) {
			System.out.println("[erreur dans mainCycle] " + e);
		}
		this.canBeInterrupted = true;
	}

	/*
	 * Signale aux autres joueurs que le joueur courant est désormais celui à notre droite (en tout
	 * cas symboliquement parlant, en termes de vents)
	 */
	private void updatePlayers() throws RemoteException {
		this.isPlaying = false;
		this.playerDroite.continueGame(true);
		this.playerGauche.continueGame(false);
		this.playerFace.continueGame(false);
		this.continueGame(false);
	}

	private boolean playCycleNormal(String input) {
		// l'input n'est pas transmis plus bas, c'est moche mais c'est pas un bug (cet input n'a pas
		// de sens, il pourrait en avoir si je revoyais le fonctionnement de certains inputs)
		int action_id = 0;
		boolean success = false;
		boolean canUseLastTuile = (this.lastPlayer != null); // XXX un peu bancal selon moi
		// TODO il faudrait un attribut booléen dédié à savoir ça ^

		if (canUseLastTuile) {
			String[] actions = {
				"Piocher",
				"Annoncer chii (utilisation de la tuile de l'adversaire pour compléter une suite de 3 tuiles)",
				"Annoncer pon (utilisation de la tuile de l'adversaire pour compléter un brelan)",
				"Annoncer kan (utilisation de la tuile de l'adversaire pour compléter un carré)",
				"Annoncer ron (utilisation de la tuile de l'adversaire pour compléter une main)"
			};
			this.updateUI(false, "Que faire ?");
			action_id = this.askActionChoice(actions, false);
		} // else on ne peut que piocher donc on ne s'embarrasse pas de la 1ère question


		synchronized(this) {
			if (!this.canBeInterrupted) {
				// le booléen est vrai au début du cycle, mais change à la moindre annonce.
				System.out.println("Une annonce a été lancée !");
				return false;
			} else { // En l'absence d'annonce, le joueur mets lui-même son attribut booléen à faux, et
				// l'adversaire qui voudrait lancer une annonce après ça sera envoyé boulé.
				this.canBeInterrupted = false;
			}
		}

		if (action_id == 0) {
			success = this.playPioche();
		} else if (action_id == 1) {
			success = this.volLastTuile("chii");
		} else if (action_id == 2) {
			success = this.volLastTuile("pon");
		} else if (action_id == 3) {
			success = this.volLastTuile("kan");
			if (success) {
				this.piocheTuile();
				Collections.sort(this.hand);
			}
		} else if (action_id == 4) {
			success = this.volLastTuile("ron");
			// TODO ...et ? victoire
		}
		return success;
	}

	private boolean playPioche() {
		int action_id = 0;
		boolean success = false;
		this.piocheTuile();

		String[] actions = {
			// "Se défausser d'une tuile", = choix 0 à 13 ; les actions suivantes étant 14 et 15
			"Annoncer tsumo (main complétée par la pioche)",
			"Annoncer kan (complétion d'un carré, il faudra repiocher)"
		};
		int hsize1 = this.hand.size() - 1;
		String str1 = String.format("Vous avez pioché [%s] ; que faire ?", this.hand.get(hsize1));
		String str2 = String.format(" (0 à %d = se défausser de la tuile)", hsize1);
		Collections.sort(this.hand); // doit rester après l'obtention de str1
		this.updateUI(true, str1 + str2);

		action_id = this.askActionChoice(actions, true);
		if (action_id <= hsize1) {
			this.poseTuile(action_id);
			success = true;
		} else if (action_id == hsize1 + 1) {
			// tsumo
			// fin théorique de la partie (à faire vérifier TODO)
			System.out.println("TODO pas implémenté");
		} else if (action_id == hsize1 + 2) {
			success = this.volLastTuile("kan");
			if (success) {
				this.piocheTuile();
				Collections.sort(this.hand);
			}
		}
		return success;
	}

	private boolean playAnnonce(String input) throws RemoteException {
		int action_id = 0;
		String[] actions = {"invalid", "pon", "kan", "ron"};
		for(int i=1; i<4; i++) {
			if (input.equals(actions[i])) {
				action_id = i;
			}
		}

		// ignorer les annonces faites entre le moment où le joueur adversaire pioche et le moment où
		// il se défausse de la tuile de son choix.
		if (!this.getJoueurCourant().getCanBeInterrupted()) {
			System.out.println("Échec de l'annonce de " + this.getVentChar() +
			" : pas possible maintenant (joueur " + this.getJoueurCourant().getVentChar() +
			" a déjà commencé son coup)");
			return false;
		}

		boolean success = false;
		if (action_id <= 0) {
			return false; // ignorer les annonces invalides
		} else {
			success = this.volLastTuile(actions[action_id]);
			if (success) {
				if (action_id == 2) {
					// Cas du carré (annonce "kan") où il faut repiocher pour conserver le bon nombre de
					// tuiles dans sa main.
					this.piocheTuile();
					Collections.sort(this.hand);
				}
				this.isPlaying = true;
				this.playerDroite.continueGame(false);
				this.playerGauche.continueGame(false);
				this.playerFace.continueGame(false);
			}
		}
		return success;
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

	private boolean volLastTuile(String s) {
		// pas très élégant tout ce fatras, faudrait des méthodes distinctes je suppose
		ArrayList<AbstractTuile> removed = new ArrayList<AbstractTuile>();
		try {
			AbstractTuile temp = this.lastPlayer.getLastTuile();
			switch(s) {
				case "ron": // victoire par vol
					// TODO
					throw new Exception("TODO le ron n'est pas implémenté");
					// break;
				case "chii": // suite
					removed = this.getTuilesPourSuite(temp);
					break;
				case "pon": // brelan
					removed = this.getTuilesWithId(temp.getSortId(), 2);
					break;
				case "kan": // carré
					removed = this.getTuilesWithId(temp.getSortId(), 3);
					break;
				default:
					return false; // annonce invalide
			}
		} catch (Exception e) {
			// soit RemoteException soit NullPointerException soit Exception
			System.out.println("Erreur (1) lors du vol d'une tuile (annonce " + s + ") : " + e);
			return false;
		}
		try {
			AbstractTuile temp = this.lastPlayer.getLastTuile();
			this.lastPlayer.removeLastTuile();
			this.combiShown.add(temp);
			for(int i=0; i < removed.size(); i++) {
				this.hand.remove(removed.get(i));
				this.combiShown.add(removed.get(i));
			}
		} catch (RemoteException e) {
			System.out.println("Erreur (2) lors du vol d'une tuile : " + e);
		}
		return true;
	}

	private ArrayList<AbstractTuile> getTuilesPourSuite(AbstractTuile stolenTuile) throws Exception {
		ArrayList<AbstractTuile> removed = new ArrayList<AbstractTuile>();
		int stolenId = stolenTuile.getSortId();

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

		if ( (stolenIsLower && stolenIsMiddle)	|| (stolenIsMiddle && stolenIsUpper)
		                                       || (stolenIsLower && stolenIsUpper) ) {
			boolean[] osef = {stolenIsLower, stolenIsMiddle, stolenIsUpper};
			osef = this.askFavoriteChii(stolenTuile, osef);
			stolenIsLower = osef[0];
			stolenIsMiddle = osef[1];
			stolenIsUpper = osef[2];
		}

		if (stolenIsLower) {
			// [x][][]
			removed = this.getTuilesWithId(stolenId + 1, 1);
			removed.addAll(this.getTuilesWithId(stolenId + 2, 1));
		} else if (stolenIsMiddle) {
			// [][x][]
			removed = this.getTuilesWithId(stolenId + 1, 1);
			removed.addAll(this.getTuilesWithId(stolenId - 1, 1));
		} else if (stolenIsUpper) {
			// [][][x]
			removed = this.getTuilesWithId(stolenId - 1, 1);
			removed.addAll(this.getTuilesWithId(stolenId - 2, 1));
		} else {
			throw new Exception("Combinaison invalide");
		}
		return removed;
	}

	/* le genre de merdier qui prend 2h à coder mais sera exécuté probablement 0 fois */
	private boolean[] askFavoriteChii(AbstractTuile stolenTuile, boolean[] possibleChiis) {
		boolean stolenIsLower = possibleChiis[0];
		boolean stolenIsMiddle = possibleChiis[1];
		boolean stolenIsUpper = possibleChiis[2];
		int action_id = -1;
		this.updateUI(false, "Plusieurs suites sont possible avec cette tuile :");

		if (stolenIsLower && stolenIsMiddle && stolenIsUpper) {
			// both [x][][], [][x][], and [][][x] are possible
			String[] actions = {
				"Utiliser " + stolenTuile + " comme 1ère tuile de la suite",
				"Utiliser " + stolenTuile + " comme 2ème tuile de la suite",
				"Utiliser " + stolenTuile + " comme 3ème tuile de la suite"
			};
			action_id = this.askActionChoice(actions, false);
		} else if (stolenIsLower && stolenIsMiddle) {
			// both [x][][], [][x][] are possible
			String[] actions = {
				"Utiliser " + stolenTuile + " comme 1ère tuile de la suite",
				"Utiliser " + stolenTuile + " comme 2ème tuile de la suite"
			};
			action_id = this.askActionChoice(actions, false);
		} else if (stolenIsLower && stolenIsUpper) {
			// both [x][][] and [][][x] are possible
			String[] actions = {
				"Utiliser " + stolenTuile + " comme 1ère tuile de la suite",
				"Utiliser " + stolenTuile + " comme 3ème tuile de la suite"
			};
			action_id = (this.askActionChoice(actions, false) == 1) ? 2 : 0;
		} else if (stolenIsMiddle && stolenIsUpper) {
			// both [][x][], and [][][x] are possible
			String[] actions = {
				"Utiliser " + stolenTuile + " comme 2ème tuile de la suite",
				"Utiliser " + stolenTuile + " comme 3ème tuile de la suite"
			};
			action_id = this.askActionChoice(actions, false) + 1;
		}

		stolenIsLower = false;
		stolenIsMiddle = false;
		stolenIsUpper = false;
		if (action_id == 0) {
			stolenIsLower = true;
		} else if (action_id == 1) {
			stolenIsMiddle = true;
		} else if (action_id == 2) {
			stolenIsUpper = true;
		}

		boolean[] osef = {stolenIsLower, stolenIsMiddle, stolenIsUpper};
		return osef;
	}

	private boolean idExistsInHand(int tuileId) throws Exception {
		return (this.getTuilesWithId(tuileId, 1).size() > 0);
	}

	/*
	 * Récupère les tuiles de la main du joueur dont l'identifiant est sortId, dans la limite du
	 * nombre number de tuiles requises.
	 */
	private ArrayList<AbstractTuile> getTuilesWithId(int sortId, int number) throws Exception {
		ArrayList<AbstractTuile> removed = new ArrayList<AbstractTuile>();
		for(int i=0; i < this.hand.size(); i++) {
			if (removed.size() < number && this.hand.get(i).getSortId() == sortId) {
				removed.add(this.hand.get(i));
			}
		}
		if (removed.size() != number) {
			throw new Exception("Il n'y a que " + removed.size() + " tuiles comme ça dans la main.");
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
			if (j.equals(this.lastPlayer) && j.getRiviere().length() > 2) {
				// XXX en pratique pas au point : il n'y a pas toujours des tuiles à voler, même quand
				// la rivière n'est pas vide. Ceci dit, vu que lastPlayer est null après une annonce,
				// les bugs s'annulent entre eux.
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

	private MahjongPlayerInterface getJoueurCourant() {
		try {
			if (this.playerDroite.isJoueurCourant()) {
				return this.playerDroite;
			} else if (this.playerFace.isJoueurCourant()) {
				return this.playerFace;
			} else if (this.playerGauche.isJoueurCourant()) {
				return this.playerGauche;
			}
		} catch (Exception e) {}
		return this;
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
		// XXX pas fiable, si une annonce survient ça sera null notamment. Ce qui n'est pas forcément
		// entièrement gênant puisque l'effet principal est que ça skip la boucle qui demande si on
		// pioche ou si on annonce. Mais c'est généralement impossible d'annoncer. Sauf si ya un kan,
		// mais j'ai jamais eu l'occasion de tester ce cas de figure.
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

	// méthode vraiment honteuse
	private void updateRiverForPlayer(MahjongPlayerInterface j, int index) throws RemoteException {
		int lastValueR = this.riversLength[index];
		this.riversLength[index] = j.getRiviere().length(); // ce sont des tailles de chaînes
		if (lastValueR != this.riversLength[index]) {
			this.lastPlayer = j;
			// probablement d'autes choses à faire ici ?
		}
	}

}

////////////////////////////////////////////////////////////////////////////////////////////////////
