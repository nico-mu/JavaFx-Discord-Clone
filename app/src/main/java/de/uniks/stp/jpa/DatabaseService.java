package de.uniks.stp.jpa;

import de.uniks.stp.controller.MainScreenController;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.jpa.model.MessageDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.*;

public class DatabaseService {
    private static EntityManagerFactory entityManagerFactory;

    public static void init() {
        if (Objects.isNull(entityManagerFactory)) {
            entityManagerFactory = Persistence.createEntityManagerFactory("de.uniks.stp.jpa");
        }

        createBackup();
    }

    private static void createBackup() {
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
        if (Objects.nonNull(entityManagerFactory)) {
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
        transaction.begin();

        final AccordSettingDTO result = entityManager.find(AccordSettingDTO.class, key);

        transaction.commit();
        entityManager.close();
        return result;
    }

    public static void saveDirectMessage(final DirectMessage message) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        entityManager.merge(
            new DirectMessageDTO()
                .setReceiver(message.getReceiver().getId())
                .setSender(message.getSender().getId())
                .setTimestamp(new Date(message.getTimestamp()))
                .setMessage(message.getMessage())
        );

        transaction.commit();
        entityManager.close();
    }

    public static List<DirectMessageDTO> getDirectMessages(User receiver) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        List<DirectMessageDTO> directMessageDTOList = new LinkedList<>();

        transaction.begin();

        CriteriaQuery<DirectMessageDTO> query = entityManager.getCriteriaBuilder().createQuery(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = query.from(DirectMessageDTO.class);

        query.where(entityManager.getCriteriaBuilder().equal(root.get("receiver"), receiver.getId()));
        List<?> resultList = entityManager.createQuery(query).getResultList();;

        transaction.commit();
        entityManager.close();

        for (Object o : resultList) {
            directMessageDTOList.add((DirectMessageDTO) o);
        }

        return directMessageDTOList;
    }

    public static void clearDirectMessages(User receiver) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaDelete<DirectMessageDTO> criteriaDelete = entityManager.getCriteriaBuilder().createCriteriaDelete(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = criteriaDelete.from(DirectMessageDTO.class);

        criteriaDelete.where(entityManager.getCriteriaBuilder().equal(root.get("receiver"), receiver.getId()));
        entityManager.createQuery(criteriaDelete).executeUpdate();

        transaction.commit();
        entityManager.close();
    }
}
