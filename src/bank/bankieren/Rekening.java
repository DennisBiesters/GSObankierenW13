package bank.bankieren;

import fontys.observer.BasicPublisher;
import fontys.observer.RemotePropertyListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

class Rekening extends UnicastRemoteObject implements IRekeningTbvBank {

    private static final long serialVersionUID = 7221569686169173632L;
    private static final int KREDIETLIMIET = -10000;
    private int nr;
    private IKlant eigenaar;
    private Money saldo;
    private BasicPublisher pub;

    /**
     * creatie van een bankrekening met saldo van 0.0<br>
     * de constructor heeft package-access omdat de PersistentAccount-objecten
     * door een het PersistentBank-object worden beheerd
     *
     * @see banking.persistence.PersistentBank
     * @param number het bankrekeningnummer
     * @param klant de eigenaar van deze rekening
     * @param currency de munteenheid waarin het saldo is uitgedrukt
     */
    Rekening(int number, IKlant klant, String currency) throws RemoteException {
        this(number, klant, new Money(0, currency));
    }

    /**
     * creatie van een bankrekening met saldo saldo<br>
     * de constructor heeft package-access omdat de PersistentAccount-objecten
     * door een het PersistentBank-object worden beheerd
     *
     * @see banking.persistence.PersistentBank
     * @param number het bankrekeningnummer
     * @param name de naam van de eigenaar
     * @param city de woonplaats van de eigenaar
     * @param currency de munteenheid waarin het saldo is uitgedrukt
     */
    Rekening(int number, IKlant klant, Money saldo) throws RemoteException {
        this.nr = number;
        this.eigenaar = klant;
        this.saldo = saldo;
        this.pub = new BasicPublisher(new String[]{"rekeningsaldo"});
    }

    public boolean equals(Object obj) {
        try {
            return nr == ((IRekening) obj).getNr();
        } catch (RemoteException ex) {
            Logger.getLogger(Rekening.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public int getNr() {
        return nr;
    }

    public String toString() {
        return nr + ": " + eigenaar.toString();
    }

    boolean isTransferPossible(Money bedrag) {
        return (bedrag.getCents() + saldo.getCents() >= KREDIETLIMIET);
    }

    public IKlant getEigenaar() {
        return eigenaar;
    }

    public Money getSaldo() {
        return saldo;
    }

    public boolean muteer(Money bedrag) {
        if (bedrag.getCents() == 0) {
            throw new RuntimeException(" bedrag = 0 bij aanroep 'muteer'");
        }

        if (isTransferPossible(bedrag)) {
            Money oldSaldo = saldo;
            saldo = Money.sum(saldo, bedrag);
            pub.inform(this, "rekeningsaldo", oldSaldo.toString(), saldo.toString());
            System.out.println(eigenaar.getNaam() + " te " + eigenaar.getPlaats() + ": " + nr + " saldo: " + saldo);
            return true;
        }
        return false;
    }

    @Override
    public int getKredietLimietInCenten() {
        return KREDIETLIMIET;
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
