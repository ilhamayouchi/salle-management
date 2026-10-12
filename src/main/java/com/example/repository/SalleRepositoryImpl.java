package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// Implémentation du repository
@Stateless
public class SalleRepositoryImpl implements SalleRepository {

    @PersistenceContext(unitName = "SallePU")
    private EntityManager entityManager;

    // salles disponibles sur un créneau recherche par JPQL
    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        String jpql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                "SELECT r.salle.id FROM Reservation r " +
                "WHERE (:debut < r.fin AND :fin > r.debut))";
        return entityManager.createQuery(jpql, Salle.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }
    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin,
                                   LocalDateTime debut, LocalDateTime fin,
                                   int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> cq = cb.createQuery(Salle.class);
        Root<Salle> salle = cq.from(Salle.class);

        List<Predicate> predicates = new ArrayList<>();

        if (nom != null && !nom.isEmpty()) {
            predicates.add(cb.like(cb.lower(salle.<String>get("nom")), "%" + nom.toLowerCase() + "%"));
        }

        if (capaciteMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(salle.<Integer>get("capacite"), capaciteMin));
        }

        if (debut != null && fin != null) {
            Subquery<Long> sub = cq.subquery(Long.class);
            Root<Reservation> reservation = sub.from(Reservation.class);
            sub.select(reservation.get("salle").<Long>get("id"));
            sub.where(cb.and(
                    cb.lessThan(reservation.<LocalDateTime>get("debut"), fin),
                    cb.greaterThan(reservation.<LocalDateTime>get("fin"), debut)
            ));
            predicates.add(cb.not(salle.get("id").in(sub)));
        }

        cq.select(salle).where(cb.and(predicates.toArray(new Predicate[0])));

        TypedQuery<Salle> query = entityManager.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    // Comptage total :
    @Override
    public long countRooms(String nom, Integer capaciteMin,
                           LocalDateTime debut, LocalDateTime fin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Salle> countRoot = countQuery.from(Salle.class);

        List<Predicate> predicates = new ArrayList<>();

        if (nom != null && !nom.isEmpty()) {
            predicates.add(cb.like(cb.lower(countRoot.<String>get("nom")), "%" + nom.toLowerCase() + "%"));
        }

        if (capaciteMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(countRoot.<Integer>get("capacite"), capaciteMin));
        }

        if (debut != null && fin != null) {
            Subquery<Long> sub = countQuery.subquery(Long.class);
            Root<Reservation> reservation = sub.from(Reservation.class);
            sub.select(reservation.get("salle").<Long>get("id"));
            sub.where(cb.and(
                    cb.lessThan(reservation.<LocalDateTime>get("debut"), fin),
                    cb.greaterThan(reservation.<LocalDateTime>get("fin"), debut)
            ));
            predicates.add(cb.not(countRoot.get("id").in(sub)));
        }

        countQuery.select(cb.count(countRoot)).where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
    // Injection de setEntityManager pour les tests en App
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}