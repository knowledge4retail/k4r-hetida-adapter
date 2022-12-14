package org.hetida.designer.adapter.k4r.dto.client;

//import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.hetida.designer.adapter.k4r.enums.DataType;

@Data
public class ClientSinkDTO {
    private String id;
    private String thingNodeId;
    private String name;
    private DataType type;
    //@JsonInclude(JsonInclude.Include.NON_NULL)
    //private String metadataKey;
    private boolean visible = true;
    private String path;
}
