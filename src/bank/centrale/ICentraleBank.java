/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank.centrale;

import bank.bankieren.Money;
import java.rmi.Remote;

/**
 *
 * @author Dennis
 */
public interface ICentraleBank extends Remote{
    public boolean maakOver(String destinationBank, int destination, Money money);
}
