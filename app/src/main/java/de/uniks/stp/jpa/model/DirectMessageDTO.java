package de.uniks.stp.jpa.model;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@DiscriminatorValue("direct_message")
@Table(name = "direct_message")
public class DirectMessageDTO extends MessageDTO {

    @Column(name = "receiver", updatable = false)
    @Type(type = "uuid-char")
    private UUID receiver;

    public UUID getReceiver() {
        return receiver;
    }

    public DirectMessageDTO setReceiver(UUID receiver) {
        this.receiver = receiver;
        return this;
    }
}
