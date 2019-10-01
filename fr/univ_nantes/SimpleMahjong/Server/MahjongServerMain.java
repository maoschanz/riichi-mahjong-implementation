package fr.univ_nantes.SimpleMahjong.Server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MahjongServerMain {
	public static void main (String[] args) throws Exception {
		try {
			LocateRegistry.createRegistry(8090);
			MahjongServer eeeee = new MahjongServer();
			Naming.bind("rmi://localhost:8090/test_print", eeeee);
			System.out.println("le serveur a démarré");
		} catch(Exception e) {
			System.out.println("erreur : " + e);
		}
	}
}

