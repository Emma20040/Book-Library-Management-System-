package com.library.management_system.services;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public abstract class AbstractBaseReportService implements ReportService {

    protected final Configuration freemarkerConfiguration;

    @Autowired
    protected AbstractBaseReportService(Configuration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    @Override
    public byte[] getReport() {
        String template = getTemplate();
        Map<String, Object> variables = getVariables();
        String html = getHtml(template, variables);
        html = fixHtmlTags(html);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private String fixHtmlTags(String html) {
        return html.replaceAll("<meta(.*?)>", "<meta$1/>");
    }

    protected String getHtml(String template, Map<String, Object> variables) {
        try {
            Template configTemplate = freemarkerConfiguration.getTemplate(template);
            try (StringWriter stringWriter = new StringWriter()) {
                configTemplate.process(variables, stringWriter);

                return stringWriter.toString();
            }
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Failed to generate HTML from template", e);
        }
    }
}
