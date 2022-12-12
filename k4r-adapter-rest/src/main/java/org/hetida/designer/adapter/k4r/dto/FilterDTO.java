package org.hetida.designer.adapter.k4r.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.hetida.designer.adapter.k4r.dto.TimestampFilterDTO;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "dataType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TimestampFilterDTO.class, name = "timestamp")
})
public class FilterDTO {
    private String name;
    private boolean required;
}
