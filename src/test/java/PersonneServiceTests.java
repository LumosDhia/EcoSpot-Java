import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tn.esprit.models.Personne;
import tn.esprit.services.PersonneService;

import java.util.List;

public class PersonneServiceTests {

    //var
    static PersonneService personneService;

    @BeforeAll
    static void setUp() {
      personneService = new PersonneService();
    }


    @Test
    void AddPersonneTest() {
        Personne p = new Personne(18, "Oncle", "Sam", "99999999");
        personneService.add(p);
        List<Personne> personnes = personneService.getAll();
        Assertions.assertTrue(personnes.stream().anyMatch(personne -> personne.getCin().equals("99999999")), "Ajout personne echoué!");
    }


    @AfterAll
    static void tearDown() {
        personneService.delete(personneService.getByCin("99999999"));
        personneService = null;

    }
}
