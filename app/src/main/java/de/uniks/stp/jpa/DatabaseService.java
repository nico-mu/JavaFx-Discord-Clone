package de.uniks.stp.jpa;

import de.uniks.stp.jpa.model.*;
import de.uniks.stp.model.DirectMessage;
import javafx.util.Pair;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DatabaseService {
    private static EntityManagerFactory entityManagerFactory;

    public static void init(boolean backup) {
        if (Objects.isNull(entityManagerFactory)) {
            entityManagerFactory = Persistence.createEntityManagerFactory("de.uniks.stp.jpa");
        }

        if(backup) {
            createBackup();
        }
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
                .setSenderName(message.getSender().getName())
                .setTimestamp(new Date(message.getTimestamp()))
                .setMessage(message.getMessage())
        );

        transaction.commit();
        entityManager.close();
    }

    public static List<DirectMessageDTO> getConversation(String currentUserName, String receiverName) {
        Objects.requireNonNull(currentUserName);
        Objects.requireNonNull(receiverName);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        List<DirectMessageDTO> directMessageDTOList = new LinkedList<>();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DirectMessageDTO> query = criteriaBuilder.createQuery(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = query.from(DirectMessageDTO.class);

        query.where(
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("receiverName"), receiverName),
                    criteriaBuilder.equal(root.get("senderName"), currentUserName)
                ),
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("receiverName"), currentUserName),
                    criteriaBuilder.equal(root.get("senderName"), receiverName)
                )
            )
        );
        query.distinct(true);
        query.orderBy(criteriaBuilder.asc(root.get("timestamp")));
        List<?> resultList = entityManager.createQuery(query).getResultList();

        transaction.commit();
        entityManager.close();

        for (Object message : resultList) {
            directMessageDTOList.add((DirectMessageDTO) message);
        }

        return directMessageDTOList;
    }

    public static void clearConversation(String currentUserName, String receiverName) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<DirectMessageDTO> criteriaDelete = criteriaBuilder.createCriteriaDelete(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = criteriaDelete.from(DirectMessageDTO.class);

        criteriaDelete.where(
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("receiverName"), receiverName),
                    criteriaBuilder.equal(root.get("senderName"), currentUserName)
                ),
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("receiverName"), currentUserName),
                    criteriaBuilder.equal(root.get("senderName"), receiverName)
                )
            )
        );
        entityManager.createQuery(criteriaDelete).executeUpdate();

        transaction.commit();
        entityManager.close();
    }

    public static void clearAllConversations() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaDelete<DirectMessageDTO> criteriaDelete = entityManager.getCriteriaBuilder().createCriteriaDelete(DirectMessageDTO.class);
        criteriaDelete.from(DirectMessageDTO.class);
        entityManager.createQuery(criteriaDelete).executeUpdate();

        transaction.commit();
        entityManager.close();
    }

    public static List<Pair<String, String>> getAllConversationPartnerOf(String currentUserName) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        List<Pair<String, String>> chatPartnerList = new LinkedList<>();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DirectMessageDTO> query = criteriaBuilder.createQuery(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = query.from(DirectMessageDTO.class);

        query.where(
            criteriaBuilder.or(
                criteriaBuilder.equal(root.get("receiverName"), currentUserName),
                criteriaBuilder.equal(root.get("senderName"), currentUserName)));
        query.distinct(true);
        List<?> resultList = entityManager.createQuery(query).getResultList();;

        transaction.commit();
        entityManager.close();

        for (Object o : resultList) {
            DirectMessageDTO msg = (DirectMessageDTO) o;

            Pair<String, String> chatPartner;

            if (msg.getReceiverName().equals(currentUserName)) {
                chatPartner = new Pair<>(msg.getSender(), msg.getSenderName());
            } else {
                chatPartner = new Pair<>(msg.getReceiver(), msg.getReceiverName());
            }

            if (!chatPartnerList.contains(chatPartner) && Objects.nonNull(msg.getId()) && Objects.nonNull(msg.getReceiverName())) {
                chatPartnerList.add(chatPartner);
            }
        }

        return chatPartnerList;
    }

    public static void addMutedChannelId(String channelId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        entityManager.merge(new MutedChannelDTO().setChannelId(channelId));

        transaction.commit();
        entityManager.close();
    }

    public static void removeMutedChannelId(String channelId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        MutedChannelDTO mutedChannelDTO = entityManager.find(MutedChannelDTO.class, channelId);

        if(Objects.nonNull(mutedChannelDTO)) {
            entityManager.remove(mutedChannelDTO);
        }

        transaction.commit();
        entityManager.close();
    }

    public static boolean isChannelMuted(String channelId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        MutedChannelDTO result = entityManager.find(MutedChannelDTO.class, channelId);

        transaction.commit();
        entityManager.close();

        if(Objects.nonNull(result)) {
            return true;
        }
        return false;
    }

    public static void addMutedCategoryId(String categoryId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        entityManager.merge(new MutedCategoryDTO().setCategoryId(categoryId));

        transaction.commit();
        entityManager.close();
    }

    public static void removeMutedCategoryId(String categoryId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        MutedCategoryDTO mutedCategoryDTO = entityManager.find(MutedCategoryDTO.class, categoryId);

        if(Objects.nonNull(mutedCategoryDTO)) {
            entityManager.remove(mutedCategoryDTO);
        }

        transaction.commit();
        entityManager.close();
    }

    public static boolean isCategoryMuted(String categoryId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        MutedCategoryDTO result = entityManager.find(MutedCategoryDTO.class, categoryId);

        transaction.commit();
        entityManager.close();

        if(Objects.nonNull(result)) {
            return true;
        }
        return false;
    }

    public static void addMutedServerId(String serverId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        entityManager.merge(new MutedServerDTO().setServerId(serverId));

        transaction.commit();
        entityManager.close();
    }

    public static void removeMutedServerId(String serverId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        MutedServerDTO mutedServerDTO = entityManager.find(MutedServerDTO.class, serverId);

        if(Objects.nonNull(mutedServerDTO)) {
            entityManager.remove(mutedServerDTO);
        }

        transaction.commit();
        entityManager.close();
    }

    public static boolean isServerMuted(String serverId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        MutedServerDTO result = entityManager.find(MutedServerDTO.class, serverId);

        transaction.commit();
        entityManager.close();

        if(Objects.nonNull(result)) {
            return true;
        }
        return false;
    }
}
