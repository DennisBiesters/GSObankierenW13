/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank.gui;

import fontys.observer.RemotePropertyListener;
import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author Dennis
 */
public class BankierSessieControllerListener extends UnicastRemoteObject implements RemotePropertyListener {

    BankierSessieController parent;

    public BankierSessieControllerListener(BankierSessieController parent) throws RemoteException {
        this.parent = parent;
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) throws RemoteException {
        parent.changeSaldo(pce.getNewValue().toString());
    }
}
