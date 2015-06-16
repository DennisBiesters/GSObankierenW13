package bank.bankieren;

import fontys.observer.RemotePublisher;
import java.rmi.RemoteException;

public interface IRekening extends RemotePublisher {
  int getNr() throws RemoteException;
  Money getSaldo() throws RemoteException;
  IKlant getEigenaar() throws RemoteException;
  int getKredietLimietInCenten() throws RemoteException;
}

