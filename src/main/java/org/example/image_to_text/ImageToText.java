package org.example.image_to_text;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.common.dto.Model;
import org.example.common.dto.messages.content.Content;
import org.example.common.dto.messages.content.ImgContent;
import org.example.common.dto.messages.request.Message;
import org.example.common.dto.messages.Role;
import org.example.common.dto.messages.content.TxtContent;
import org.example.common.dto.messages.response.ai.ChatCompletion;
import org.example.common.utils.Constant;
import org.example.common.utils.OpenAIUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class ImageToText {

    private static final String USER_PROMPT = "What is present on this image?";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, InterruptedException {
        // NO need to change smth. This `main` method just to test how it works
        ImageToText imageToText = new ImageToText();

        byte[] imageBytes = getPicture();
        String llmResponse = imageToText.callLLM(getMessages(imageBytes));
        System.out.println(llmResponse);

        String content = imageToText.getContent(llmResponse);
        System.out.println(content);
    }

    public String callLLM(List<Message> messages) throws IOException, InterruptedException {
        ObjectNode request = collectRequestNode(messages);
        System.out.println("Request: \n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
        System.out.println();
        return OpenAIUtils.call(
            Constant.BASE_OPEN_AI_URL + "/chat/completions",
            "application/json",
            HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(request))
        );
    }

    public String getContent(String llmResponse) throws JsonProcessingException {
        ChatCompletion chatCompletion = mapper.readValue(llmResponse, ChatCompletion.class);
        Object content = chatCompletion.choices().getFirst().message().content();
        if (content instanceof String strContent) {
            return strContent;
        }
        return null;
    }

    private static ObjectNode collectRequestNode(List<Message> messages) {
        ObjectNode request = mapper.createObjectNode();
        request.put("model", Model.GPT_4o.getValue());
        request.set("messages", mapper.valueToTree(messages));
        return request;
    }

    /**
     * Provides list with request Messages.
     * <b>Pay attention on the message structure.</b>
     *
     * <pre>
     *   {
     *     "model": "gpt-4o-mini",
     *     "messages": [
     *       {
     *         "role": "user",
     *         "content": [
     *           {
     *             "type": "text",
     *             "text": "What is in this image?"
     *           },
     *           {
     *             "type": "image_url",
     *             "image_url": {
     *                  "url": f"data:image/jpeg;base64,{base64_image}"
     *             }
     *           }
     *         ]
     *       }
     *     ],
     *     "max_tokens": 300
     *   }
     * </pre>
     */
    private static List<Message> getMessages(byte[] imageBytes) {
        return List.of(
            new Message(
                Role.USER,
                List.of(
                    new TxtContent(USER_PROMPT),
                    new ImgContent(String.format("data:image/jpeg;base64, %s", Base64.getEncoder().encodeToString(imageBytes)))
                )
            )
        );
    }

    private static byte[] getPicture() throws IOException {
        // NO need to change smth.
        ClassLoader classLoader = ImageToText.class.getClassLoader();
        File file = new File(classLoader.getResource("cat_in_the_forest.jpg").getFile());
        InputStream inputStream = new FileInputStream(file);
        return inputStream.readAllBytes();
    }

}