package me.betacat.ai.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
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

    @GetMapping("/ai/ask2")
    @CrossOrigin
    public Flux<ChatResponse> ask2(@RequestParam(value = "message", defaultValue = "这场比赛谁赢了，比分是多少？") String message) {
        log.info("question: " + message);

        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest
                        .query(message)
//                        .withSimilarityThreshold(0.5)
                        .withFilterExpression("project == 'nba'")
        );
        log.info("similarDocuments size: " + similarDocuments.size());

        String content = "";
        int i = 0;
        for (Document document : similarDocuments) {
            content += document.getContent() + "";
            log.info((++i) + "，distance：" + document.getMetadata().get("distance") + ", content: \n" + document.getContent());
        }


        Message userMessage = new UserMessage(message);

        String systemText = """
                      请利用如下上下文的信息回答问题，如果上下文信息中没有帮助,只需说"我不知道"。
                      后面的都是上下文信息：\n
                      {documents}
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("documents", content));

        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

//        List<Generation> response = chatClient.call(prompt).getResults();
//        log.info("\n返回的结果: \n" + response.getFirst().getOutput().getContent());

        return chatClient.stream(prompt);
    }


    @GetMapping("/ai/ask")
    public Map ask(@RequestParam(value = "message", defaultValue = "这场比赛谁赢了，比分是多少？") String message) {
        log.info("question: " + message);

        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest
                        .query(message)
                        .withSimilarityThreshold(0.5)
        );
        log.info("similarDocuments size: " + similarDocuments.size());

        String content = "";
        int i = 0;
        for (Document document : similarDocuments) {
            content += document.getContent() + "";
            log.info((++i) + "，distance：" + document.getMetadata().get("distance") + ", content: \n" + document.getContent());
        }


        Message userMessage = new UserMessage(message);

        String systemText = """
                      请利用如下上下文的信息回答问题，如果上下文信息中没有帮助,只需说"我不知道"。
                      后面的都是上下文信息：\n
                      {documents}
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("documents", content));

        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

        List<Generation> response = chatClient.call(prompt).getResults();
        log.info("\n返回的结果: \n" + response.getFirst().getOutput().getContent());

        return Map.of("generation", response);
    }


    @PostMapping("/ai/askStream")
    @CrossOrigin
    public Flux<ChatResponse> generateVitePressDoc(@RequestBody Map params) {

        String question = MapUtils.getString(params, "question", "");


        log.info("question: " + question);

        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(question)
                        .withFilterExpression("project == 'vitepress'")
        );
        log.info("\nsimilarDocuments size: " + similarDocuments.size());

        String content = "";
        int i = 0;
        for (Document document : similarDocuments) {
            // TODO 需要限制上下文的大小 MAX_CONTEXT_TOKEN
            content += document.getContent() + "";
            log.info((++i) + "，distance：" + document.getMetadata().get("distance") + ", content: \n" + document.getContent());
        }


        // Ask gpt
        String userPrompt = """
                You are a very kindly assistant who loves to help people. Given the following sections from documatation, answer the question using only that information, outputted in markdown format. If you are unsure and the answer is not explicitly written in the documentation, say "Sorry, I don't know how to help with that." Always trying to anwser in the spoken language of the questioner.

                Context sections:
                %s

                Question:
                %s

                Answer as markdown (including related code snippets if available):
                """;
//        String userPrompt = """
//                作为一位乐于助人的助理，您喜欢帮助别人。根据以下文档节，使用 markdown 格式回答问题。如果您不确定，且答案在文档中未明确提及，可以说：“抱歉，我不知道怎么帮助您。”尽量用提问者的口语回答。
//
//                上下文部分：
//                %s
//
//                问题：
//                %s
//
//                Markdown 格式的答案（如果有相关的代码片段）：
//                """;

        Prompt prompt = new Prompt(new UserMessage(String.format(userPrompt, content, question)));


//        Message userMessage = new UserMessage(question);
//
//        String systemText = """
//                      请利用如下上下文的信息回答问题，上下文信息如下：{documents}\n
//                      如果上下文信息中没有帮助,只需说"我不知道"。
//                """;
//        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
//        Message systemMessage = systemPromptTemplate.createMessage(Map.of("documents", content));
//
//        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

//        List<Generation> response = chatClient.call(prompt).getResults();
//        log.info("\n返回的结果: {}", response.getFirst().getOutput().getContent());

        return chatClient.stream(prompt);
    }

}
