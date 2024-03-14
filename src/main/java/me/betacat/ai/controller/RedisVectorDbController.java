package me.betacat.ai.controller;


import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RedisVectorDbController {

    private final VectorStore vectorStore;

    @Autowired
    public RedisVectorDbController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }


    private int topK = 4;
    private double similarityThreshold = 0.275;


    @GetMapping("/ai/init-vector")
    public String initVector() {
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("country", "UK", "year", 2020)),
                new Document("The World is Big and Salvation Lurks Around the Corner", Map.of()),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("country", "NL", "year", 2023)));

        vectorStore.add(documents);

        return "OK";
    }


    @GetMapping("/ai/search-vector")
    public List<Document> searcheVector(@RequestParam(value = "message", defaultValue = "Spring") String message) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest
                        .query(message)
                        .withTopK(5));
        return results;
    }

    @GetMapping("/ai/search-vector2")
    public List<Document> searcheVector2(@RequestParam(value = "message", defaultValue = "The World") String message) {

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest
                        .query(message)
                        .withTopK(topK)
                        .withSimilarityThreshold(similarityThreshold)
                        .withFilterExpression("country in ['UK', 'NL'] && year >= 2020"));
        return results;
    }


    @GetMapping("/ai/search-vector3")
    public List<Document> searcheVector3(@RequestParam(value = "message", defaultValue = "The World") String message) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        List<Document> results =  vectorStore.similaritySearch(
                SearchRequest
                        .query("The World")
                        .withTopK(topK)
                        .withSimilarityThreshold(similarityThreshold)
                        .withFilterExpression(b.and(
                                b.in("country", "UK", "NL"),
                                b.gte("year", 2021)).build()));
        return results;
    }

}
