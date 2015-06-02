package bank.bankieren;

import fontys.observer.BasicPublisher;
import fontys.observer.RemotePropertyListener;
import fontys.util.*;

import java.rmi.RemoteException;
import java.util.*;

public class Bank implements IBank {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8728841131739353765L;
	private Map<Integer,IRekeningTbvBank> accounts;
	private Collection<IKlant> clients;
	private int nieuwReknr;
	private String name;
        private BasicPublisher pub;

	public Bank(String name) {
		accounts = new HashMap<Integer,IRekeningTbvBank>();
		clients = new ArrayList<IKlant>();
		nieuwReknr = 100000000;	
		this.name = name;
                this.pub = new BasicPublisher(new String[]{"banksaldo"});
        }

	public int openRekening(String name, String city) {
		if (name.equals("") || city.equals(""))
			return -1;

		IKlant klant = getKlant(name, city);
		IRekeningTbvBank account = new Rekening(nieuwReknr, klant, Money.EURO);
		accounts.put(nieuwReknr,account);
		nieuwReknr++;
		return nieuwReknr-1;
	}

	private IKlant getKlant(String name, String city) {
		for (IKlant k : clients) {
			if (k.getNaam().equals(name) && k.getPlaats().equals(city))
				return k;
		}
		IKlant klant = new Klant(name, city);
		clients.add(klant);
		return klant;
	}

	public IRekening getRekening(int nr) {
		return accounts.get(nr);
	}

	public synchronized boolean maakOver(int source, int destination, Money money)
			throws NumberDoesntExistException {
		if (source == destination)
			throw new RuntimeException(
					"cannot transfer money to your own account");
		if (!money.isPositive())
			throw new RuntimeException("money must be positive");

		IRekeningTbvBank source_account = (IRekeningTbvBank) getRekening(source);
		if (source_account == null)
			throw new NumberDoesntExistException("account " + source
					+ " unknown at " + name);

		Money negative = Money.difference(new Money(0, money.getCurrency()),
				money);
                
                Money oldSaldo = source_account.getSaldo();
		boolean success = source_account.muteer(negative);
		if (!success)
			return false;

		IRekeningTbvBank dest_account = (IRekeningTbvBank) getRekening(destination);
		if (dest_account == null) 
			throw new NumberDoesntExistException("account " + destination
					+ " unknown at " + name);
		success = dest_account.muteer(money);

		if (!success) // rollback
			source_account.muteer(money);
                else
                    pub.inform(this, "banksaldo", oldSaldo.toString(), source_account.getSaldo().toString());
                    
		return success;
	}

	@Override
	public String getName() {
		return name;
	}

    @Override
    public void addListener(RemotePropertyListener rl, String string) throws RemoteException {
        pub.addListener(rl, string);
    }

    @Override
    public void removeListener(RemotePropertyListener rl, String string) throws RemoteException {
        pub.removeListener(rl, string);
    }

}
