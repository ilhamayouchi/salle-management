package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.repository.SalleRepositoryImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class App {

    public static void main(String[] args) {
        // Connexion JPA
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        EntityManager em = emf.createEntityManager();

        // Insertion des données
        em.getTransaction().begin();

        Salle s1 = new Salle();
        s1.setNom("Salle A");
        s1.setCapacite(20);
        em.persist(s1);

        Salle s2 = new Salle();
        s2.setNom("Salle B");
        s2.setCapacite(50);
        em.persist(s2);

        Salle s3 = new Salle();
        s3.setNom("Salle C");
        s3.setCapacite(10);
        em.persist(s3);

        // Salle A est réservée de 10h à 12h (chevauche le créneau 9h-11h)
        Reservation r1 = new Reservation();
        r1.setSalle(s1);
        r1.setDebut(LocalDateTime.of(2024, 6, 1, 10, 0));
        r1.setFin(LocalDateTime.of(2024, 6, 1, 12, 0));
        em.persist(r1);

        em.getTransaction().commit();

        SalleRepositoryImpl repo = new SalleRepositoryImpl();
        repo.setEntityManager(em);


        // Créneau recherché :
        LocalDateTime debut = LocalDateTime.of(2024, 6, 1, 9, 0);
        LocalDateTime fin = LocalDateTime.of(2024, 6, 1, 11, 0);

        List<Salle> salles = repo.findAvailableRooms(debut, fin);
        System.out.println("Salles disponibles entre " + debut + " et " + fin + " :");
        for (Salle s : salles) {
            System.out.println("- " + s.getNom() + " (Capacité: " + s.getCapacite() + ")");
        }

        em.close();
        emf.close();
    }
}