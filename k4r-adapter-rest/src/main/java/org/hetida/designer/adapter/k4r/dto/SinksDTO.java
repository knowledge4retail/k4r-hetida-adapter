package org.hetida.designer.adapter.k4r.dto;

import lombok.Data;

import java.util.List;

@Data
public class SinksDTO {

    private int resultCount;
    private List<SinkDTO> sinks;
}
