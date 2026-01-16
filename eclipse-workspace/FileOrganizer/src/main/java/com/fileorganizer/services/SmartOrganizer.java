package com.fileorganizer.services;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.apache.tika.Tika;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class SmartOrganizer {
    private final OllamaChatModel textModel;
    private final OllamaChatModel visionModel;
    private final Tika tika = new Tika();

    public SmartOrganizer() {
        this.textModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .build();

        this.visionModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llava")
                .build();
    }

    public String classifyFile(File file) {
        try {
            String type = tika.detect(file);
            String prompt = "Ești un expert în organizarea fișierelor. " +
                            "Răspunde cu UN SINGUR CUVÂNT (numele folderului). " +
                            "Categorii: [Facturi, Vacante, Proiecte, Screenshots, Documents, Arhive].\n";

            String result; 

            if (type.startsWith("image")) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                
             
                var response = visionModel.generate(
                    dev.langchain4j.data.message.UserMessage.from(
                        dev.langchain4j.data.message.ImageContent.from(base64, "image/jpeg"),
                        dev.langchain4j.data.message.TextContent.from(prompt)
                    )
                );
                result = response.content().text();
            } else {
                String content = tika.parseToString(file);
                String snippet = content.substring(0, Math.min(content.length(), 1000));
                
              
                result = textModel.generate(prompt + "Fisier: " + file.getName() + "\nContinut: " + snippet);
            }

            return sanitize(result);

        } catch (Exception e) {
            e.printStackTrace();
            return "Nesortate";
        }
    }

    private String sanitize(String input) {
        if (input == null || input.isBlank()) return "Nesortate";
        String clean = input.trim().split("\\s+")[0].replaceAll("[^a-zA-Z]", "");
        return clean.isEmpty() ? "Nesortate" : clean;
    }
}