package de.uniks.stp.jpa;

import de.uniks.stp.jpa.model.AccordSettingDTO;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Objects;

public class AppDatabaseService {
    protected EntityManagerFactory entityManagerFactory;

    public AppDatabaseService(boolean backup) {
        entityManagerFactory = Persistence.createEntityManagerFactory("de.uniks.stp.jpa");
        if(backup) {
            createBackup();
        }
    }

    private void createBackup() {
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
    public void stop() {
        if (Objects.nonNull(entityManagerFactory)) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

    public void saveAccordSetting(final AccordSettingKey key, final String value) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        entityManager.merge(
            new AccordSettingDTO().setKey(key).setValue(value)
        );

        transaction.commit();
        entityManager.close();
    }

    public AccordSettingDTO getAccordSetting(final AccordSettingKey key) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        final AccordSettingDTO result = entityManager.find(AccordSettingDTO.class, key);

        transaction.commit();
        entityManager.close();
        return result;
    }
}
