package com.airesume.sectionservice.controller;

import com.airesume.sectionservice.model.Section;
import com.airesume.sectionservice.model.SectionType;
import com.airesume.sectionservice.service.SectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SectionController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple controller tests
public class SectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SectionService sectionService;

    @MockitoBean
    private com.airesume.sectionservice.service.JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("API: POST / - Should add a new section")
    void addSection_ShouldReturnCreatedSection() throws Exception {
        Section requestSection = new Section();
        requestSection.setResumeId(10L);
        requestSection.setSectionType(SectionType.SUMMARY);
        requestSection.setTitle("Summary");
        requestSection.setContent("{\"text\":\"Experienced developer\"}");

        Section responseSection = new Section();
        responseSection.setSectionId(1L);
        responseSection.setResumeId(10L);
        responseSection.setSectionType(SectionType.SUMMARY);
        responseSection.setTitle("Summary");

        when(sectionService.addSection(any(Section.class))).thenReturn(responseSection);

        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestSection)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sectionId").value(1))
                .andExpect(jsonPath("$.resumeId").value(10))
                .andExpect(jsonPath("$.title").value("Summary"));
    }

    @Test
    @DisplayName("API: GET /resume/{resumeId} - Should return sections")
    void getSectionsByResume_ShouldReturnList() throws Exception {
        Section section = new Section();
        section.setSectionId(1L);
        section.setResumeId(10L);
        section.setSectionType(SectionType.SUMMARY);
        section.setTitle("Summary");

        when(sectionService.getSectionsByResume(10L)).thenReturn(List.of(section));

        mockMvc.perform(get("/resume/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectionId").value(1))
                .andExpect(jsonPath("$[0].title").value("Summary"));
    }

    @Test
    @DisplayName("API: GET /{sectionId} - Should return section")
    void getSectionById_ShouldReturnSection() throws Exception {
        Section section = new Section();
        section.setSectionId(1L);
        section.setResumeId(10L);
        section.setSectionType(SectionType.EXPERIENCE);

        when(sectionService.getSectionById(1L)).thenReturn(section);

        mockMvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value(1))
                .andExpect(jsonPath("$.sectionType").value("EXPERIENCE"));
    }
}
