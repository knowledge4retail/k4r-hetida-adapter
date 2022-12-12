package org.hetida.designer.adapter.k4r.converter;

//import org.hetida.designer.adapter.k4r.dto.client.ClientSourceDTO;
import org.hetida.designer.adapter.k4r.dto.client.ClientSourcesDTO;
import org.hetida.designer.adapter.k4r.dto.client.ClientStructureDTO;
import org.hetida.designer.adapter.k4r.dto.SourceDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SourceDataConverter {
    public List<SourceDTO> convertClientStructureToSources(ClientStructureDTO clientStructureDTO) {
        return convertClientSourcesToSources(clientStructureDTO.getSources());
    }

    public List<SourceDTO> convertClientSourcesToSources(ClientSourcesDTO sources) {
        List<SourceDTO> sourceDTOS = new ArrayList<>();
        sources.getSources().forEach(s -> {
            SourceDTO sourceDTO = new SourceDTO();
            sourceDTO.setId(s.getId());
            sourceDTO.setThingNodeId(s.getThingNodeId());
            sourceDTO.setName(s.getName());
            sourceDTO.setType(s.getType().getName());
            //sourceDTO.setMetadataKey(determineMetadataKey(s));
            sourceDTO.setPath(s.getPath());
            sourceDTO.setVisible(s.isVisible());
            sourceDTO.setFilters(s.getFilters());
            sourceDTOS.add(sourceDTO);
        });
        return sourceDTOS;
    }

    /*private String determineMetadataKey(ClientSourceDTO dto) {
        if (dto.getType().getName().startsWith("metadata")) {
            return dto.getMetadataKey();
        }
        return null;
    }*/
}
