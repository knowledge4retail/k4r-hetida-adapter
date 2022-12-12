package org.hetida.designer.adapter.k4r.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.hetida.designer.adapter.k4r.dto.DataFrameDTO;
import org.hetida.designer.adapter.k4r.dto.TimeSeriesCriteriaDto;
import org.hetida.designer.adapter.k4r.dto.client.ClientSinksDTO;
import org.hetida.designer.adapter.k4r.dto.client.ClientSourcesDTO;
import org.hetida.designer.adapter.k4r.dto.client.ClientStructureDTO;
import org.hetida.designer.adapter.k4r.dto.client.ClientThingNodesDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Log4j2
@SuppressWarnings({"unchecked", "rawtypes", "ConstantConditions"})
public class AdapterClient {

    @Value("${adapter.k4r.dt.api.url}")
    private String DtApiUrl;

    @Value("${adapter.k4r.dt.graph.url}")
    private String DtGraphQlUrl;

    private final ObjectMapper objectMapper;

    public AdapterClient(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    public ClientStructureDTO getStructure(String parentId) throws HttpStatusCodeException {

        try {
            return createClientStructure(parentId);
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

    private ClientStructureDTO createClientStructure(String parentId) throws Exception {
        ClientStructureDTO clientStructureDTO = new ClientStructureDTO();
        readThingNodes(clientStructureDTO, parentId);
        readSources(clientStructureDTO, parentId);
        readSinks(clientStructureDTO);
        return clientStructureDTO;
    }


    private void readThingNodes(ClientStructureDTO clientStructureDTO, String parentId) throws IOException {

        List<String> params;
        if (parentId != null) {
            params = Arrays.asList(parentId.split("\\."));
        } else {
            params = new ArrayList<>();
        }

        int count = params.size();
        StringBuilder structFile = new StringBuilder("{\"thingNodes\": [  ");

        if (count == 0) {

            structFile.append(thingNodeTemplate("stores", null));
            structFile.append(thingNodeTemplate("products", null));
            structFile.append(thingNodeTemplate("devices", null));
            structFile.append(thingNodeTemplate("customers", null));
            structFile.append(thingNodeTemplate("items", null));
            structFile.append(thingNodeTemplate("itemGroups", null));
            structFile.append(thingNodeTemplate("storeCharacteristics", null));
            structFile.append(thingNodeTemplate("productCharacteristics", null));
            structFile.append(thingNodeTemplate("materialGroups", null));
            structFile.append(thingNodeTemplate("productDescriptions", null));
            structFile.append(thingNodeTemplate("productGtins", null));
            structFile.append(thingNodeTemplate("productUnits", null));
            structFile.append(thingNodeTemplate("deliveredItems", null));
            structFile.append(thingNodeTemplate("units", null));
            structFile.append(thingNodeTemplate("planograms", null));
        }

        if (count == 1) {
            List<String> stringEntities = Arrays.asList("products", "devices");
            List<String> integerEntities = Arrays.asList("stores");

            if(stringEntities.contains(params.get(0))){
                List<String> Ids = getStringIdsFromGraphQLQuery(params.get(0));
                for (String id : Ids) {

                    structFile.append(thingNodeTemplate(
                            params.get(0) + "." + id,
                            params.get(0)
                    ));
                }
            } else if (integerEntities.contains(params.get(0))){
                List<Integer> Ids = getIdsFromGraphQLQuery(params.get(0), null, null);
                for (Integer id : Ids) {

                    structFile.append(thingNodeTemplate(
                            params.get(0) + "." + id,
                            params.get(0)
                    ));
                }
            }
        }

        if (count == 2) {

            List<String> nextSteps = new ArrayList<>();
            switch (params.get(0)) {
                case "stores":
                    nextSteps.add(".shelves");
                    nextSteps.add(".productGroups");
                    nextSteps.add(".storeObjects");
                    nextSteps.add(".map2ds");
                    nextSteps.add(".storeProperties");
                    nextSteps.add(".trolleys");
                    nextSteps.add(".despatchAdvices");
                    break;
                case "shelves":
                    nextSteps.add(".shelfLayers");
                    break;
                case "shelfLayers":
                    nextSteps.add(".facing");
                    break;
                case "products":
                    nextSteps.add(".productProperties");
                    break;
                case "devices":
                    nextSteps.add(".images");
                    break;
                case "despatchAdvices":
                    nextSteps.add(".despatchLogisticUnits");
                    break;
                case "despatchLogisticUnits":
                    nextSteps.add(".despatchLineItems");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + params.get(0));
            }
            for(String nextStep: nextSteps) {
                structFile.append(thingNodeTemplate(
                        params.get(0) + "." + params.get(1) + nextStep,
                        params.get(0) + "." + params.get(1))
                );
            }
        }

        if (count == 3) {

            List<String> integerEntities = Arrays.asList("shelves", "shelfLayers", "despatchAdvices", "despatchLogisticUnits");

            if(integerEntities.contains(params.get(2))) {
                String filterId;
                switch (params.get(2)) {
                    case "shelves":
                    case "despatchAdvices":
                        filterId = "storeId";
                        break;
                    case "shelfLayers":
                        filterId = "shelfId";
                        break;
                    case "despatchLogisticUnits":
                        filterId = "despatchAdviceId";
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + params.get(2));
                }

                List<Integer> Ids = getIdsFromGraphQLQuery(params.get(2), filterId, Integer.parseInt(params.get(1)));
                for (Integer id : Ids) {

                    structFile.append(thingNodeTemplate(
                            params.get(2) + "." + id,
                            params.get(0) + "." + params.get(1) + "." + params.get(2)
                    ));
                }
            }
        }

        structFile = new StringBuilder(structFile.substring(0, structFile.length() - 2));
        structFile.append("]}");

        ClientThingNodesDTO thingNodes = objectMapper.readValue(structFile.toString(), new TypeReference<ClientThingNodesDTO>(){});
        clientStructureDTO.setThingNodes(thingNodes);
    }

    private void readSources(ClientStructureDTO clientStructureDTO, String parentId) throws IOException {

        List<String> params;
        if (parentId != null) {
            params = Arrays.asList(parentId.split("\\."));
        } else {
            params = new ArrayList<>();
        }

        int count = params.size();
        StringBuilder structFile = new StringBuilder("{\"sources\": [  ");

        if (count == 1) {

            structFile.append(sourceTemplateAll(params.get(0), "DATAFRAME_TYPE"));
        }

        if (count == 2) {

            if(params.get(0).equals("shelves") || params.get(0).equals("shelfLayers")){

                structFile.append(sourceTemplateOne(params.get(0) + "." + params.get(1), "DATAFRAME_TYPE"));
            }
        }

        if (count == 3) {

            structFile.append(sourceTemplateAll(params.get(0) + "." + params.get(1) + "." + params.get(2), "DATAFRAME_TYPE"));
        }

        structFile = new StringBuilder(structFile.substring(0, structFile.length() - 2));
        structFile.append("]}");

        ClientSourcesDTO sources = objectMapper.readValue(structFile.toString(), new TypeReference<ClientSourcesDTO>(){});
        clientStructureDTO.setSources(sources);
    }

    private void readSinks(ClientStructureDTO clientStructureDTO) throws IOException {

        // currently, no sinks
        String sinkMock = "{\"sinks\": []}";
        ClientSinksDTO sinks = objectMapper.readValue(sinkMock, new TypeReference<ClientSinksDTO>(){});
        clientStructureDTO.setSinks(sinks);
    }

    private String sourceTemplateOne(String thingNodeId, String type) {

        return String.format("{ \"id\": \"%1$s.m\", \"thingNodeId\": \"%1$s\", \"name\": \"%1$s.m\", \"type\": \"%2$s\" }, ", thingNodeId, type);
    }

    private String sourceTemplateAll(String thingNodeId, String type) {

        return String.format("{ \"id\": \"%1$s.a\", \"thingNodeId\": \"%1$s\", \"name\": \"%1$s.a\", \"type\": \"%2$s\" }, ", thingNodeId, type);
    }

    private String thingNodeTemplate(String id, String parentId) {

        if (parentId == null) {

            return String.format("{ \"id\": \"%1$s\", \"parentId\": null, \"name\": \"%1$s\", \"description\": \"\" }, ", id);
        } else {

            return String.format("{ \"id\": \"%1$s\", \"parentId\": \"%2$s\", \"name\": \"%1$s\", \"description\": \"\" }, ", id, parentId);
        }
    }

    private List<Integer> getIdsFromGraphQLQuery(String entity, String fkEntity, Integer fk) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("content-type", "application/graphql");
        String query;

        if(fkEntity == null) {
            query = "query getIds {" + entity + " {id}}";

        } else {
            query = "query getIds {" + entity
                    + "(filter: {"
                    + fkEntity
                    + ": {operator: \"eq\" value: \""
                    + fk.toString()
                    + "\" type: \"int\"}})" + "{id}}";
        }
        log.info("graphQL:" + query);

        ResponseEntity<String> response = restTemplate.postForEntity(DtGraphQlUrl, new HttpEntity<>(query, httpHeaders), String.class);
        String json = response.getBody();

        JSONObject jsonObject = new JSONObject(json);
        JSONArray data = jsonObject.getJSONObject("data").getJSONArray(entity);
        List<Integer> entityIdList = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            entityIdList.add(data.getJSONObject(i).getInt("id"));
            }
        return entityIdList;
    }

    private List<String> getStringIdsFromGraphQLQuery(String entity) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("content-type", "application/graphql");
        String query;

        query = "query getIds {" + entity + " {id}}";

        ResponseEntity<String> response = restTemplate.postForEntity(DtGraphQlUrl, new HttpEntity<>(query, httpHeaders), String.class);
        String json = response.getBody();

        JSONObject jsonObject = new JSONObject(json);
        JSONArray data = jsonObject.getJSONObject("data").getJSONArray(entity);
        List<String> entityIdList = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            entityIdList.add(data.getJSONObject(i).getString("id"));
        }
        return entityIdList;
    }



    public ArrayList<LinkedHashMap> getDataframe(final String id, TimeSeriesCriteriaDto timeSeriesCriteriaDto) {

        String context = id.substring(0, id.length() -2);
        context = context.replace(".", "/");

        try {
            if (id.endsWith(".m")) {

                return (ArrayList<LinkedHashMap>) readOneJson(context).getDataframes().get("series");
            } else if (id.endsWith(".a")) {

                return (ArrayList<LinkedHashMap>) readTable(context).getDataframes().get("dataframe");
            }
            return new ArrayList<>();
        } catch (Exception e) {

            log.error(e);
            return null;
        }
    }

    private DataFrameDTO readOneJson(String context) {
        try (InputStream inputStream = readFromUri(DtApiUrl + context);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String contents = reader.readLine();
            if (contents == null) {

                log.info("Set manually contents to an empty list: []");
                contents = " ";
            }
            contents = "{\"series\": [" + contents + "]}";
            return objectMapper.readValue(contents, new TypeReference<DataFrameDTO>(){});
        } catch (IOException e) {

            log.error(e);
            return null;
        }
    }

    private DataFrameDTO readTable(String context) {
        try (InputStream inputStream = readFromUri(DtApiUrl + context);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            if (contents.isEmpty()) {

                log.info("Set manually contents to an empty list: []");
                contents = "[]";
            }
            contents = "{\"dataframe\": " + contents + "}";
            return objectMapper.readValue(contents, new TypeReference<DataFrameDTO>(){});
        } catch (IOException e) {

            log.error(e);
            return null;
        }
    }

    private InputStream readFromUri(String uri) throws IOException {
        try {

            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setDoInput(true);
            return con.getInputStream();
        } catch (FileNotFoundException foundException) {

            log.info(foundException);
            InputStream empty = new InputStream() {
                @Override
                public int read() {
                    return -1;
                }
            };
            return empty;
        } catch (IOException e) {

            log.error(e);
            return null;
        }
    }
}
