package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongRoundInterface;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongLobbyInterface;

import java.rmi.Naming;

/*
 * Fonction "main" de la partie client. On récupère l'interface de connexion au serveur, puis on la
 * transmet à un objet "Player" qui représente un joueur.
 */
public class MainMahjongClient {
	private static final int portnum = 8090;
	private static final String lobbyUri = "rmi://localhost:" + portnum + "/lobby";
	private static final String roundUri = "rmi://localhost:" + portnum + "/round";
	public static void main (String[] args) {
		try {

			MahjongLobbyInterface lobbyServer = (MahjongLobbyInterface)Naming.lookup(lobbyUri);
			MahjongRoundInterface roundServer = (MahjongRoundInterface)Naming.lookup(roundUri);
			MahjongPlayer client = new MahjongPlayer(lobbyServer, roundServer);

		} catch(Exception e) {
			System.out.println("[erreur à la création du client] " + e);
		}
	}
}

