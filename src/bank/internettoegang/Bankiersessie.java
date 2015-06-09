package bank.internettoegang;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import bank.bankieren.IBank;
import bank.bankieren.IRekening;
import bank.bankieren.Money;
import fontys.observer.BasicPublisher;
import fontys.observer.RemotePropertyListener;

import fontys.util.InvalidSessionException;
import fontys.util.NumberDoesntExistException;
import java.beans.PropertyChangeEvent;

public class Bankiersessie extends UnicastRemoteObject implements
        IBankiersessie, RemotePropertyListener {

    private static final long serialVersionUID = 1L;
    private long laatsteAanroep;
    private int reknr;
    private IBank bank;
    private BasicPublisher pub;

    public Bankiersessie(int reknr, IBank bank) throws RemoteException {
        laatsteAanroep = System.currentTimeMillis();
        this.reknr = reknr;
        this.bank = bank;
        this.pub = new BasicPublisher(new String[]{"sessiesaldo"});
        bank.addListener(this, "banksaldo");
    }

    public boolean isGeldig() {
        return System.currentTimeMillis() - laatsteAanroep < GELDIGHEIDSDUUR;
    }

    @Override
    public boolean maakOver(String bestemmingBank, int bestemming, Money bedrag)
            throws NumberDoesntExistException, InvalidSessionException,
            RemoteException {

        updateLaatsteAanroep();

        if (reknr == bestemming && bank.getName().equals(bestemmingBank)) {
            throw new RuntimeException(
                    "source and destination must be different");
        }
        if (!bedrag.isPositive()) {
            throw new RuntimeException("amount must be positive");
        }
        if(bestemmingBank.equals("")){
            throw new RuntimeException("destination bank may not be empty");
        }

        return bank.maakOver(reknr, bestemmingBank, bestemming, bedrag);
    }

    private void updateLaatsteAanroep() throws InvalidSessionException {
        if (!isGeldig()) {
            throw new InvalidSessionException("session has been expired");
        }

        laatsteAanroep = System.currentTimeMillis();
    }

    @Override
    public IRekening getRekening() throws InvalidSessionException,
            RemoteException {

        updateLaatsteAanroep();

        return bank.getRekening(reknr);
    }

    @Override
    public void logUit() throws RemoteException {
        UnicastRemoteObject.unexportObject(this, true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) throws RemoteException {
        if (isGeldig()) {
            pub.inform(this, "sessiesaldo", pce.getOldValue(), pce.getNewValue());
        }
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
