package ru.kernelpunik.teradactyle.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.kernelpunik.teradactyle.AntiplagiatApplication;
import ru.kernelpunik.teradactyle.services.IPlagiarismDetectorService;
import ru.kernelpunik.teradactyle.services.PlagiarismDetectorTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AntiplagiatApplication.class)
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
@AutoConfigureMockMvc
class MainControllerTest extends PlagiarismDetectorTest {
    MockMvc mvc;

    @Autowired
    public MainControllerTest(MockMvc mvc) {
        super(new MvcPlagiarismServiceAdapter(mvc));
        this.mvc = mvc;
    }

    public

    @Test
    void getLanguages() throws Exception {
        mvc.perform(get("/v0/api/getLanguages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$[*].name").exists())
                .andExpect(jsonPath("$[*].tsLanguage").doesNotExist());
    }
}