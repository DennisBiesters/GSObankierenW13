package bank.bankieren;

import bank.centrale.ICentraleBank;
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

    public Bank(String name) throws RemoteException {
        accounts = new HashMap<Integer, IRekeningTbvBank>();
        clients = new ArrayList<IKlant>();
        nieuwReknr = 100000000;
        this.name = name;
    }

    public int openRekening(String name, String city) {
        if (name.equals("") || city.equals("")) {
            return -1;
        }

        IKlant klant = getKlant(name, city);
        IRekeningTbvBank account = null;
        try {
            account = new Rekening(nieuwReknr, klant, Money.EURO);
        } catch (RemoteException ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    public synchronized boolean maakOver(int source, String destinationBank, int destination, Money money)
            throws NumberDoesntExistException {
        if (source == destination && this.name.equals(destinationBank)) {
            throw new RuntimeException(
                    "cannot transfer money to your own account");
        }
        if (!money.isPositive()) {
            throw new RuntimeException("money must be positive");
        }

        IRekeningTbvBank source_account = null;
        try {
            source_account = (IRekeningTbvBank) getRekening(source);
        } catch (RemoteException ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (source_account == null) {
            throw new NumberDoesntExistException("account " + source
                    + " unknown at " + name);
        }

        Money negative = Money.difference(new Money(0, money.getCurrency()),
                money);

        boolean success = false;
        try {
            success = source_account.muteer(negative);
        } catch (RemoteException ex) {
            Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (!success) {
            return false;
        }

        IRekeningTbvBank dest_account = null;

        if (this.name.equals(destinationBank)) {
            try {
                dest_account = (IRekeningTbvBank) getRekening(destination);
            } catch (RemoteException ex) {
                Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (dest_account == null) {
                throw new NumberDoesntExistException("account " + destination
                        + " unknown at " + name);
            }

            try {
                success = dest_account.muteer(money);
            } catch (RemoteException ex) {
                Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            try {
                source_account.muteer(money);
            } catch (RemoteException ex) {
                Logger.getLogger(Bank.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return success;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean muteer(int destination, Money money) throws RemoteException {
        IRekeningTbvBank rekening = getRekening(destination);
        
        if(rekening == null){
            return false;
        }
        
        return rekening.muteer(money);
    }

    @Override
    public IRekeningTbvBank getRekening(int nr) throws RemoteException {
       return accounts.get(nr);
    }
}
