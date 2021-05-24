package de.uniks.stp.jpa.model;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "MESSAGE_TYPE", discriminatorType = DiscriminatorType.STRING)
@Table(name = "MESSAGE")
public class MessageDTO {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "ID", updatable = false, nullable = false, unique = true)
    @ColumnDefault("random_uuid()")
    @Type(type = "uuid-char")
    private UUID id;

    @Column(name = "SENDER", updatable = false)
    private String sender;

    @Column(name = "MESSAGE", updatable = false)
    private String message;

    @Column(name = "TIMESTAMP", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public UUID getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public MessageDTO setSender(String sender) {
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
