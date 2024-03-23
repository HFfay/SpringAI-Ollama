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
        System.out.println("问题: " + message);

        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest
                        .query(message));
        System.out.println("\n相似度内容数量: " + similarDocuments.size());

        String content = "";
        int i = 0;
        for(Document document : similarDocuments) {
            content += document.getContent() + "";
            System.out.println((++i) + "，相似度：" + document.getMetadata().get("vector_score") + ", 内容: \n" + document.getContent());
        }


        Message userMessage = new UserMessage(message);

        String systemText = """
                      请利用如下上下文的信息回答问题，上下文信息如下：{documents}\n
                      如果上下文信息中没有帮助,只需说"我不知道"。
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("documents", content));

        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

        List<Generation> response = chatClient.call(prompt).getResults();
        System.out.println("\n返回的结果: \n" + response.getFirst().getOutput().getContent());

        return Map.of("generation", response);
    }


}