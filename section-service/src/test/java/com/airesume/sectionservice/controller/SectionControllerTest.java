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

    @Test
    @DisplayName("API: GET /resume/{resumeId}/type/{type} - Should return sections by type")
    void getSectionsByType_ShouldReturnList() throws Exception {
        Section section = new Section();
        section.setSectionId(1L);
        section.setSectionType(SectionType.EDUCATION);
        
        when(sectionService.getSectionsByType(10L, SectionType.EDUCATION)).thenReturn(List.of(section));

        mockMvc.perform(get("/resume/10/type/EDUCATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectionId").value(1))
                .andExpect(jsonPath("$[0].sectionType").value("EDUCATION"));
    }

    @Test
    @DisplayName("API: GET /resume/{resumeId}/ai-generated - Should return ai sections")
    void getAiGeneratedSections_ShouldReturnList() throws Exception {
        Section section = new Section();
        section.setSectionId(1L);
        section.setAiGenerated(true);
        
        when(sectionService.getAiGeneratedSections(10L)).thenReturn(List.of(section));

        mockMvc.perform(get("/resume/10/ai-generated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectionId").value(1))
                .andExpect(jsonPath("$[0].aiGenerated").value(true));
    }

    @Test
    @DisplayName("API: PUT /{sectionId} - Should update section")
    void updateSection_ShouldReturnUpdated() throws Exception {
        Section requestSection = new Section();
        requestSection.setTitle("New Title");
        
        Section responseSection = new Section();
        responseSection.setSectionId(1L);
        responseSection.setTitle("New Title");

        when(sectionService.updateSection(any(Long.class), any(Section.class))).thenReturn(responseSection);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestSection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value(1))
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    @DisplayName("API: PUT /{sectionId}/toggle-visibility - Should toggle visibility")
    void toggleVisibility_ShouldReturnUpdated() throws Exception {
        Section responseSection = new Section();
        responseSection.setSectionId(1L);
        responseSection.setIsVisible(false);

        when(sectionService.toggleVisibility(1L)).thenReturn(responseSection);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/1/toggle-visibility"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value(1))
                .andExpect(jsonPath("$.isVisible").value(false));
    }

    @Test
    @DisplayName("API: PUT /resume/{resumeId}/reorder - Should reorder sections")
    void reorderSections_ShouldReturnNoContent() throws Exception {
        List<Long> sectionIds = List.of(3L, 1L, 2L);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/resume/10/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sectionIds)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("API: PUT /bulk-update - Should bulk update sections")
    void bulkUpdate_ShouldReturnUpdatedList() throws Exception {
        Section s1 = new Section(); s1.setSectionId(1L);
        List<Section> sections = List.of(s1);

        when(sectionService.bulkUpdate(any())).thenReturn(sections);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/bulk-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sections)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectionId").value(1));
    }

    @Test
    @DisplayName("API: DELETE /{sectionId} - Should delete section")
    void deleteSection_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("API: DELETE /resume/{resumeId}/all - Should delete all sections")
    void deleteAllSectionsByResume_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/resume/10/all"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("API: GET /resume/{resumeId}/count - Should return count")
    void countSections_ShouldReturnCount() throws Exception {
        when(sectionService.countSections(10L)).thenReturn(5L);

        mockMvc.perform(get("/resume/10/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }
}
