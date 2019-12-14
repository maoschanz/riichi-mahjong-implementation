package fr.univ_nantes.SimpleMahjong.Server;
import fr.univ_nantes.SimpleMahjong.Interface.*;
import fr.univ_nantes.SimpleMahjong.Tuile.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.concurrent.TimeUnit;

public class MahjongTableManager extends UnicastRemoteObject implements MahjongTableInterface {
	private int nbPlayers = 0;
	private MahjongPlayerInterface[] players;
	private ArrayList<AbstractTuile> muraille = new ArrayList<AbstractTuile>();

	protected MahjongTableManager(MahjongPlayerInterface[] players) throws RemoteException {
		super();
		this.players = players;
		System.out.println("[démarrage du gestionnaire de table]");
		this.initMuraille();
		ArrayList<String> VENTS = this.initVents();

		try {
			for (int i=0; i<4; i++) {
				this.players[i].initTable(this);
				this.players[i].initHand(VENTS.get(i));
			}
			for (int i=0; i<4; i++) {
				for (int h=0; h<4; h++) {
					if (h != i) {
						this.players[i].discoverOther(this.players[h]);
					}
				}
			}
		} catch(Exception e) {
			System.out.println("[erreur à l'initialisation de la table de jeu] " + e);
		}

		try {
			for (int i=0; i<4; i++) {
				if (!VENTS.get(i).equals("東")) {
					this.players[i].startGame(false);
				}
			}
			// Le vent d'est doit être lancé en dernier
			for (int i=0; i<4; i++) {
				if (VENTS.get(i).equals("東")) {
					this.players[i].startGame(true);
				}
			}
		} catch(Exception e) {
			System.out.println("[erreur au démarrage des joueurs] " + e);
			System.out.println(e);
		}
	}

	//-----------------------------------------------------------------------------------------------

	/*
	 * Fournit une nouvelle tuile au joueur qui en demande. Il est important que la muraille soit
	 * gérée par le serveur car il serait INACCEPTABLE que les tuiles soient générées à la volée par
	 * les clients : un joueur qui voit quatre exemplaires d'une tuile donnée dans la rivière ne
	 * devrait normalement plus pouvoir espérer piocher une telle tuile. À l'inverse, une tuile
	 * jamais posée dans la rivière se trouve soit dans le mur (réduit en fin de partie) soit dans
	 * les mains des autres joueurs, ce qui influe sur les tactiques de jeu à adopter.
	 */
	public AbstractTuile pioche() throws RemoteException {
		AbstractTuile t = this.muraille.get(0);
		// XXX en théorie non puisqu'on le prend plutôt à partir de la brêche, qui n'est pas à 0
		this.muraille.remove(0);
		return t;
	}

	//-----------------------------------------------------------------------------------------------

	private ArrayList<String> initVents() {
		ArrayList<String> vents = new ArrayList<String>();
		vents.add("東");
		vents.add("南");
		vents.add("西");
		vents.add("北");
		Collections.shuffle(vents);
		return vents;
	}

	/*
	 * Initialisation d'une muraille pleine. Au Mahjong japonais, la muraille contient 136 tuiles, en
	 * fait 34 tuiles en 4 exemplaires chacune. Elles se répartissent ainsi :
	 * - 9 tuiles numérotées avec un rond (🔵)
	 * - 9 tuiles numérotées avec un bambou (🎍)
	 * - 9 tuiles numérotées avec un kanji (万)
	 * - 4 tuiles "vents" (est東/nord北/ouest西/sud南)
	 * - 3 tuiles "dragon" (rouge/vert/blanc)
	 * On commencera le jeu en distribuant une partie de ces tuiles aux joueurs. Par la suite, ils
	 * pourront piocher dedans.
	 * TODO théoriquement, une partie de la muraille, dite "mur mort", n'est pas disponible pour être
	 * piochée, et contient des tuiles retournées (visibles des joueurs) qui modifient les valeurs
	 * des scores (https://fr.wikipedia.org/wiki/R%C3%A8gles_du_Mah-jong_(Riichi)#Dora). Ceci n'est
	 * pas implémenté.
	 */
	private void initMuraille() {
		for (int h=0; h<4; h++) {
			for (int i=1; i<10; i++) {
				this.muraille.add(new TuileNombre('k', h, i));
				this.muraille.add(new TuileNombre('b', h, i));
				this.muraille.add(new TuileNombre('r', h, i));
			}
			for (int i=1; i<4; i++) {
				this.muraille.add(new TuileDragon(i, h));
			}
			for (int i=1; i<5; i++) {
				this.muraille.add(new TuileVent(i, h));
			}
		}
		Collections.shuffle(this.muraille);
	}

}

