package me.betacat.ai.controller;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    private final OllamaChatClient chatClient;

    private final VectorStore vectorStore;

    @Autowired
    public ChatController(OllamaChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", chatClient.call(message));
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatClient.stream(prompt);
    }


    @GetMapping("/ai/ask")
    public Map ask(@RequestParam(value = "message", defaultValue = "这场比赛谁赢了，比分是多少？") String message) {


        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest
                        .query(message)
                        .withTopK(5));


        String content = "";
        for(Document document : similarDocuments) {
            content += document.getContent() + "";
        }
        System.out.println("相似度内容数量: " + similarDocuments.size());
        System.out.println("相似度内容: \n" + content);

//        String userText = """
//        Tell me about three famous pirates from the Golden Age of Piracy and why they did.
//        Write at least a sentence for each pirate.
//        """;

        Message userMessage = new UserMessage(message);

        String systemText = """
                      你是一位有助于人们查找信息的友好AI助理。
                      你应该在给定的范围内容中回复。如果您无法找到答案,只需说"我不知道"。
                      回答的范围在以下内容中:{documents}。
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("documents", content));

        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));


        List<Generation> response = chatClient.call(prompt).getResults();

        return Map.of("generation", response);
    }




    @GetMapping("/ai/ask2")
    public Map ask2(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {

        String userText = """
        Tell me about three famous pirates from the Golden Age of Piracy and why they did.
        Write at least a sentence for each pirate.
        """;

        Message userMessage = new UserMessage(userText);

        String systemText = """
          You are a helpful AI assistant that helps people find information.
          Your name is {name}
          You should reply to the user's request with your name and reply in chinese and also in the style of a {voice}.
          """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", "萌幻妖姬", "voice", "少女声"));

        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

        List<Generation> response = chatClient.call(prompt).getResults();

        return Map.of("generation", response);
    }
}