package fr.univ_nantes.SimpleMahjong.Server;
import fr.univ_nantes.SimpleMahjong.Interface.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;

public class MahjongLobby extends UnicastRemoteObject implements MahjongLobbyInterface {
	private int nbPlayers = 0;
	private ArrayList ids = new ArrayList();
	private MahjongPlayerInterface[] joueurs;

	protected MahjongLobby() throws RemoteException {
		super();
		this.joueurs = new MahjongPlayerInterface[4];
	}

	/*  */
	public void registerPlayer(MahjongPlayerInterface player, String pseudo) throws RemoteException {
		System.out.println("Requête d'un nouveau joueur (" + (this.nbPlayers+1) + "/4): " + pseudo);
		this.joueurs[this.nbPlayers] = player;
		synchronized(this) {
			this.nbPlayers++; // atomicité
		}
		if (this.nbPlayers == 4) {
			this.nbPlayers = 0;
			new MahjongTableManager(joueurs); // XXX probablement un pointeur vers le tableau des
			// joueurs, qui se ferait écraser si un 5ème se connectait ? à vérifier TODO
		}
	}

}

