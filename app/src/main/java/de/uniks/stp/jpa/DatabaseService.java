package de.uniks.stp.jpa;

import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import javafx.util.Pair;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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
                .setReceiverName(message.getReceiver().getName())
                .setSender(message.getSender().getId())
                .setTimestamp(new Date(message.getTimestamp()))
                .setMessage(message.getMessage())
        );

        transaction.commit();
        entityManager.close();
    }

    public static List<DirectMessageDTO> getDirectMessages(String receiverId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        List<DirectMessageDTO> directMessageDTOList = new LinkedList<>();

        transaction.begin();

        CriteriaQuery<DirectMessageDTO> query = entityManager.getCriteriaBuilder().createQuery(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = query.from(DirectMessageDTO.class);

        query.where(entityManager.getCriteriaBuilder().equal(root.get("receiver"), receiverId));
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

    public static List<Pair<String, String>> getDirectMessageReceiver() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        List<Pair<String, String>> receiverList = new LinkedList<>();

        transaction.begin();

        CriteriaQuery<DirectMessageDTO> query = entityManager.getCriteriaBuilder().createQuery(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = query.from(DirectMessageDTO.class);

        query.select(root);
        query.distinct(true);
        List<?> resultList = entityManager.createQuery(query).getResultList();;

        transaction.commit();
        entityManager.close();

        for (Object o : resultList) {
            DirectMessageDTO msg = (DirectMessageDTO) o;

            Pair<String, String> receiver = new Pair<>(msg.getReceiver(), msg.getReceiverName());

            if (!receiverList.contains(receiver) && Objects.nonNull(msg.getId()) && Objects.nonNull(msg.getReceiverName())) {
                receiverList.add(receiver);
            }
        }

        return receiverList;
    }
}
