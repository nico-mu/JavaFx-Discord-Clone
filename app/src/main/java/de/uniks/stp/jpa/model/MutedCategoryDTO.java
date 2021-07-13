package de.uniks.stp.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MUTED_CATEGORIES")
public class MutedCategoryDTO {
    @Id
    @Column(name = "CATEGORY_ID", nullable = false, unique = true)
    private String categoryId;

    @Column(name = "USERNAME", updatable = false)
    private String username;

    public String getCategoryId() {
        return categoryId;
    }

    public MutedCategoryDTO setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public MutedCategoryDTO setUsername(String userName) {
        this.username = userName;
        return this;
    }
}
