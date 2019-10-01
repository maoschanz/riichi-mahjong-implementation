package fr.univ_nantes.SimpleMahjong.Client;
import fr.univ_nantes.SimpleMahjong.Interface.MahjongInterface;

import java.rmi.Naming;

public class MahjongClientMain {
	private static final int portnum = 8090;
	public static void main (String[] args) {
		try {

			String uri = "rmi://localhost:" + portnum + "/test_print";
			MahjongInterface cccccc = (MahjongInterface)Naming.lookup(uri);
			int n = cccccc.test_print("hello world");
			System.out.println("résultat = " + n);

		} catch(Exception e) {
			System.out.println("erreur client : " + e);
		}
	}
}

