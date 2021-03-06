package com.runmate.security.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runmate.configure.jwt.JwtAuthenticationFilter;
import com.runmate.configure.jwt.JwtProvider;
import com.runmate.domain.dto.AuthRequest;
import com.runmate.domain.user.User;
import com.runmate.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTests {
    MockMvc mockMvc;
    ObjectMapper mapper=new ObjectMapper();

    @Autowired
    WebApplicationContext ctx;
    @Autowired
    JwtProvider provider;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    public void setUp(){
        mockMvc= MockMvcBuilders.webAppContextSetup(ctx)
                .addFilter(new JwtAuthenticationFilter(provider))
                .build();
    }

    @Test
    public void joinAndLogin() throws Exception {
        AuthRequest request=new AuthRequest();
        request.setEmail("anny@anny.com");
        request.setPassword("1234");

        String jsonBody=mapper.writeValueAsString(request);

        assertNotNull(mockMvc);
        mockMvc.perform(post("/api/auth/local/new")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk());

        User user=new User();
        user.setEmail("anny@anny.com");
        user.setPassword("1234");

        jsonBody=mapper.writeValueAsString(user);

        MvcResult mvcResult=mockMvc.perform(post("/api/auth/local/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization")).andReturn();

        String token=mvcResult.getResponse().getHeader("Authorization").replace("Bearer ","");
        assertEquals(jwtProvider.validate(token),true);
        assertEquals(jwtProvider.getClaim(token),user.getEmail());

        assertNotNull(userRepository.findByEmail(user.getEmail()));
    }
}
