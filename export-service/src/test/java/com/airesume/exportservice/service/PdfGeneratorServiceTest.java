package com.airesume.exportservice.service;

import com.airesume.exportservice.dto.ResumeResponseDTO;
import com.airesume.exportservice.dto.SectionDTO;
import com.airesume.exportservice.dto.TemplateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PDF Generator Service.
 * Verifies that HTML/CSS layouts are correctly converted to PDF bytes
 * with template variable substitution.
 */
class PdfGeneratorServiceTest {

    private PdfGeneratorService pdfGeneratorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        pdfGeneratorService = new PdfGeneratorService(objectMapper);
    }

    /**
     * Verifies that PDF generation fails if the template is null.
     */
    @Test
    void testGeneratePdf_NullTemplate() {
        ResumeResponseDTO resume = new ResumeResponseDTO();
        assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generatePdf(resume, null, null));
    }

    /**
     * Verifies that PDF generation fails if the template's HTML layout is missing.
     */
    @Test
    void testGeneratePdf_NullHtmlLayout() {
        ResumeResponseDTO resume = new ResumeResponseDTO();
        TemplateDTO template = new TemplateDTO();
        assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generatePdf(resume, template, null));
    }

    /**
     * Tests a complete successful PDF generation cycle with a mock resume and template.
     */
    @Test
    void testGeneratePdf_Success() throws Exception {
        // Setup Resume
        ResumeResponseDTO resume = new ResumeResponseDTO();
        resume.setTitle("My Resume");
        resume.setTargetJobTitle("Software Engineer");

        SectionDTO personalInfo = new SectionDTO();
        personalInfo.setSectionType("personal_info");
        personalInfo.setIsVisible(true);
        personalInfo.setContent("{\"name\":\"John Doe\", \"email\":\"john@example.com\"}");

        SectionDTO experience = new SectionDTO();
        experience.setSectionType("experience");
        experience.setIsVisible(true);
        experience.setContent("[{\"company\":\"Acme\", \"role\":\"Dev\"}]");

        SectionDTO hiddenSection = new SectionDTO();
        hiddenSection.setSectionType("hidden");
        hiddenSection.setIsVisible(false);

        resume.setSections(List.of(personalInfo, experience, hiddenSection));

        // Setup Template
        TemplateDTO template = new TemplateDTO();
        template.setHtmlLayout("<h1>{{title}}</h1><h2>{{targetJobTitle}}</h2>{{#personal_info}}<p>{{name}}</p>{{/personal_info}}");
        template.setCssStyles("h1 { color: red; }");

        // Customizations
        String customizations = "{\"primaryColor\":\"#ff0000\", \"fontFamily\":\"Roboto\"}";

        // Execute
        byte[] pdfBytes = pdfGeneratorService.generatePdf(resume, template, customizations);

        // Verify
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    /**
     * Verifies that invalid customization JSON is gracefully handled without failing the export.
     */
    @Test
    void testGeneratePdf_InvalidCustomizations() throws Exception {
        ResumeResponseDTO resume = new ResumeResponseDTO();
        resume.setTitle("Test");
        resume.setSections(List.of());

        TemplateDTO template = new TemplateDTO();
        template.setHtmlLayout("<p>Test</p>");

        // Execute with invalid JSON to trigger catch block
        byte[] pdfBytes = pdfGeneratorService.generatePdf(resume, template, "INVALID_JSON");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    /**
     * Verifies that PDF generation succeeds even with empty customizations.
     */
    @Test
    void testGeneratePdf_EmptyCustomizations() throws Exception {
        ResumeResponseDTO resume = new ResumeResponseDTO();
        resume.setTitle("Test");
        resume.setSections(List.of());

        TemplateDTO template = new TemplateDTO();
        template.setHtmlLayout("<p>Test</p>");

        // Execute with null customizations
        byte[] pdfBytes = pdfGeneratorService.generatePdf(resume, template, null);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    /**
     * Verifies that malformed JSON in a resume section does not crash the PDF generator.
     */
    @Test
    void testGeneratePdf_InvalidSectionContent() throws Exception {
        ResumeResponseDTO resume = new ResumeResponseDTO();
        SectionDTO invalidSection = new SectionDTO();
        invalidSection.setSectionType("invalid");
        invalidSection.setIsVisible(true);
        invalidSection.setContent("INVALID JSON CONTENT");
        resume.setSections(List.of(invalidSection));

        TemplateDTO template = new TemplateDTO();
        template.setHtmlLayout("<p>Test</p>");

        // It should catch the exception and continue
        byte[] pdfBytes = pdfGeneratorService.generatePdf(resume, template, null);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    /**
     * Verifies that blank section content is handled gracefully during PDF generation.
     */
    @Test
    void testGeneratePdf_BlankSectionContent() throws Exception {
        ResumeResponseDTO resume = new ResumeResponseDTO();
        SectionDTO section = new SectionDTO();
        section.setSectionType("experience");
        section.setIsVisible(true);
        section.setContent("   "); // blank
        resume.setSections(List.of(section));

        TemplateDTO template = new TemplateDTO();
        template.setHtmlLayout("<p>Test</p>");

        byte[] pdfBytes = pdfGeneratorService.generatePdf(resume, template, null);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    /**
     * Verifies that null section content is handled gracefully during PDF generation.
     */
    @Test
    void testGeneratePdf_NullSectionContent() throws Exception {
        ResumeResponseDTO resume = new ResumeResponseDTO();
        SectionDTO section = new SectionDTO();
        section.setSectionType("experience");
        section.setIsVisible(true);
        section.setContent(null); // null
        resume.setSections(List.of(section));

        TemplateDTO template = new TemplateDTO();
        template.setHtmlLayout("<p>Test</p>");

        byte[] pdfBytes = pdfGeneratorService.generatePdf(resume, template, null);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}

