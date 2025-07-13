package dev.hyperapi.runtime.core.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class HyperDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String createdBy;

    private String updatedBy;

    private Date createdOn;

    private Date updatedOn;

}
