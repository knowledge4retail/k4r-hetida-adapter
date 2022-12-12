package org.hetida.designer.adapter.k4r.dto;

import lombok.Data;

import java.util.List;

@Data
public class SourcesDTO {

    private int resultCount;
    private List<SourceDTO> sources;
}
