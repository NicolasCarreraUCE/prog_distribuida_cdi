package com.distribuida.servicios;

import com.distribuida.db.Persona;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class ServicioPersonaImpl implements ServicioPersona {
    @Inject
    EntityManager em;

    @Override
    public List<Persona> findAll() {
        return em.createQuery("select o from Persona o")
                .getResultList();
    }

    public Persona findById(Integer id) {

        return em.find(Persona.class, id);
    }

    @Override
    public boolean borrar(Integer id) {
        var tx = em.getTransaction();
        try {
            tx.begin();

            Persona persona = findById(id);

            if (persona != null) {
                // Si la persona existe, elimínala
                em.remove(persona);
                tx.commit(); // Completar la transacción si la eliminación fue exitosa
                return true; // Retornar true indicando que la operación fue exitosa
            } else {
                System.out.println("La persona con ID " + id + " no fue encontrada.");
                tx.rollback(); // Revertir la transacción si la persona no fue encontrada
                return false; // Retornar false indicando que la operación no fue exitosa
            }
        } catch (Exception ex) {
            tx.rollback(); // Revertir la transacción en caso de error
            ex.printStackTrace(); // Opcional: Imprimir la excepción para diagnóstico
            return false; // Retornar false indicando que la operación no fue exitosa
        }
    }


    @Override
    public void actualizar(Persona persona) {
        var tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(persona);

            tx.commit();
        } catch(Exception ex) {
            tx.rollback();

        }
    }

    public void insert(Persona p) {
        var tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(p);
            tx.commit();
        }
        catch(Exception ex) {
            tx.rollback();
        }
    }
}
