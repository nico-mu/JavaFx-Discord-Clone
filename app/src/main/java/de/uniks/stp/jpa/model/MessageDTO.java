package de.uniks.stp.jpa.model;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "message_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "message")
public class MessageDTO {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    @ColumnDefault("random_uuid()")
    @Type(type = "uuid-char")
    private UUID id;

    @Column(name = "sender", updatable = false)
    @Type(type = "uuid-char")
    private UUID sender;

    @Column(name = "message", updatable = false)
    private String message;

    @Column(name = "timestamp", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public UUID getId() {
        return id;
    }

    public UUID getSender() {
        return sender;
    }

    public MessageDTO setSender(UUID sender) {
        this.sender = sender;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MessageDTO setMessage(String message) {
        this.message = message;
        return this;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public MessageDTO setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
