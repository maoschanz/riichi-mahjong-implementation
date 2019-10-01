package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongInterface;

import java.rmi.Naming;

/*
 * Fonction "main" de la partie client. On récupère l'interface de connexion au serveur, puis on la
 * transmet à un objet "Player" qui représente un joueur.
 */
public class MainMahjongClient {
	private static final int portnum = 8090;
	private static final String serverUri = "rmi://localhost:" + portnum + "/mahjong";
	public static void main (String[] args) {
		try {

			MahjongInterface server = (MahjongInterface)Naming.lookup(serverUri);
			MahjongPlayer client = new MahjongPlayer(server);

		} catch(Exception e) {
			System.out.println("[erreur à la création du client] " + e);
		}
	}
}

