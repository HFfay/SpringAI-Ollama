package me.betacat.ai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class EmbeddingController {

    private final EmbeddingClient embeddingClient;

    @Autowired
    public EmbeddingController(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingClient.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }




    @PostMapping("/ai/upload")
    public Map upload(@RequestBody Map params) {

        String operation = params.get("operation");
        const { operation, project = 'default' } = params;

        if (!["add", "delete", "clean", "generate"].includes(operation)) {
            log.info("Operation {} is not supported. Abort.", operation);
            context.status(400);
            return Map.of("error", "Operation {} is not supported. Abort.");
        }
        if (operation === 'clean') {
            // Delete all the stored pages
            await PagesTable.where({ project }).delete();
            return Map.of("ok", 1);
        }
        if (operation === 'generate') {
            await generateEmbeddings(project);
            return Map.of("ok", 1);
        }

        const { path, title = '', content = '' } = params;
        if (!path) {
            console.log('Missing param `path`. Abort.');
            context.status(400);
            return Map.of("error", "You are missing param `path`. Abort.");
        }

        if (operation === 'delete') {
            // Delete single page
            await PagesTable.where({ project, path }).delete();
            return Map.of("ok", 1);
        }




        // Generate checksum for the page, so we can determine if this page is changed
          const checksum = crypto.createHash('md5').update(content).digest('hex');
          const existed = await PagesTable.where({
                        project,
                        path,
                }).findOne();

        if (existed) {
            if (existed.checksum === checksum) {
                console.log('This page\'s content is still fresh. Skip regenerating.');
                return Map.of("ok", 1);
            } else {
                // Delete the exist one since we will regenerate it
                await PagesTable.where({ project, path }).delete();
            }
        }

          const chunks = getContentChunks(content);
          const pagesToSave = chunks.map((chunk, index) => ({
                        project,
                        path,
                        title,
                        checksum,
                        chunkIndex: index,
                        content: chunk,
                        embedding: null,
          }))

        // Save the result to database
        for (let i = 0; i < pagesToSave.length; i += 100) {
            await PagesTable.save(pagesToSave.slice(i, i + 100));
        }


        return Map.of("ok", 1);
    }

    private static final int MAX_TOKEN_PER_CHUNK = 8191;

    // Split the page content into chunks base on the MAX_TOKEN_PER_CHUNK
    private String getContentChunks(String content) {
          const encoded = tokenizer.encode(content);
          const tokenChunks = encoded.reduce(
                        (acc, token) => (
                        acc[acc.length - 1].length < MAX_TOKEN_PER_CHUNK
                                ? acc[acc.length - 1].push(token)
                                : acc.push([token]),
                        acc
            ),
            [[]],
          );
        return tokenChunks.map(tokens => tokenizer.decode(tokens));
    }



    static final String param = """
     {"operation":"add",
     "path":"README.md",
     "content":"# Documate Vitepress Starter\n\nAn example shows how to integrate Documate into Vitepress site. You can also use this as a template to start a new
 project.\n"}
""";


    private static final String context = """
            {"responseHeader":{},"cookies":{},"responseCookies":[],"code":400,"method":"POST","path":"/upload","trigger":"HTTP","rawBody":{"type":"Buffer","data":[123,34,111,112,101,114,97,116,105,111,110,34
            ,58,34,97,100,100,34,44,34,112,97,116,104,34,58,34,82,69,65,68,77,69,46,109,100,34,44,34,99,111,110,116,101,110,116,34,58,34,35,32,68,111,99,117,109,97,116,101,32,86,105,116,101,112,114,101,115,115,32,83,11
            6,97,114,116,101,114,92,110,92,110,65,110,32,101,120,97,109,112,108,101,32,115,104,111,119,115,32,104,111,119,32,116,111,32,105,110,116,101,103,114,97,116,101,32,68,111,99,117,109,97,116,101,32,105,110,116,
            111,32,86,105,116,101,112,114,101,115,115,32,115,105,116,101,46,32,89,111,117,32,99,97,110,32,97,108,115,111,32,117,115,101,32,116,104,105,115,32,97,115,32,97,32,116,101,109,112,108,97,116,101,32,116,111,32
            ,115,116,97,114,116,32,97,32,110,101,119,32,112,114,111,106,101,99,116,46,92,110,34,125]},"host":"f3v34sknbs.us.aircode.run","url":"/upload","protocol":"https","query":{},"headers":{"host":"production-chfky
            p-dcdwwryubx.us-west-1-vpc.fcapp.run","user-agent":"axios/1.6.8","content-length":"208","accept":"application/json, text/plain, */*","accept-encoding":"gzip, compress, deflate, br","content-type":"applicati
            on/json","x-aircode-request-id":"2024032709390735022f831f074","x-forwarded-for":"116.232.52.122, 172.30.159.252","x-forwarded-host":"f3v34sknbs.us.aircode.run","x-forwarded-path":"/upload","x-forwarded-port
            ":"443","x-forwarded-proto":"https, https","x-real-ip":"116.232.52.122"}}
                        
            """;

}
