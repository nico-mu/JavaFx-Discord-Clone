package de.uniks.stp.jpa.model;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "API_INTEGRATION_SETTING")
public class ApiIntegrationSettingDTO {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "ID", updatable = false, nullable = false, unique = true)
    @ColumnDefault("random_uuid()")
    @Type(type = "uuid-char")
    private UUID id;

    @Column(name = "SERVICE_NAME", updatable = false)
    private String serviceName;

    @Column(name = "USERNAME", updatable = false)
    private String username;

    @Column(name = "REFRESH_TOKEN", updatable = false)
    private String refreshToken;

    public String getServiceName() {
        return serviceName;
    }

    public ApiIntegrationSettingDTO setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ApiIntegrationSettingDTO setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public ApiIntegrationSettingDTO setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
}
