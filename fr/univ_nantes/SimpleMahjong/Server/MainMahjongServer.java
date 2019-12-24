package fr.univ_nantes.SimpleMahjong.Server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainMahjongServer {
	private static final int portnum = 8090;
	private static final String lobbyUri = "rmi://localhost:" + portnum + "/lobby";

	public static void main (String[] args) throws Exception {
		try {
			LocateRegistry.createRegistry(portnum);

			MahjongLobby lobby = new MahjongLobby();
			Naming.bind(lobbyUri, lobby);

		} catch(Exception e) {
			System.out.println("[erreur au démarrage du serveur] " + e);
		}
	}
}

