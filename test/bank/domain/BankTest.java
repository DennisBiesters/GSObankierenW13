/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank.domain;

import bank.bankieren.Bank;
import bank.bankieren.IRekening;
import bank.bankieren.Money;
import fontys.util.NumberDoesntExistException;
import java.rmi.RemoteException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dennis
 */
public class BankTest {

    /**
     * Tests for the openRekening method
     */
    @Test 
    public void testOpenRekening() throws RemoteException {
        // setup
        Bank bank = new Bank("Rabobank");

        // add new accounts
        assertEquals(100000000, bank.openRekening("Dennis", "Geldrop"));
        assertEquals(100000001, bank.openRekening("Rick", "Eindhoven"));

        // check if new klant is added and information is correct
        assertEquals("Dennis", bank.getRekening(100000000).getEigenaar().getNaam());
        assertEquals("Eindhoven", bank.getRekening(100000001).getEigenaar().getPlaats());

        // fail test if input is incorrect
        assertEquals(-1, bank.openRekening("Dennis", ""));
        assertEquals(-1, bank.openRekening("", "Geldrop"));
        assertEquals(-1, bank.openRekening("", ""));
    }

    /**
     * Test for the getRekening method
     */
    @Test
    public void testGetRekening() throws RemoteException{
        // setup
        Bank bank = new Bank("ING");
        Integer nr = bank.openRekening("Henk", "Nutspeet");
        IRekening rekening = bank.getRekening(nr);

        // test if rekening exists
        assertEquals("Henk", rekening.getEigenaar().getNaam());
        assertEquals("Nutspeet", rekening.getEigenaar().getPlaats());

        // fail test if rekening doesnt exists
        assertNull(bank.getRekening(1));
        assertNull(bank.getRekening(100000001));
    }

    /**
     * Test for the maakOver method
     */
    @Test
    public void testMaakOver() throws NumberDoesntExistException, RemoteException {
        // setup
        Bank bank = new Bank("Tridios");
        Integer bron = bank.openRekening("Henk", "Zwolle");
        Integer bestemming = bank.openRekening("Fred", "America");

        // true tests
        assertTrue(bank.maakOver(bron,"Tridios", bestemming, new Money(100, Money.EURO)));
        
        // check if saldos are changed
        assertEquals("-1,00", bank.getRekening(bron).getSaldo().getValue());
        assertEquals("1,00", bank.getRekening(bestemming).getSaldo().getValue());
        
        // false tests
        assertFalse(bank.maakOver(bron,"Tridios", bestemming, new Money(1000000, Money.EURO)));       
        
        // check if saldos aren't changed
        assertEquals("-1,00", bank.getRekening(bron).getSaldo().getValue());
        assertEquals("1,00", bank.getRekening(bestemming).getSaldo().getValue());
        
        // fail test if input is incorrect
        try {
            bank.maakOver(bron,"Tridios", bron, new Money(1, Money.EURO));
            fail("money transferred to your own account");
        } catch (RuntimeException exc) {
        }

        try {
            bank.maakOver(bron,"Tridios", bestemming, new Money(-1, Money.EURO));
            fail("negative amount of money transferred");
        } catch (RuntimeException exc) {
        }

        try {
            bank.maakOver(1,"Tridios", bestemming, new Money(1, Money.EURO));
            fail("money transferred from not existing source");
        } catch (NumberDoesntExistException exc) {
        }
        
        try {
            bank.maakOver(bron,"Tridios", 1, new Money(1, Money.EURO));
            fail("money transferred from not existing destination");
        } catch (NumberDoesntExistException exc) {
        }
    }

    /**
     * Test for the getName method
     */
    @Test
    public void testGetName() throws RemoteException {
        // setup
        Bank bank = new Bank("ABN AMRO");

        // check if name is correct
        assertEquals("ABN AMRO", bank.getName());
    }
}
