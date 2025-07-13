package dev.hyperapi.runtime.core.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class HyperEntity extends PanacheEntity {

  private String createdBy;

  private String updatedBy;

  @Temporal(TemporalType.TIMESTAMP)
  private Date createdOn;

  @Column(name = "UPDATED_ON")
  @Temporal(TemporalType.TIMESTAMP)
  private Date updatedOn;

  @PrePersist
  public void prePersist() {
    createdOn = new Date();
    updatedOn = new Date();
  }

  @PreUpdate
  public void preUpdate() {
    updatedOn = new Date();
  }

}
