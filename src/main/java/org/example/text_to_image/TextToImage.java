package org.example.text_to_image;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.dto.Model;
import org.example.common.utils.OpenAIUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public class TextToImage {

    private static final String USER_PROMPT = """
            Create a fantasy forest image which will be a place where elfs can live their life.
            """;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, InterruptedException {
        // NO need to change smth. This `main` method just to test how it works
        TextToImage textToImage = new TextToImage();
        String imageName = UUID.randomUUID() + ".png";

        String imageUrl = textToImage.generateImage(USER_PROMPT);
        System.out.println(imageUrl);

        if (imageUrl != null) {
            textToImage.saveImageToFile(imageUrl, imageName);
            System.out.println("Image saved to: " + imageName);
        } else {
            System.err.println("Failed to generate the image.");
        }
    }

    /**
     * Generates picture by user prompt.
     *
     * @return picture url.
     */
    public String generateImage(String prompt) throws IOException, InterruptedException {
        String requestBody = generateRequestBody(prompt);

        String response = OpenAIUtils.call(
            "https://api.openai.com/v1/images/generations",
            "application/json",
            HttpRequest.BodyPublishers.ofString(requestBody)
        );

        if (response != null) {
            JsonNode jsonResponse = mapper.readTree(response);
            JsonNode dataNode = jsonResponse.get("data");
            if (dataNode != null && dataNode.isArray() && !dataNode.isEmpty()) {
                return dataNode.get(0).get("url").asText();
            }
        }
        return null;
    }

    /**
     * <pre>
     *  curl https://api.openai.com/v1/images/generations \
     *   -H "Content-Type: application/json" \
     *   -H "Authorization: Bearer $OPENAI_API_KEY" \
     *   -d '{
     *     "model": "dall-e-3",
     *     "prompt": "a white siamese cat",
     *     "n": 1,
     *     "size": "1024x1024"
     *   }'
     * </pre>
     */
    private String generateRequestBody(String prompt) throws JsonProcessingException {
        return mapper.writeValueAsString(
            Map.of(
                "model", Model.DALL_E_3.getValue(),
                "prompt", prompt
            )
        );
    }


    public void saveImageToFile(String imageUrl, String fileName) throws IOException {
        // NO need to change smth.
        if (imageUrl == null || !imageUrl.startsWith("http")) {
            throw new IllegalArgumentException("Invalid image url: " + imageUrl);
        }

        String outputPath = "src/main/resources/images/" + fileName;
        Path newFilePath = Paths.get(outputPath);
        Path parentDir = newFilePath.getParent();

        if (!Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        if (!Files.exists(newFilePath)) {
            Files.createFile(newFilePath);
        }

        try (InputStream inputStream = new URL(imageUrl).openStream();
             FileOutputStream fileOutputStream = new FileOutputStream(outputPath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

}
