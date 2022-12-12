package org.hetida.designer.adapter.k4r.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class TimeSeriesCriteriaDto {
    private Instant fromInstant;
    private Instant toInstant;
    private String[] channel_ids;
}
