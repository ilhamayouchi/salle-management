package com.example.repository;

import com.example.model.Salle;
import java.time.LocalDateTime;
import java.util.List;


// Contrat d'accès aux données :
public interface SalleRepository {
    // Salle libre sur un créneau
    List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin);


    List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);
    // Nombre total de résultats
    long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin);
}
