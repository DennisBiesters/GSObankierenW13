package bank.bankieren;

import bank.centrale.ICentraleBank;
import fontys.observer.BasicPublisher;
import fontys.observer.RemotePropertyListener;
import fontys.util.*;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bank extends UnicastRemoteObject implements IBank{

    /**
     *
     */
    private static final long serialVersionUID = -8728841131739353765L;
    private Map<Integer, IRekeningTbvBank> accounts;
    private Collection<IKlant> clients;
    private int nieuwReknr;
    private String name;
    private BasicPublisher pub;

    public Bank(String name) throws RemoteException {
        accounts = new HashMap<Integer, IRekeningTbvBank>();
        clients = new ArrayList<IKlant>();
        nieuwReknr = 100000000;
        this.name = name;
        this.pub = new BasicPublisher(new String[]{"banksaldo"});
    }

    public int openRekening(String name, String city) {
        if (name.equals("") || city.equals("")) {
            return -1;
        }

        IKlant klant = getKlant(name, city);
        IRekeningTbvBank account = new Rekening(nieuwReknr, klant, Money.EURO);
        accounts.put(nieuwReknr, account);
        nieuwReknr++;
        return nieuwReknr - 1;
    }

    private IKlant getKlant(String name, String city) {
        for (IKlant k : clients) {
            if (k.getNaam().equals(name) && k.getPlaats().equals(city)) {
                return k;
            }
        }
        IKlant klant = new Klant(name, city);
        clients.add(klant);
        return klant;
    }

    @Override
    public IRekening getRekening(int nr) {
        return accounts.get(nr);
    }
    
    @Override
    public synchronized boolean maakOver(int source, String destinationBank, int destination, Money money)
            throws NumberDoesntExistException {
        if (source == destination && this.name.equals(destinationBank)) {
            throw new RuntimeException(
                    "cannot transfer money to your own account");
        }
        if (!money.isPositive()) {
            throw new RuntimeException("money must be positive");
        }

        IRekeningTbvBank source_account = (IRekeningTbvBank) getRekening(source);
        if (source_account == null) {
            throw new NumberDoesntExistException("account " + source
                    + " unknown at " + name);
        }

        Money negative = Money.difference(new Money(0, money.getCurrency()),
                money);

        Money oldSaldo = source_account.getSaldo();
        boolean success = source_account.muteer(negative);
        if (!success) {
            return false;
        }

        IRekeningTbvBank dest_account = null;

        if (this.name.equals(destinationBank)) {
            dest_account = (IRekeningTbvBank) getRekening(destination);

            if (dest_account == null) {
                throw new NumberDoesntExistException("account " + destination
                        + " unknown at " + name);
            }

            success = dest_account.muteer(money);
        } else {
            //centrale bank: bank opzoeken, rekening opzoeken, muteer
            String address;
            try {
                address = java.net.InetAddress.getLocalHost().getHostAddress();
                Registry registry = LocateRegistry.getRegistry(address, 1097);
                ICentraleBank cb = (ICentraleBank) registry.lookup("cb");
                success = cb.maakOver(destinationBank, destination, money);
            } catch (UnknownHostException | RemoteException | NotBoundException ex) {
                Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            }
        }

        if (!success) // rollback
        {
            source_account.muteer(money);
        } else {
            pub.inform(this, "banksaldo", oldSaldo.toString(), source_account.getSaldo().toString());
        }

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
