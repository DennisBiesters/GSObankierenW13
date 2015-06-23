/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank.server;

import bank.bankieren.Bank;
import bank.bankieren.IBank;
import bank.centrale.CentraleBank;
import bank.centrale.ICentraleBank;
import bank.gui.BankierClient;
import bank.internettoegang.Balie;
import bank.internettoegang.IBalie;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author frankcoenen
 */
public class BalieServer extends Application {

    private Stage stage;
    private final double MINIMUM_WINDOW_WIDTH = 600.0;
    private final double MINIMUM_WINDOW_HEIGHT = 200.0;
    private String nameBank;

    @Override
    public void start(Stage primaryStage) throws IOException {

        try {
            stage = primaryStage;
            stage.setTitle("Bankieren");
            stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
            stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
            gotoBankSelect();

            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean startBalie(String nameBank) {

        FileOutputStream out = null;
        try {
            this.nameBank = nameBank;
            String address = java.net.InetAddress.getLocalHost().getHostAddress();
            int port = 1099;
            Properties props = new Properties();
            String rmiBalie = address + ":" + port + "/" + "RaboBank";
            props.setProperty("balie", rmiBalie);
            out = new FileOutputStream("RaboBank" + ".props");
            props.store(out, null);
            
            props = new Properties();
            props.setProperty("balie", address + ":" + port + "/" + "ING");
            out = new FileOutputStream("ING" + ".props");
            props.store(out, null);
            
            props = new Properties();
            props.setProperty("balie", address + ":" + port + "/" + "SNS");
            out = new FileOutputStream("SNS" + ".props");
            props.store(out, null);
            
            props = new Properties();
            props.setProperty("balie", address + ":" + port + "/" + "ABN AMRO");
            out = new FileOutputStream("ABN AMRO" + ".props");
            props.store(out, null);
            
            props = new Properties();
            props.setProperty("balie", address + ":" + port + "/" + "ASN");
            out = new FileOutputStream("ASN" + ".props");
            props.store(out, null);          
            out.close();
            
            /**
             * create balie registry
             */
            Registry balieRegistry = LocateRegistry.createRegistry(port);
            
            /**
             * create bank registry
             */
            Registry bankRegistry = LocateRegistry.createRegistry(1098);

            /**
             * create bank objects and rebind to registry RaboBank, ING, SNS,
             * ABN AMRO, ASN
             */
            IBank rabo = new Bank("RaboBank");
            IBank ing = new Bank("ING");
            IBank sns = new Bank("SNS");
            IBank abn = new Bank("ABN");
            IBank asn = new Bank("ASN");

            bankRegistry.rebind("RaboBank", rabo);
            bankRegistry.rebind("ING", ing);
            bankRegistry.rebind("SNS", sns);
            bankRegistry.rebind("ABN", abn);
            bankRegistry.rebind("ASN", asn);

            IBalie balie = null;

            // only for testing purpose
            balie = new Balie(rabo);
            //System.out.println(balie.openRekening("Henk", "Helmond", "nope"));
            balieRegistry.rebind("RaboBank", balie);
            balie = new Balie(ing);
            balieRegistry.rebind("ING", balie);
            balie = new Balie(sns);
            balieRegistry.rebind("SNS", balie);
            balie = new Balie(abn);
            balieRegistry.rebind("ABN AMRO", balie);
            balie = new Balie(asn);
            balieRegistry.rebind("ASN", balie);

            switch (nameBank) {
                case "RaboBank":
                    balie = new Balie(rabo);
                    break;
                case "ING":
                    balie = new Balie(ing);
                    break;
                case "SNS":
                    balie = new Balie(sns);
                    break;
                case "ABN AMRO":
                    balie = new Balie(abn);
                    break;
                case "ASN":
                    balie = new Balie(asn);
                    break;
            }

            try {
                ICentraleBank centraleBank = new CentraleBank();
                Registry centraleRegistry = LocateRegistry.createRegistry(1097);
                centraleRegistry.rebind("cb", centraleBank);
            } catch (RemoteException ex) {
                Logger.getLogger(CentraleBank.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (balie == null) {
                return false;
            }

            // only for testing purpose
            //System.out.println(balie.openRekening("Dennis", "Geldrop", "test"));
            //System.out.println(balie.openRekening("Rick", "Eindhoven", "nein"));

            balieRegistry.rebind(nameBank, balie);

            return true;

        } catch (IOException ex) {
            Logger.getLogger(BalieServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(BalieServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public void gotoBankSelect() {
        try {
            BalieController bankSelect = (BalieController) replaceSceneContent("Balie.fxml");
            bankSelect.setApp(this);
        } catch (Exception ex) {
            Logger.getLogger(BankierClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = BalieServer.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(BalieServer.class.getResource(fxml));
        AnchorPane page;
        try {
            page = (AnchorPane) loader.load(in);
        } finally {
            in.close();
        }
        Scene scene = new Scene(page, 800, 600);
        stage.setScene(scene);
        stage.sizeToScene();
        return (Initializable) loader.getController();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
