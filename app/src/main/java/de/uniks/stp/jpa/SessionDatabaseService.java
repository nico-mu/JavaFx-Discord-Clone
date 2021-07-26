package de.uniks.stp.jpa;

import de.uniks.stp.jpa.model.*;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import javafx.util.Pair;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SessionDatabaseService extends AppDatabaseService {

    private final User currentUser;

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
                .setOwnerName(currentUser.getName())
        );

        transaction.commit();
        entityManager.close();
    }

    public ApiIntegrationSettingDTO getApiIntegrationSetting(String serviceName) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ApiIntegrationSettingDTO> query = criteriaBuilder.createQuery(ApiIntegrationSettingDTO.class);
        Root<ApiIntegrationSettingDTO> root = query.from(ApiIntegrationSettingDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("username"), currentUser.getName()),
                criteriaBuilder.equal(root.get("serviceName"), serviceName)
            )
        );

        ApiIntegrationSettingDTO result = null;

        try {
            result = entityManager.createQuery(query).getSingleResult();
        }
        catch (NoResultException ignored) {}

        transaction.commit();
        entityManager.close();

        return result;
    }

    public void addApiIntegrationSetting(String serviceName, String refreshToken) {
        ApiIntegrationSettingDTO apiIntegrationSettingDTO = getApiIntegrationSetting(serviceName);

        if(Objects.isNull(apiIntegrationSettingDTO)) {
            apiIntegrationSettingDTO = new ApiIntegrationSettingDTO().setServiceName(serviceName)
                .setUsername(currentUser.getName())
                .setRefreshToken(refreshToken);
        }
        else {
            apiIntegrationSettingDTO.setRefreshToken(refreshToken);
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        entityManager.merge(apiIntegrationSettingDTO);

        transaction.commit();
        entityManager.close();
    }

    public void deleteApiIntegrationSetting(String serviceName) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ApiIntegrationSettingDTO> query = criteriaBuilder.createQuery(ApiIntegrationSettingDTO.class);
        Root<ApiIntegrationSettingDTO> root = query.from(ApiIntegrationSettingDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("username"), currentUser.getName()),
                criteriaBuilder.equal(root.get("serviceName"), serviceName)
            )
        );

        ApiIntegrationSettingDTO result = null;

        try {
            result = entityManager.createQuery(query).getSingleResult();
        }
        catch (NoResultException ignored) {}

        if(Objects.nonNull(result)) {
            entityManager.remove(result);
        }

        transaction.commit();
        entityManager.close();
    }

    public final List<DirectMessageDTO> getConversation(String receiverName) {
        Objects.requireNonNull(receiverName);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DirectMessageDTO> query = criteriaBuilder.createQuery(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = query.from(DirectMessageDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.or(
                    criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("receiverName"), receiverName),
                        criteriaBuilder.equal(root.get("senderName"), currentUser.getName())
                    ),
                    criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("receiverName"), currentUser.getName()),
                        criteriaBuilder.equal(root.get("senderName"), receiverName)
                    )
                ),
                criteriaBuilder.equal(root.get("ownerName"), currentUser.getName())
            )

        );
        query.distinct(true);
        query.orderBy(criteriaBuilder.asc(root.get("timestamp")));
        List<DirectMessageDTO> resultList = entityManager.createQuery(query).getResultList();

        transaction.commit();
        entityManager.close();

        return resultList;
    }

    public void clearConversation(String receiverName) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<DirectMessageDTO> criteriaDelete = criteriaBuilder.createCriteriaDelete(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = criteriaDelete.from(DirectMessageDTO.class);

        criteriaDelete.where(
            criteriaBuilder.and(
                criteriaBuilder.or(
                    criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("receiverName"), receiverName),
                        criteriaBuilder.equal(root.get("senderName"), currentUser.getName())
                    ),
                    criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("receiverName"), currentUser.getName()),
                        criteriaBuilder.equal(root.get("senderName"), receiverName)
                    )
                ),
                criteriaBuilder.equal(root.get("ownerName"), currentUser.getName())
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

    public final List<Pair<String, String>> getAllConversationPartners() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        List<Pair<String, String>> chatPartnerList = new LinkedList<>();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DirectMessageDTO> query = criteriaBuilder.createQuery(DirectMessageDTO.class);
        Root<DirectMessageDTO> root = query.from(DirectMessageDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("receiverName"), currentUser.getName()),
                    criteriaBuilder.equal(root.get("senderName"), currentUser.getName())
                ),
                criteriaBuilder.equal(root.get("ownerName"), currentUser.getName())
            )
        );
        query.distinct(true);
        List<DirectMessageDTO> resultList = entityManager.createQuery(query).getResultList();

        transaction.commit();
        entityManager.close();

        for (DirectMessageDTO msg: resultList) {
            Pair<String, String> chatPartner;

            if (msg.getReceiverName().equals(currentUser.getName())) {
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

        entityManager.merge(new MutedChannelDTO().setChannelId(channelId).setUsername(currentUser.getName()));

        transaction.commit();
        entityManager.close();
    }

    public void removeMutedChannelId(String channelId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MutedChannelDTO> query = criteriaBuilder.createQuery(MutedChannelDTO.class);
        Root<MutedChannelDTO> root = query.from(MutedChannelDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("channelId"), channelId),
                criteriaBuilder.equal(root.get("username"), currentUser.getName())
            )
        );

        MutedChannelDTO result = null;

        try {
            result = entityManager.createQuery(query).getSingleResult();
        }
        catch (NoResultException ignored) {}

        if(Objects.nonNull(result)) {
            entityManager.remove(result);
        }

        transaction.commit();
        entityManager.close();
    }

    public boolean isChannelMuted(String channelId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MutedChannelDTO> query = criteriaBuilder.createQuery(MutedChannelDTO.class);
        Root<MutedChannelDTO> root = query.from(MutedChannelDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("channelId"), channelId),
                criteriaBuilder.equal(root.get("username"), currentUser.getName())
            )
        );

        MutedChannelDTO result = null;

        try {
            result = entityManager.createQuery(query).getSingleResult();
        }
        catch (NoResultException ignored) {}

        transaction.commit();
        entityManager.close();

        return Objects.nonNull(result);
    }

    public void addMutedCategoryId(String categoryId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        entityManager.merge(new MutedCategoryDTO().setCategoryId(categoryId).setUsername(currentUser.getName()));

        transaction.commit();
        entityManager.close();
    }

    public void removeMutedCategoryId(String categoryId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MutedCategoryDTO> query = criteriaBuilder.createQuery(MutedCategoryDTO.class);
        Root<MutedCategoryDTO> root = query.from(MutedCategoryDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("categoryId"), categoryId),
                criteriaBuilder.equal(root.get("username"), currentUser.getName())
            )
        );

        MutedCategoryDTO result = null;

        try {
            result = entityManager.createQuery(query).getSingleResult();
        }
        catch (NoResultException ignored) {}

        if(Objects.nonNull(result)) {
            entityManager.remove(result);
        }

        transaction.commit();
        entityManager.close();
    }

    public boolean isCategoryMuted(String categoryId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MutedCategoryDTO> query = criteriaBuilder.createQuery(MutedCategoryDTO.class);
        Root<MutedCategoryDTO> root = query.from(MutedCategoryDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("categoryId"), categoryId),
                criteriaBuilder.equal(root.get("username"), currentUser.getName())
            )
        );

        MutedCategoryDTO result = null;

        try {
            result = entityManager.createQuery(query).getSingleResult();
        }
        catch (NoResultException ignored) {}

        transaction.commit();
        entityManager.close();

        return Objects.nonNull(result);
    }

    public void addMutedServerId(String serverId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        entityManager.merge(new MutedServerDTO().setServerId(serverId).setUsername(currentUser.getName()));

        transaction.commit();
        entityManager.close();
    }

    public void removeMutedServerId(String serverId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MutedServerDTO> query = criteriaBuilder.createQuery(MutedServerDTO.class);
        Root<MutedServerDTO> root = query.from(MutedServerDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("serverId"), serverId),
                criteriaBuilder.equal(root.get("username"), currentUser.getName())
            )
        );

        MutedServerDTO result = null;

        try {
            result = entityManager.createQuery(query).getSingleResult();
        }
        catch (NoResultException ignored) {}

        if(Objects.nonNull(result)) {
            entityManager.remove(result);
        }

        transaction.commit();
        entityManager.close();
    }

    public boolean isServerMuted(String serverId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MutedServerDTO> query = criteriaBuilder.createQuery(MutedServerDTO.class);
        Root<MutedServerDTO> root = query.from(MutedServerDTO.class);

        query.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("serverId"), serverId),
                criteriaBuilder.equal(root.get("username"), currentUser.getName())
            )
        );

        MutedServerDTO result = null;

        try {
            result = entityManager.createQuery(query).getSingleResult();
        }
        catch (NoResultException ignored) {}

        transaction.commit();
        entityManager.close();

        return Objects.nonNull(result);
    }
}
