package org.hetida.designer.adapter.k4r.dto;


import lombok.Data;

import java.util.List;

@Data
public class StructureDTO {

    private String id;
    private String name;
    private List<ThingNodeDTO> thingNodes;
    private List<SourceDTO> sources;
    private List<SinkDTO> sinks;
}
