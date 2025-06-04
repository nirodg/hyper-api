package dev.hyperapi.runtime.core.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class BaseDTO {

    private String guid;

    private String createdBy;

    private String updatedBy;

    private Date createdOn;

    private Date updatedOn;

}
