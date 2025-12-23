package com.developer.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.developer.dto.response.ResumeDTO;
import com.developer.dto.response.ResumeDTO.Header;
import com.developer.dto.response.ResumeDTO.ProjectItem;
import com.developer.exception.ResumeGenerationException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class PdfGeneratorService {

    private static final Font FONT_HEADER_NAME = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font FONT_HEADER_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 12);
    private static final Font FONT_SECTION_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 11);

    public byte[] generateResumePdf(ResumeDTO resume) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, resume.getHeader());
            addProfessionalSummary(document, resume.getProfessionalSummary());
            addSkillsSection(document, resume.getSkills());
            addProjectsSection(document, resume.getProjects());

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new ResumeGenerationException("Failed to generate resume PDF", e);
        } catch (Exception e) {
            throw new ResumeGenerationException("Unexpected error during resume PDF generation", e);
        }
    }

    private void addHeader(Document document, Header header) throws DocumentException {
        if (header == null) {
            return;
        }

        Paragraph name = new Paragraph(header.getFullName(), FONT_HEADER_NAME);
        name.setSpacingAfter(4f);
        document.add(name);

        if (header.getHeadline() != null && !header.getHeadline().isBlank()) {
            Paragraph headline = new Paragraph(header.getHeadline(), FONT_HEADER_SUBTITLE);
            headline.setSpacingAfter(8f);
            document.add(headline);
        }

        StringBuilder contactLine = new StringBuilder();
        if (header.getEmail() != null) {
            contactLine.append(header.getEmail());
        }
        if (header.getLocation() != null && !header.getLocation().isBlank()) {
            if (!contactLine.isEmpty()) {
                contactLine.append(" | ");
            }
            contactLine.append(header.getLocation());
        }
        if (!contactLine.isEmpty()) {
            Paragraph contact = new Paragraph(contactLine.toString(), FONT_NORMAL);
            contact.setSpacingAfter(4f);
            document.add(contact);
        }

        StringBuilder linksLine = new StringBuilder();
        if (header.getGithubUrl() != null && !header.getGithubUrl().isBlank()) {
            linksLine.append("GitHub: ").append(header.getGithubUrl());
        }
        if (header.getLinkedinUrl() != null && !header.getLinkedinUrl().isBlank()) {
            if (!linksLine.isEmpty()) {
                linksLine.append(" | ");
            }
            linksLine.append("LinkedIn: ").append(header.getLinkedinUrl());
        }
        if (header.getPortfolioUrl() != null && !header.getPortfolioUrl().isBlank()) {
            if (!linksLine.isEmpty()) {
                linksLine.append(" | ");
            }
            linksLine.append("Portfolio: ").append(header.getPortfolioUrl());
        }
        if (!linksLine.isEmpty()) {
            Paragraph links = new Paragraph(linksLine.toString(), FONT_NORMAL);
            links.setSpacingAfter(10f);
            document.add(links);
        } else {
            Paragraph spacer = new Paragraph(" ", FONT_NORMAL);
            spacer.setSpacingAfter(8f);
            document.add(spacer);
        }
    }

    private void addProfessionalSummary(Document document, String summary) throws DocumentException {
        if (summary == null || summary.isBlank()) {
            return;
        }

        Paragraph title = new Paragraph("Professional Summary", FONT_SECTION_TITLE);
        title.setSpacingBefore(4f);
        title.setSpacingAfter(4f);
        document.add(title);

        Paragraph body = new Paragraph(summary, FONT_NORMAL);
        body.setSpacingAfter(8f);
        document.add(body);
    }

    private void addSkillsSection(Document document, java.util.List<String> skills) throws DocumentException {
        if (skills == null || skills.isEmpty()) {
            return;
        }

        Paragraph title = new Paragraph("Skills", FONT_SECTION_TITLE);
        title.setSpacingBefore(4f);
        title.setSpacingAfter(4f);
        document.add(title);

        String skillsLine = String.join(", ", skills);
        Paragraph skillsParagraph = new Paragraph(skillsLine, FONT_NORMAL);
        skillsParagraph.setSpacingAfter(8f);
        document.add(skillsParagraph);
    }

    private void addProjectsSection(Document document, java.util.List<ProjectItem> projects) throws DocumentException {
        if (projects == null || projects.isEmpty()) {
            return;
        }

        Paragraph title = new Paragraph("Projects", FONT_SECTION_TITLE);
        title.setSpacingBefore(4f);
        title.setSpacingAfter(4f);
        document.add(title);

        for (ProjectItem project : projects) {
            addSingleProject(document, project);
        }
    }

    private void addSingleProject(Document document, ProjectItem project) throws DocumentException {
        Paragraph projectTitle = new Paragraph(project.getTitle(), FONT_HEADER_SUBTITLE);
        projectTitle.setSpacingBefore(4f);
        document.add(projectTitle);

        if (project.getRole() != null && !project.getRole().isBlank()) {
            Paragraph role = new Paragraph("Role: " + project.getRole(), FONT_NORMAL);
            role.setSpacingAfter(2f);
            document.add(role);
        }

        if (project.getDescription() != null && !project.getDescription().isBlank()) {
            Paragraph desc = new Paragraph(project.getDescription(), FONT_NORMAL);
            desc.setSpacingAfter(2f);
            document.add(desc);
        }

        if (project.getTechStack() != null && !project.getTechStack().isEmpty()) {
            String techLine = "Tech stack: " + String.join(", ", project.getTechStack());
            Paragraph tech = new Paragraph(techLine, FONT_NORMAL);
            tech.setSpacingAfter(2f);
            document.add(tech);
        }

        StringBuilder linksLine = new StringBuilder();
        if (project.getProjectUrl() != null && !project.getProjectUrl().isBlank()) {
            linksLine.append("Project: ").append(project.getProjectUrl());
        }
        if (project.getGithubRepoUrl() != null && !project.getGithubRepoUrl().isBlank()) {
            if (!linksLine.isEmpty()) {
                linksLine.append(" | ");
            }
            linksLine.append("GitHub: ").append(project.getGithubRepoUrl());
        }
        if (!linksLine.isEmpty()) {
            Paragraph links = new Paragraph(linksLine.toString(), FONT_NORMAL);
            links.setSpacingAfter(6f);
            document.add(links);
        } else {
            Paragraph spacer = new Paragraph(" ", FONT_NORMAL);
            spacer.setSpacingAfter(4f);
            document.add(spacer);
        }
    }
}


