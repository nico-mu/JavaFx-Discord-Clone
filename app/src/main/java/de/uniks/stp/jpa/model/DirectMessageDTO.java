package de.uniks.stp.jpa.model;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@DiscriminatorValue("DIRECT_MESSAGE")
@Table(name = "DIRECT_MESSAGE")
public class DirectMessageDTO extends MessageDTO {

    @Column(name = "RECEIVER", updatable = false)
    private String receiver;

    @Column(name = "RECEIVER_NAME", updatable = false)
    private String receiverName;

    public String getReceiver() {
        return receiver;
    }

    public DirectMessageDTO setReceiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public DirectMessageDTO setReceiverName(String receiverName) {
        this.receiverName = receiverName;
        return this;
    }
}
