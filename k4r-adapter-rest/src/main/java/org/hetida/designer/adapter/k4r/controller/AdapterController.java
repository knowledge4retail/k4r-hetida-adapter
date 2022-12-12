package org.hetida.designer.adapter.k4r.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.hetida.designer.adapter.k4r.dto.*;
import org.hetida.designer.adapter.k4r.service.AdapterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Log4j2
@RestController
@RequestMapping(value = "/adapter-k4r", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdapterController {

    private final AdapterService adapterService;

    public AdapterController(AdapterService adapterService) {

        this.adapterService = adapterService;
    }

    /**
     * Returns adapter info
     *
     * @return AdapterInfoDTO
     */
    @ApiOperation(
            value = "Returns adapter info",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = AdapterInfoDTO.class
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Successfully got adapter info"),
                    @ApiResponse(code = 503, message = "Internal server error")
            }
    )
    @GetMapping(value = "/info")
    public ResponseEntity<AdapterInfoDTO> getAdapterInfo() {

        log.info("Get adapter info");

        return new ResponseEntity<>(adapterService.getAdapterInfo(), HttpStatus.OK);
    }

    /**
     * Returns StructureDTO
     *
     * @return StructureDTO
     */
    @ApiOperation(
                value = "Returns structure metadata for the adapter",
                produces = MediaType.APPLICATION_JSON_VALUE,
                response = StructureDTO.class)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Successfully got metadata of adapter"),
                    @ApiResponse(code = 404, message = "Adapter not found"),
                    @ApiResponse(code = 503, message = "Internal server error")
            }
    )
    @GetMapping(value = "/structure")
    public ResponseEntity<StructureDTO> getStructure(@RequestParam(required = false) String parentId) {

        log.info("Get structure for parentId: {}", parentId != null ? parentId : "null");

        StructureDTO structureDTO = adapterService.getStructure(parentId);

        return new ResponseEntity<>(structureDTO, HttpStatus.OK);
    }


    /**
     * Returns dataframe
     *
     * @return A Stream of dataframes
     */
    @ApiOperation(
            value = "Returns a Stream of json-line-delimited dataframe for the given id.",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = List.class,
            responseContainer = "A Stream of List"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Dataframes are getting streamed."),
                    @ApiResponse(code = 503, message = "Internal server error")
            }
    )
    @GetMapping(value = "/dataframe")
    public ResponseEntity<StreamingResponseBody> getDataframe(@RequestParam final String id) {

        log.info("Streaming dataframes for id: {}", id);

        final StreamingResponseBody streamingResponseBody = outputStream -> adapterService.startDataframeStreaming(outputStream, id);

        return ResponseEntity.ok(streamingResponseBody);
    }

    /*@ApiOperation(
            value = "Adds a dataframe for the given Id with the new value(s).",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = HttpStatus.class,
            responseContainer = "HttpStatus.CREATED"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Dataframe was/were added successfully."),
                    @ApiResponse(code = 404, message = "Channel or one of the dataframe were not found."),
                    @ApiResponse(code = 400, message = "Query params were not valid."),
                    @ApiResponse(code = 500, message = "Internal server error")
            }
    )
    @PostMapping(value = "/dataframe")
    public ResponseEntity<List<DataFrameDTO>> addDataframe(@RequestParam(value = "id") final String id, @RequestBody final List<DataFrameDTO> requestDtos) {

        log.info("Adding dataframe for id {}", id);

        return ResponseEntity.ok(requestDtos);
    }*/

    /**
     * Returns filtered sources
     *
     * @return ResponseEntity<SourceDTO>
     */
    @ApiOperation(
            value = "Returns a list of SourceDTO Objects for the given filter",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = SourceDTO.class,
            responseContainer = "List"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "sources successfully returned"),
                    @ApiResponse(code = 503, message = "Internal server error")
            }
    )
    @GetMapping(value = "/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourcesDTO> getFilteredSources(@RequestParam(value = "optional filter", required = false) String filter) {

        log.info("Getting sources for all channels, filtered by {}", filter);

        return new ResponseEntity<>(adapterService.getFilteredSources(filter), HttpStatus.OK);
    }

    /**
     * Returns one source
     *
     * @return ResponseEntity<List<SourceDTO>>
     */
    @ApiOperation(
            value = "Returns a SourceDTO Objects for the given id",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = SourceDTO.class
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "source successfully returned"),
                    @ApiResponse(code = 503, message = "Internal server error")
            }
    )
    @GetMapping(value = "/sources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceDTO> getSource(@PathVariable String id) throws Exception{

        String decodedId = URLDecoder.decode(id, StandardCharsets.UTF_8.toString());
        log.info("Getting sources for id {}", decodedId);

        return new ResponseEntity<>(adapterService.getSource(decodedId), HttpStatus.OK);
    }

    /**
     * Returns filtered sinks
     *
     * @return ResponseEntity<List<SinkDTO>>
     */
    @ApiOperation(
            value = "Returns a list of SinkDTO Objects for the given filter",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = SinkDTO.class,
            responseContainer = "List"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "sinks successfully returned"),
                    @ApiResponse(code = 503, message = "Internal server error")
            }
    )
    @GetMapping(value = "/sinks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SinksDTO> getFilteredSinks(@RequestParam(value = "filter", required = false) String filter) {

        log.info("Getting sinks for all channels, filtered by {}", filter);

        return new ResponseEntity<>(adapterService.getFilteredSinks(filter), HttpStatus.OK);
    }

    /**
     * Returns one thingNodes
     *
     * @return ResponseEntity<ThingNodeDTO>
     */
    @ApiOperation(
            value = "Returns a ThingNodeDTO Objects for the given id",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = ThingNodeDTO.class
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "ThingNode successfully returned"),
                    @ApiResponse(code = 503, message = "Internal server error")
            }
    )
    @GetMapping(value = "/thingNodes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ThingNodeDTO> getThingNodes(@PathVariable String id) throws Exception{

        String decodedId = URLDecoder.decode(id, StandardCharsets.UTF_8.toString());
        log.info("Getting thingNodes for id {}", decodedId);
        log.info("hier " + adapterService.getThingNode(decodedId));

        return new ResponseEntity<>(adapterService.getThingNode(decodedId), HttpStatus.OK);
    }
}
