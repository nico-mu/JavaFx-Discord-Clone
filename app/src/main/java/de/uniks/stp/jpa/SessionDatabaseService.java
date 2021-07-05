package de.uniks.stp.jpa;

import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.jpa.model.MutedCategoryDTO;
import de.uniks.stp.jpa.model.MutedChannelDTO;
import de.uniks.stp.jpa.model.MutedServerDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import javafx.util.Pair;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SessionDatabaseService extends AppDatabaseService {

    private User currentUser;

    @Inject
    public SessionDatabaseService(@Named("currentUser") User currentUser) {
        super(false);
        this.currentUser = currentUser;
    }

    public void saveDirectMessage(final DirectMessage message) {
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

    public final List<DirectMessageDTO> getConversation(String currentUserName, String receiverName) {
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

    public void clearConversation(String currentUserName, String receiverName) {
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

    public void clearAllConversations() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaDelete<DirectMessageDTO> criteriaDelete = entityManager.getCriteriaBuilder().createCriteriaDelete(DirectMessageDTO.class);
        criteriaDelete.from(DirectMessageDTO.class);
        entityManager.createQuery(criteriaDelete).executeUpdate();

        transaction.commit();
        entityManager.close();
    }

    public final List<Pair<String, String>> getAllConversationPartnerOf(String currentUserName) {
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

    public void addMutedChannelId(String channelId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        entityManager.merge(new MutedChannelDTO().setChannelId(channelId));

        transaction.commit();
        entityManager.close();
    }

    public void removeMutedChannelId(String channelId) {
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

    public boolean isChannelMuted(String channelId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        MutedChannelDTO result = entityManager.find(MutedChannelDTO.class, channelId);

        transaction.commit();
        entityManager.close();

        return Objects.nonNull(result);
    }

    public void addMutedCategoryId(String categoryId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        entityManager.merge(new MutedCategoryDTO().setCategoryId(categoryId));

        transaction.commit();
        entityManager.close();
    }

    public void removeMutedCategoryId(String categoryId) {
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

    public boolean isCategoryMuted(String categoryId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        MutedCategoryDTO result = entityManager.find(MutedCategoryDTO.class, categoryId);

        transaction.commit();
        entityManager.close();

        return Objects.nonNull(result);
    }

    public void addMutedServerId(String serverId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        entityManager.merge(new MutedServerDTO().setServerId(serverId));

        transaction.commit();
        entityManager.close();
    }

    public void removeMutedServerId(String serverId) {
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

    public boolean isServerMuted(String serverId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        MutedServerDTO result = entityManager.find(MutedServerDTO.class, serverId);

        transaction.commit();
        entityManager.close();

        return Objects.nonNull(result);
    }
}
