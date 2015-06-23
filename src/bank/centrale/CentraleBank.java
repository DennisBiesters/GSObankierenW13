/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank.centrale;

import bank.bankieren.IBank;
import bank.bankieren.IRekeningTbvBank;
import bank.bankieren.Money;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dennis
 */
public class CentraleBank extends UnicastRemoteObject implements ICentraleBank{   
    private IBank foundBank;
    private IRekeningTbvBank foundRekening;
    
    public CentraleBank() throws RemoteException{
        foundBank = null;
        foundRekening = null;
    }
    
    @Override
    public boolean maakOver(String destinationBank, int destination, Money money){
        
        try {
            String address = java.net.InetAddress.getLocalHost().getHostAddress();
            Registry registry = LocateRegistry.getRegistry(address, 1098);
            foundBank = (IBank) registry.lookup(destinationBank);
                      
            return foundBank.muteer(destination, money);
                     
        } catch (UnknownHostException | RemoteException | NotBoundException ex) {
            Logger.getLogger(CentraleBank.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    } 
}
