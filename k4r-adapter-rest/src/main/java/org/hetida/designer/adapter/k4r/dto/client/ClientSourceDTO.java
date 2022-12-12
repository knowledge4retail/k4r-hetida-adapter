package org.hetida.designer.adapter.k4r.dto.client;

//import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.hetida.designer.adapter.k4r.dto.FilterDTO;
import org.hetida.designer.adapter.k4r.enums.DataType;

import java.util.Map;

@Data
public class ClientSourceDTO {
    private String id;
    private String thingNodeId;
    private String name;
    private DataType type;
    //@JsonInclude(JsonInclude.Include.NON_NULL)
    //private String metadataKey;
    private boolean visible = true;
    private String path;
    private Map<String, FilterDTO> filters;
}
