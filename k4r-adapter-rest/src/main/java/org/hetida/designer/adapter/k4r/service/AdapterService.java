package org.hetida.designer.adapter.k4r.service;

import org.hetida.designer.adapter.k4r.dto.*;

import java.io.OutputStream;

public interface AdapterService {

    AdapterInfoDTO getAdapterInfo();

    StructureDTO getStructure(final String parentId);
    StructureDTO getCachedStructure(String parentId);

    void startDataframeStreaming(final OutputStream outputStream, final String id);

    SourcesDTO getFilteredSources(final String filter);
    SourceDTO getSource(final String id);
    SinksDTO getFilteredSinks(final String filter);
    //SinkDTO getSink(final String id);
    ThingNodeDTO getThingNode(final String id);

    /*List<MetaDataRequestResponseDTO> getSinkMetaData(final String id, String key);
    List<MetaDataRequestResponseDTO> getThingNodeMetadata(final String id, String key);
    void startTimeseriesStreaming(final OutputStream outputStream, final String[] timeseriesIds, final TimeSeriesCriteriaDto timeSeriesCriteriaDto);
    List<MetaDataRequestResponseDTO> getSourceMetaData(final String id, final String key);*/
}
