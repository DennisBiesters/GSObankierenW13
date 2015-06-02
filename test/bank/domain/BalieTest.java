/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank.domain;

import bank.internettoegang.Balie;
import org.junit.Test;
import bank.bankieren.Bank;
import bank.internettoegang.Bankiersessie;
import bank.internettoegang.IBankiersessie;
import java.rmi.RemoteException;
import static org.junit.Assert.*;

/**
 *
 * @author rick
 */
public class BalieTest {

    @Test
    public void testOpenRekening() throws RemoteException {

        //setup
        Bank testBank = new Bank("Rabobank");
        Balie testBalie = new Balie(testBank);

        //normal tests
        assertEquals(100000000, testBalie.openRekening("Rick", "Eindhoven", "Password"));
        assertEquals(100000001, testBalie.openRekening("Dennis", "Geldrop", "Password"));

        //fail tests
        assertNull(testBalie.openRekening("Rick", "", ""));
        assertNull(testBalie.openRekening("", "Eindhoven", ""));
        assertNull(testBalie.openRekening("", "", "Password"));
        assertNull(testBalie.openRekening("", "", ""));
    }

    @Test
    public void testLogIn() throws RemoteException {
        
        //setup
        Bank testBank = new Bank("Rabobank");
        Balie testBalie = new Balie(testBank);
        IBankiersessie sessie = new Bankiersessie(100000000, testBank);
        testBalie.openRekening("Rick", "Eindhoven", "Password");
        
        //normal tests
        assertEquals(sessie, testBalie.logIn("Rick", "Password"));
        
        //fail tests
        assertNotNull(testBalie.logIn("Kevin", "Bram"));
        assertNotNull(testBalie.logIn("Rick", "Bram"));
        assertNotNull(testBalie.logIn("", ""));
    }
    
}
