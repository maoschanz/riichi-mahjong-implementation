package fr.univ_nantes.SimpleMahjong.Server;
import fr.univ_nantes.SimpleMahjong.Interface.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.concurrent.TimeUnit;

public class MahjongTableManager extends UnicastRemoteObject implements MahjongTableInterface {
	private int nbPlayers = 0;
	private MahjongPlayerInterface[] players;
	private LinkedList<AbstractTuile> river = new LinkedList<AbstractTuile>(); // TODO n'a pas lieu d'être ici
	private ArrayList<AbstractTuile> muraille = new ArrayList<AbstractTuile>();

	protected MahjongTableManager(MahjongPlayerInterface[] players) throws RemoteException {
		super();
		this.players = players;
		System.out.println("[démarrage du gestionnaire de table]");

		// Initialisation d'une muraille pleine
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
		// retourner un indicateur de dora TODO

		String[] VENTS= {"東", "南", "西", "北"}; // TODO passer des TuileVent plutôt
		try {
			for (int i=0; i<4; i++) {
				this.players[i].initTable(this);
				this.players[i].initHand(VENTS[i]);
				for (int h=0; h<4; h++) {
					if (h != i) {
						this.players[i].discoverOther(this.players[h], VENTS[h], h);
					}
				}
				this.players[i].startGame(VENTS[i].equals("東"));
			}
		} catch(Exception e) {
			System.out.println("[erreur à l'initialisation de la table] " + e);
		}
	}

	//----------------------------------------------------------------------------------------------

	/*
	 * TODO pourrait se faire entre joueurs sans intervention du serveur
	 */
	public AbstractTuile annonceEtVol(String s) throws Exception {
		System.out.println("annonce : " + s);
		switch(s) {
			case "chii": break; // suite TODO
			case "pon": break; // brelan TODO
			default: break; // case "kan" // carré TODO
		}
		AbstractTuile t = this.river.getLast();
		this.river.removeLast();
		return t;
	}

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
		// XXX en théorie non puisqu'on le prend plutôt à partir de la brêche
		this.muraille.remove(0);
		return t;
	}

	/*
	 * TODO pourrait se gérer entre joueurs sans intervention du serveur
	 */
	public void pose(AbstractTuile t) throws RemoteException {
		this.river.addLast(t);
		// TODO notifier les joueurs de la pose
	}
}

