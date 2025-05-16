package ru.kernelpunik.teradactyle.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.models.Interference;
import ru.kernelpunik.teradactyle.services.ICompositionDetectorService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

public class MvcCompositionServiceAdapter implements ICompositionDetectorService {

    private final MockMvc mvc;
    ObjectMapper mapper = new ObjectMapper();

    public MvcCompositionServiceAdapter(MockMvc mvc) {
        this.mvc = mvc;
    }


    @Override
    public Component putComponent(Component solution) {
        try {
            String id = mvc.perform(
                    post("/v0/api/putSolution")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(solution))
                    )
                    .andExpect(status().isOk())
                    .andExpect(
                            result ->
                                    assertTrue(result.getResponse().getContentAsString().matches("\\d+(\\.\\d+)?")
                                    )
                    ).andReturn().getResponse().getContentAsString();
            solution.setComponentId(Integer.parseInt(id));
            return solution;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Component getComponent(long componentId) {
        try {
            String ans = mvc.perform(get("/v0/api/getSolution?id=" + componentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.solutionId").exists())
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.languageId").exists())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.description").exists())
                    .andReturn().getResponse().getContentAsString();
            return mapper.readValue(ans, Component.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Interference> getInterferences(long solutionId) {
        try {
            String ans = mvc.perform(get("/v0/api/getInterferences?solution_id=" + solutionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andReturn().getResponse().getContentAsString();
            return mapper.readValue(ans, mapper.getTypeFactory().constructCollectionType(List.class, Interference.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
