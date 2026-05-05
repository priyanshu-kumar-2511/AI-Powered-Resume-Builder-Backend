package com.airesume.exportservice.service;

import com.airesume.exportservice.dto.ResumeResponseDTO;
import com.airesume.exportservice.dto.SectionDTO;
import com.airesume.exportservice.dto.TemplateDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorService {

    private final ObjectMapper objectMapper;
    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public byte[] generatePdf(ResumeResponseDTO resume, TemplateDTO template, String customizations) throws Exception {
        if (template == null || template.getHtmlLayout() == null) {
            throw new IllegalArgumentException("Template or HTML layout is missing for export.");
        }
        
        // 1. Prepare data for Mustache
        Map<String, Object> context = prepareContext(resume);
        
        // 2. Parse and Apply Customizations
        Map<String, String> customMap = parseCustomizations(customizations);
        context.putAll(customMap);

        // 3. Render HTML with Mustache
        String renderedHtml = renderTemplate(template.getHtmlLayout(), context);

        // 4. Combine with CSS and Clean HTML
        String fullHtml = wrapWithStyles(renderedHtml, template.getCssStyles(), customMap);
        String cleanedHtml = cleanToXhtml(fullHtml);

        // 5. Generate PDF
        return renderPdf(cleanedHtml);
    }

    private Map<String, String> parseCustomizations(String json) {
        try {
            if (json != null && !json.isBlank()) {
                return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse customizations JSON: {}", e.getMessage());
        }
        return new HashMap<>();
    }

    private Map<String, Object> prepareContext(ResumeResponseDTO resume) {
        Map<String, Object> context = new HashMap<>();
        context.put("title", resume.getTitle());
        context.put("targetJobTitle", resume.getTargetJobTitle());

        // Group sections by type for easy access in templates
        for (SectionDTO section : resume.getSections()) {
            if (Boolean.FALSE.equals(section.getIsVisible())) continue;

            String type = section.getSectionType().toLowerCase();
            try {
                Object content = parseSectionContent(section.getContent());
                context.put(type, content);
                
                // Also support lists for iterative sections like EXPERIENCE, EDUCATION
                if (isListType(type)) {
                    List<Object> list = (List<Object>) context.getOrDefault(type + "List", new ArrayList<>());
                    list.add(content);
                    context.put(type + "List", list);
                }
            } catch (Exception e) {
                log.error("Failed to parse section content for type {}: {}", type, e.getMessage());
            }
        }
        
        return context;
    }

    private Object parseSectionContent(String jsonContent) throws Exception {
        if (jsonContent == null || jsonContent.isBlank()) return new HashMap<>();
        String trimmed = jsonContent.trim();
        if (trimmed.startsWith("[")) {
            return objectMapper.readValue(trimmed, new TypeReference<List<Object>>() {});
        }
        return objectMapper.readValue(trimmed, new TypeReference<Map<String, Object>>() {});
    }

    private boolean isListType(String type) {
        return List.of("experience", "education", "projects", "certifications", "languages", "skills")
                .contains(type);
    }

    private String renderTemplate(String htmlLayout, Map<String, Object> context) {
        Mustache mustache = mustacheFactory.compile(new StringReader(htmlLayout), "resume");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }

    private String wrapWithStyles(String html, String css, Map<String, String> customs) {
        String safeCss = css != null ? css : "";
        String primary = customs.getOrDefault("primaryColor", "#00d4b4");
        String font = customs.getOrDefault("fontFamily", "Inter");

        return "<html><head><style>" +
                ":root { --primary: " + primary + "; --font-family: '" + font + "'; }\n" +
                "body { font-family: var(--font-family), sans-serif; }\n" +
                ".section-title, .resume-role, .edu-inst, .skill-chip { color: var(--primary); }\n" +
                ".header { border-bottom-color: var(--primary) !important; }\n" +
                safeCss + 
                "</style></head><body>" + html + "</body></html>";
    }

    private String cleanToXhtml(String html) {
        Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        document.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
        return document.html();
    }

    private byte[] renderPdf(String html) throws Exception {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            // Removed useFastMode() for better stability with complex HTML
            builder.withHtmlContent(html, new java.io.File(".").toURI().toString());
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            log.error("PDF Rendering Engine Error: {}", e.getMessage());
            e.printStackTrace(); // Added full stack trace
            throw e;
        }
    }
}
