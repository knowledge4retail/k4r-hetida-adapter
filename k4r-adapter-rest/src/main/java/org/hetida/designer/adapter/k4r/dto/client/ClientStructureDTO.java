package org.hetida.designer.adapter.k4r.dto.client;

import lombok.Data;

@Data
public class ClientStructureDTO {

    private ClientThingNodesDTO thingNodes;
    private ClientSourcesDTO sources;
    private ClientSinksDTO sinks;
}
