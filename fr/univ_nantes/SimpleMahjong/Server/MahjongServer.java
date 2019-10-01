package fr.univ_nantes.SimpleMahjong.Server;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongInterface;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongTuile;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MahjongServer extends UnicastRemoteObject implements MahjongInterface {
	int nbPlayers = 0;

	protected MahjongServer() throws RemoteException {
		super();
	}

	public boolean registerPlayer(int playerId, String pseudo) throws RemoteException {
		System.out.println("Requête d'un nouveau joueur (" + (this.nbPlayers+1)
		                           + "/4): " + pseudo + " (id : " + playerId + ")");
		// XXX le retenir ?
		this.nbPlayers++; // FIXME certainement pas atomique
		return this.nbPlayers < 5;
	}

	public int test_print (String s) throws RemoteException {
		// TODO à virer
		System.out.println(s);
		return 2;
	}

	public MahjongTuile annonceEtVol(String s) throws RemoteException {
		System.out.println("annonce : " + s);
		// switch(type) {
		// 	case 'd': this.type = TypeTuile.DRAGON; break;
		// 	case 'v': this.type = TypeTuile.VENT; break;
		// 	case 'k': this.type = TypeTuile.KANJI; break;
		// 	case 'b': this.type = TypeTuile.BAMBOU; break;
		// 	default: this.type = TypeTuile.ROND; break; // case 'r'
		// }
		MahjongTuile t = new MahjongTuile(7, 'b');
		return t;
	}

	public MahjongTuile pioche() throws RemoteException {
		MahjongTuile t = new MahjongTuile(8, 'r');
		return t;
	}
}

