package com.fintech.ledger.account;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// End to end: the whole application is started, nothing is mocked.
// Proves controller, service, repository and JSON mapping work together, which the slice tests can't.
@SpringBootTest
@AutoConfigureMockMvc
class AccountApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createdAccountCanBeFetchedFromItsLocation() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/accounts")
                                           .contentType(MediaType.APPLICATION_JSON)
                                           .content("""
                                                   {"ownerName": "Alice", "currency": "INR"}
                                                   """))
                                   .andExpect(status().isCreated())
                                   .andReturn();

        String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        String location = created.getResponse().getHeader("Location");
        assertThat(location).endsWith("/api/accounts/" + id);

        // Follow the Location header exactly as a real client would
        mockMvc.perform(get(URI.create(location)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(id))
               .andExpect(jsonPath("$.ownerName").value("Alice"))
               .andExpect(jsonPath("$.currency").value("INR"))
               .andExpect(content().string(containsString("\"balance\":0.00")));
    }

    @Test
    void depositIsVisibleInLaterReads() throws Exception {
        String id = createAccount();

        mockMvc.perform(post("/api/accounts/{id}/deposits", id)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {"amount": 500.00}
                               """))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("\"balance\":500.00")));

        mockMvc.perform(get("/api/accounts/{id}", id))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("\"balance\":500.00")));
    }

    private String createAccount() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/accounts")
                                           .contentType(MediaType.APPLICATION_JSON)
                                           .content("""
                                                   {"ownerName": "Alice", "currency": "INR"}
                                                   """))
                                   .andExpect(status().isCreated())
                                   .andReturn();
        return JsonPath.read(created.getResponse().getContentAsString(), "$.id");
    }
}
