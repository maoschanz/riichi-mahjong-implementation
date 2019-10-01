package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongInterface;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongTuile;

import java.rmi.Naming;

public class MahjongClientMain {
	private static final int portnum = 8090;
	private static final String serverUri = "rmi://localhost:" + portnum + "/mahjong";
	public static void main (String[] args) {
		try {

			MahjongInterface cccccc = (MahjongInterface)Naming.lookup(serverUri);

			int n = cccccc.test_print("hello world");
			System.out.println("résultat = " + n);

			MahjongTuile t = cccccc.pioche();
			System.out.println("résultat = " + t.toString());

		} catch(Exception e) {
			System.out.println("erreur client : " + e);
		}
	}
}

