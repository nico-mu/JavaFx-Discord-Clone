package de.uniks.stp.jpa;

import de.uniks.stp.jpa.model.AccordSettingDTO;

import javax.persistence.*;
import java.util.Objects;

public class DatabaseService {

    private static EntityManagerFactory entityManagerFactory;

    public static void init() {
        if (Objects.isNull(entityManagerFactory)) {
            entityManagerFactory = Persistence.createEntityManagerFactory( "de.uniks.stp.jpa" );
        }

        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        entityManager.createNativeQuery("BACKUP TO './db/backup_accord.zip';").executeUpdate();

        transaction.commit();
        entityManager.close();
    }

    /**
     * Stops the service. You must call init() before it can be used again
     */
    public static void stop() {
        if(Objects.nonNull(entityManagerFactory)) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

    public static void saveAccordSetting(final AccordSettingKey key, final String value) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        entityManager.merge(
            new AccordSettingDTO().setKey(key).setValue(value)
        );

        transaction.commit();
        entityManager.close();
    }

    public static AccordSettingDTO getAccordSetting(final AccordSettingKey key) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        entityManager.getTransaction().begin();

        final AccordSettingDTO result = entityManager.find( AccordSettingDTO.class, key );

        transaction.commit();
        entityManager.close();
        return result;
    }
}
