import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MahjongServer extends UnicastRemoteObject implements MahjongInterface {
	protected MahjongServer() throws RemoteException {
		super();
	}

	public int test_print (String s) throws RemoteException {
		System.out.println(s);
		return 2;
	}
}

