import java.rmi.Naming;

public class MahjongClientMain {
	public static void main (String[] args) throws Exception {
		MahjongInterface cccccc = (MahjongInterface)Naming.lookup("rmi://localhost:8090/test_print");
		int n = cccccc.test_print("hello world");
		System.out.println("résultat = " + n);
	}
}

