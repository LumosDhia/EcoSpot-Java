package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.models.Personne;
import tn.esprit.services.PersonneService;
import java.net.URL;
import java.util.ResourceBundle;


public class PersonneManagementGUIController implements Initializable {

    //var
    GlobalInterface global = new PersonneService();

    //Binding : Attributes
    @FXML
    private Label mainLabel;
    @FXML
    private TextField ageTF;
    @FXML
    private TextField cinTF;
    @FXML
    private TextField firstnameTF;
    @FXML
    private TextField lastnameTF;
    @FXML
    private TextArea personnesLabel;

    //Binding : Actions
    @FXML
    void AddPersonne(ActionEvent event) {
        Personne p = new Personne(Integer.parseInt(ageTF.getText()), firstnameTF.getText(), lastnameTF.getText(), cinTF.getText());
        global.add(p);
    }

    //init
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        personnesLabel.setEditable(false);
        personnesLabel.setText(global.getAll().toString());
    }
}
