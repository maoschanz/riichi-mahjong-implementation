package fr.univ_nantes.SimpleMahjong.Server;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongInterface;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongTuile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MahjongServer extends UnicastRemoteObject implements MahjongInterface {
	private int nbPlayers = 0;
	private LinkedList<MahjongTuile> empireCeleste = new LinkedList<MahjongTuile>();
	private ArrayList<MahjongTuile> muraille = new ArrayList<MahjongTuile>();

	protected MahjongServer() throws RemoteException {
		super();
		System.out.println("Le serveur a démarré.");
		// TODO initialiser une muraille pleine
		for (int h=0; h<4; h++) {
			for (int i=1; i<10; i++) {
				this.muraille.add(new MahjongTuile(i, 'k', h));
				this.muraille.add(new MahjongTuile(i, 'b', h));
				this.muraille.add(new MahjongTuile(i, 'r', h));
			}
			for (int i=1; i<4; i++) {
				this.muraille.add(new MahjongTuile(i, 'd', h));
			}
			for (int i=1; i<5; i++) {
				this.muraille.add(new MahjongTuile(i, 'v', h));
			}
		}
		Collections.shuffle(this.muraille);
		// retourner un indicateur de dora TODO
	}

	public synchronized boolean registerPlayer(int playerId, String pseudo) throws RemoteException {
		System.out.println("Requête d'un nouveau joueur (" + (this.nbPlayers+1)
		                           + "/4): " + pseudo + " (id : " + playerId + ")");
		this.nbPlayers++;
		boolean accepted = this.nbPlayers < 5;
		if (accepted) {
			// TODO retenir les clients pour pouvoir les notifier ? sinon ils peuvent
			// requêter en boucle dans un while mdr
		}
		return accepted;
	}

	public MahjongTuile annonceEtVol(String s) throws Exception {
		System.out.println("annonce : " + s);
		switch(s) {
			case "chii": break; // suite
			case "pon": break; // brelan
			default: break; // case "kan" // carré
		}
		MahjongTuile t = this.empireCeleste.getLast();
		this.empireCeleste.removeLast();
		return t;
	}

	public MahjongTuile pioche() throws RemoteException {
		MahjongTuile t = this.muraille.get(0);
		// XXX en théorie non puisqu'on le prend plutôt à partir de la brêche
		this.muraille.remove(0);
		return t;
	}

	public void pose(MahjongTuile t) throws RemoteException {
		this.empireCeleste.addLast(t);
		// TODO notifier les joueurs de la pose
	}
}

