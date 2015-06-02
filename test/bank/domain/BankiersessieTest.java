/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank.domain;

import bank.bankieren.Bank;
import bank.bankieren.IRekening;
import bank.bankieren.Money;
import bank.internettoegang.Balie;
import bank.internettoegang.Bankiersessie;
import bank.internettoegang.IBankiersessie;
import fontys.util.InvalidSessionException;
import fontys.util.NumberDoesntExistException;
import java.rmi.RemoteException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rick
 */
public class BankiersessieTest {

    @Test
    public void isGeldigTest() throws RemoteException {

        //setup
        Bank testBank = new Bank("Rabobank");
        Balie testBalie = new Balie(testBank);
        String name = testBalie.openRekening("Rick", "Eindhoven", "Password");
        IBankiersessie testSessie = testBalie.logIn(name, "password");

        //normal tests
        assertTrue(testSessie.isGeldig());

        //fail tests
        //ik zie niet how ik dit kan testen, gezien ik 'GELDIGSHEIDSDUUR' en 'laatsteAanroep' niet aan te passen zijn
    }

    @Test
    public void testMaakOver() throws RemoteException, NumberDoesntExistException, InvalidSessionException {

        //setup
        Bank testBank = new Bank("Rabobank");
        Balie testBalie = new Balie(testBank);
        String name = testBalie.openRekening("Rick", "Eindhoven", "Password");
        testBalie.openRekening("Dennis", "Geldrop", "Password");
        IBankiersessie testSessie = testBalie.logIn(name, "password");
        Money geld = new Money(1, Money.EURO);

        //normal tests
        assertTrue(testSessie.maakOver(100000001, geld));

        //fail tests
        try {
            testSessie.maakOver(100000000, geld);
            fail("source and destination must be different");
        } catch (RuntimeException exc) {
        }

        try {
            Money negaGeld = new Money(-1, Money.EURO);
            testSessie.maakOver(100000001, negaGeld);
            fail("amount must be positive");
        } catch (RuntimeException exc) {
        }

    }

    @Test
    public void testGetRekening() throws RemoteException, InvalidSessionException {

        //setup
        Bank testBank = new Bank("Rabobank");
        Balie testBalie = new Balie(testBank);
        String name = testBalie.openRekening("Rick", "Eindhoven", "Password");
        IBankiersessie testSessie = testBalie.logIn(name, "password");

        //normal tests
        assertEquals(100000000, testSessie.getRekening());
    }

    @Test
    public void testLogUit() throws RemoteException, InvalidSessionException {

        //setup
        Bank testBank = new Bank("Rabobank");
        Balie testBalie = new Balie(testBank);
        String name = testBalie.openRekening("Rick", "Eindhoven", "Password");
        IBankiersessie testSessie = testBalie.logIn(name, "password");

        //normal test
        try {
            testSessie.logUit();
            testSessie.getRekening();
            fail("you should be logged out");
        } catch (RemoteException | InvalidSessionException exc) {
        }

    }

}
