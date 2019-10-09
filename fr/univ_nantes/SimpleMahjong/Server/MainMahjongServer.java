package fr.univ_nantes.SimpleMahjong.Server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainMahjongServer {
	private static final int portnum = 8090;
	private static final String lobbyUri = "rmi://localhost:" + portnum + "/lobby";
	private static String roundUri = "rmi://localhost:" + portnum + "/table";

	public static void main (String[] args) throws Exception {
		try {
			LocateRegistry.createRegistry(portnum);

			MahjongLobby lobby = new MahjongLobby();
			Naming.bind(lobbyUri, lobby);

			MahjongTableManager server = new MahjongTableManager();
			Naming.bind(roundUri + "0", server); // XXX
			// TODO support several servers for more than 4 clients

		} catch(Exception e) {
			System.out.println("[erreur au démarrage du serveur] " + e);
		}
	}
}

