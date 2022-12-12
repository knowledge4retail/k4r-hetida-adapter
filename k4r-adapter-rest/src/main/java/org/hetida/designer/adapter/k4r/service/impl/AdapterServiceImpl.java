package org.hetida.designer.adapter.k4r.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.hetida.designer.adapter.k4r.client.AdapterClient;
import org.hetida.designer.adapter.k4r.converter.*;
import org.hetida.designer.adapter.k4r.dto.*;
import org.hetida.designer.adapter.k4r.dto.client.ClientStructureDTO;
//import org.hetida.designer.adapter.k4r.dto.client.ClientThingNodeDTO;
//import org.hetida.designer.adapter.k4r.dto.client.ClientTimeseriesDTO;
import org.hetida.designer.adapter.k4r.service.AdapterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import springfox.documentation.annotations.Cacheable;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import static org.hetida.designer.demo.adapter.util.AdapterUtil.getIdFromString;
//import static org.hetida.designer.demo.adapter.util.AdapterUtil.getKeyFromString;

@Service
@Log4j2
public class AdapterServiceImpl implements AdapterService {

    private final String LINE_DELIMITER = "\n";

    private final AdapterClient client;
    /*private final MetaDataConverter metaDataConverter;
    private final TimeseriesConverter timeseriesConverter;*/
    private final SourceDataConverter sourceDataConverter;
    private final SinkDataConverter sinkDataConverter;
    private final ThingNodeDataConverter thingNodeDataConverter;
    private final ObjectMapper objectMapper;

    @Value("${adapter.k4r.id}")
    private String adapterId;

    @Value("${adapter.k4r.name}")
    private String adapterName;

    @Value("${adapter.k4r.version}")
    private String adapterVersion;

