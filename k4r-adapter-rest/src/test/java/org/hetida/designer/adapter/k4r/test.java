package org.hetida.designer.adapter.k4r;

import org.hetida.designer.adapter.k4r.dto.AdapterInfoDTO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.Assert.assertNotNull;

public class test extends AbstractIntegrationTest {

    // Test wird nicht gestartet !!!!!!!!
    private static final String TEST_URI = "info";

    @Before
    @Override
    public void setUp() throws Exception{

        super.setUp();
    }

    @Test
    public void getTest() throws Exception {
        MvcResult itemsMvcResult = mvc.perform(MockMvcRequestBuilders.get(TEST_URI)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        assertResponseCodeEquals(HttpStatus.OK, itemsMvcResult);

        AdapterInfoDTO content = mapFromJson(itemsMvcResult.getResponse().getContentAsString(), AdapterInfoDTO.class);
        assertNotNull(content);
        assertNotNull(content.getName());
    }
}
