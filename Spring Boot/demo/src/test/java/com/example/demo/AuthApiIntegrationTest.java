package com.example.demo;

import java.util.UUID;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
class AuthApiIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository users;
    @Autowired private ObjectMapper mapper;
    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwords;
    private MockMvc http;

    @BeforeEach
    void setUp() {
        http = MockMvcBuilders.webAppContextSetup(context)
            .addFilters(context.getBean(FilterChainProxy.class)).build();
    }

    @Test
    void registerLoginAndReadOwnProfile() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@example.com";
        String register = """
            {"name":"Test Passenger","email":"%s","password":"SecurePass123"}
            """.formatted(email);
        http.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
            .andExpect(status().isCreated());

        var stored = users.findByEmailIgnoreCase(email).orElseThrow();
        assertNotNull(stored.getPasswordHash());
        assertTrue(passwords.matches("SecurePass123", stored.getPasswordHash()));
        assertTrue(stored.getLegacyPassword() == null);

        String login = """
            {"email":"%s","password":"SecurePass123"}
            """.formatted(email);
        MvcResult result = http.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(login))
            .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        http.perform(get("/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
        http.perform(get("/auth/me")).andExpect(status().isUnauthorized());
        http.perform(get("/health")).andExpect(status().isOk());
    }

    @Test
    void allowsDeployedFrontendCorsPreflightEvenWhenConfiguredOriginsAreLocalOnly() throws Exception {
        String origin = "https://reactproject-rho-nine.vercel.app";
        http.perform(options("/auth/register")
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", origin));
    }
}