    public AdapterServiceImpl(AdapterClient client,
                              /*MetaDataConverter metaDataConverter,
                              TimeseriesConverter timeseriesConverter,*/
                              SourceDataConverter sourceDataConverter,
                              SinkDataConverter sinkDataConverter,
                              ThingNodeDataConverter thingNodeDataConverter,
                              ObjectMapper objectMapper) {
        this.client = client;
        /*this.metaDataConverter = metaDataConverter;
        this.timeseriesConverter = timeseriesConverter;*/
        this.sourceDataConverter = sourceDataConverter;
        this.sinkDataConverter = sinkDataConverter;
        this.thingNodeDataConverter = thingNodeDataConverter;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdapterInfoDTO getAdapterInfo() {
        AdapterInfoDTO adapterInfoDTO = new AdapterInfoDTO();
        adapterInfoDTO.setId(adapterId);
        adapterInfoDTO.setName(adapterName);
        adapterInfoDTO.setVersion(adapterVersion);
        return adapterInfoDTO;
    }

    @Override
    public StructureDTO getStructure(final String parentId) {
        StructureDTO structureDTO = getCachedStructure(parentId);
        structureDTO.setThingNodes(filterThingNodes(structureDTO, parentId));
        structureDTO.setSources(filterSources(structureDTO, parentId));
        structureDTO.setSinks(filterSinks(structureDTO, parentId));
        return structureDTO;
    }

    @Override
    @Cacheable("structure")
    public StructureDTO getCachedStructure(String parentId) {
        ClientStructureDTO clientStructureDTO = client.getStructure(parentId);
        return thingNodeDataConverter.convertToStructureDto(clientStructureDTO);
    }

    private List<ThingNodeDTO> filterThingNodes(final StructureDTO structureDTO, final String parentId) {

        if (parentId != null) {
            // filter 1st level
            return structureDTO.getThingNodes().stream().filter(tndto -> parentId.equals(tndto.getParentId())).collect(Collectors.toList());
        } else {
            // only root
            return structureDTO.getThingNodes().stream().filter(tndto -> tndto.getParentId() == null).collect(Collectors.toList());
        }
    }

    private List<SourceDTO> filterSources(final StructureDTO structureDTO, final String parentId) {

        if (parentId != null) {
            return structureDTO.getSources().stream().filter(
                    sourceDTO -> parentId.equals(sourceDTO.getThingNodeId())
            ).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private List<SinkDTO> filterSinks(final StructureDTO structureDTO, final String parentId) {

        if (parentId != null) {
            return structureDTO.getSinks().stream().filter(
                    sinkDTO -> parentId.equals(sinkDTO.getThingNodeId())
            ).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


    @Override
    public void startDataframeStreaming(final OutputStream outputStream, final String id) {

        final String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        final String threadName = Thread.currentThread().getName();
        final String threadGroupName = Thread.currentThread().getThreadGroup().getName();
        log.info("Executing Async-Dataframe-Task -> methodName: {}, threadName: {}, threadGroupName: {}.", methodName, threadName, threadGroupName);
        log.info("Streaming dataframes by id: [{}]", id);

        final AtomicInteger counter = new AtomicInteger(0);
        final long start = System.currentTimeMillis();

        final TimeSeriesCriteriaDto timeSeriesCriteriaDto = TimeSeriesCriteriaDto.builder().fromInstant(Instant.parse("2020-01-15T07:30:45.000Z"))
                .toInstant(Instant.now()).build();
        try (final Stream<LinkedHashMap> stream = client.getDataframe(id, timeSeriesCriteriaDto).stream()) {
            stream.forEach(dataframe -> {
                try {
                    outputStream.write(objectMapper.writeValueAsBytes(dataframe));
                    outputStream.write(LINE_DELIMITER.getBytes());
                    counter.incrementAndGet();

                } catch (final IOException e) {
                    log.error("Error while executing Async-Dataframe-Task -> methodName: {}, threadName: {}, threadGroupName: {}."
                            , methodName, threadName, threadGroupName);
                    log.error("Error while streaming dataframe by criteria: [{}]", timeSeriesCriteriaDto);
                }
            });
        }
        log.info("Streaming of {} dataframes by criteria: [{}] processed successfully, Task: [{}] took {}ms.", counter.get(),
                timeSeriesCriteriaDto, threadName, (System.currentTimeMillis() - start));
    }

    @Override
    public SourcesDTO getFilteredSources(final String filter) {

        SourcesDTO sourcesDTO = new SourcesDTO();
        ClientStructureDTO metaData = client.getStructure(filter);
        List<SourceDTO> sources = sourceDataConverter.convertClientStructureToSources(metaData);
        sourcesDTO.setResultCount(sources.size());
        sourcesDTO.setSources(sources);

        return sourcesDTO;
    }

    @Override
    public SourceDTO getSource(final String id) {
        return getFilteredSources(null).getSources().stream()
                .filter(source -> source.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public SinksDTO getFilteredSinks(final String filter) {

        SinksDTO sinksDTO = new SinksDTO();
        ClientStructureDTO metaData = client.getStructure(filter);
        List<SinkDTO> sinks = sinkDataConverter.convertClientStructureToSinks(metaData);
        sinksDTO.setResultCount(sinks.size());
        sinksDTO.setSinks(sinks);

        return sinksDTO;
    }

    /*@Override
    public List<MetaDataRequestResponseDTO> getSourceMetaData(final String id, final String key) {

        Set<String> availableSources = client.getSources().stream()
                .map(s -> getIdFromString(s.getId()))
                .filter(s -> s.equals(id))
                .collect(Collectors.toSet());

        List<MetaDataDTO> metaDataDTOS = client.getMetaData().getMetadata().stream()
                .filter(m -> availableSources.contains(getIdFromString(m.getKey())))
                .collect(Collectors.toList());

        if (key != null) {
            metaDataDTOS = metaDataDTOS.stream()
                    .filter(m -> key.endsWith(getKeyFromString(m.getKey()))).collect(Collectors.toList());
        }
        return metaDataConverter.convertToMetaDataRequestResponseDTOs(metaDataDTOS);
    }*/

    /*@Override
    public SinkDTO getSink(final String id) {
        return getFilteredSinks(null).getSinks().stream()
                .filter(sink -> sink.getId().equals(id))
                .findFirst()
                .orElse(null);
    }*/

    @Override
    public ThingNodeDTO getThingNode(final String id) {
        return getCachedStructure(id).getThingNodes().stream()
                .filter(thingNodeDTO -> thingNodeDTO.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /*@Override
    public List<MetaDataRequestResponseDTO> getSinkMetaData(final String id, final String key) {
        Set<String> availableSinks = org.hetida.designer.adapter.k4r.client.getSinks().stream()
                .map(s -> getIdFromString(s.getId()))
                .filter(s -> s.equals(id))
                .collect(Collectors.toSet());

        List<MetaDataDTO> metaDataDTOS = org.hetida.designer.adapter.k4r.client.getMetaData().getMetadata().stream()
                .filter(m -> availableSinks.contains(getIdFromString(m.getKey())) && m.isSink())
                .collect(Collectors.toList());

        if (key != null) {
            metaDataDTOS = metaDataDTOS.stream()
                    .filter(m -> key.endsWith(getKeyFromString(m.getKey()))).collect(Collectors.toList());
        }
        return metaDataConverter.convertToMetaDataRequestResponseDTOs(metaDataDTOS);
    }

    @Override
    public List<MetaDataRequestResponseDTO> getThingNodeMetadata(final String id, final String key) {
        // check if metadata is a thingNode...
        Set<String> availableThingNodes = org.hetida.designer.adapter.k4r.client.getStructure().getThingNodes().getThingNodes().stream()
                .map(ClientThingNodeDTO::getId)
                .filter(nodeId -> nodeId.equals(id)).collect(Collectors.toSet());

        List<MetaDataDTO> metaDataDTOS = org.hetida.designer.adapter.k4r.client.getMetaData().getMetadata().stream()
                    .filter(m -> availableThingNodes.contains(getIdFromString(m.getKey())))
                    .collect(Collectors.toList());

        // ... or is an attribute of a thingNode
        if (metaDataDTOS.isEmpty()) {
            metaDataDTOS = org.hetida.designer.adapter.k4r.client.getMetaData().getMetadata().stream()
                    .filter(m -> id.equals(getIdFromString(m.getKey())) && key.equals(getKeyFromString(m.getKey())))
                    .collect(Collectors.toList());
        }

        // filter by key
        if (key != null) {
            metaDataDTOS = metaDataDTOS.stream()
                    .filter(m -> key.endsWith(getKeyFromString(m.getKey()))).collect(Collectors.toList());
        }
        return metaDataConverter.convertToMetaDataRequestResponseDTOs(metaDataDTOS);
    }


    @Override
    public void startTimeseriesStreaming(final OutputStream outputStream, final String[] timeseriesIds, final TimeSeriesCriteriaDto timeSeriesCriteriaDto) {

        List<String> channelIds = new ArrayList<>();
        String callType = "";
        for (String value : timeseriesIds) {

            String[] timeseries = value.split("-");
            if (timeseries.length > 1) {
                channelIds.add(timeseries[4]);
                callType = timeseries[5];
            } else {
                channelIds.add("");
                callType = "MEASUREMENTS";
            }
        }
        timeSeriesCriteriaDto.setChannel_ids(channelIds.toArray(new String[0]));

        final String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        final String threadName = Thread.currentThread().getName();
        final String threadGroupName = Thread.currentThread().getThreadGroup().getName();
        log.info("Executing Async-TimeseriesTask -> methodName: {}, threadName: {}, threadGroupName: {}.", methodName, threadName,
                threadGroupName);
        log.info("Streaming timeseries by id: [{}]", timeseriesIds);

        final AtomicInteger counter = new AtomicInteger(0);
        final long start = System.currentTimeMillis();
        List<ClientTimeseriesDTO> timeseries = org.hetida.designer.adapter.k4r.client.getTimeseries(callType, timeSeriesCriteriaDto);
        try (final Stream<TimeseriesDTO> stream = timeseriesConverter.convertToTimeseriesDtos(timeseries, timeseriesIds).stream()) {
            stream.forEach(dataframe -> {
                try {
                    outputStream.write(objectMapper.writeValueAsBytes(dataframe));
                    outputStream.write(LINE_DELIMITER.getBytes());
                    counter.incrementAndGet();

                } catch (final IOException e) {
                    log.error("Error while executing Async-Timeseries-Task -> methodName: {}, threadName: {}, threadGroupName: {}."
                            , methodName, threadName, threadGroupName);
                    log.error("Error while streaming timeseries by criteria: [{}]", timeSeriesCriteriaDto);
                }
            });
        }
        log.info("Streaming of {} timeseries by criteria: [{}] processed successfully, Task: [{}] took {}ms.", counter.get(),
                timeSeriesCriteriaDto, threadName, (System.currentTimeMillis() - start));
    }*/

}