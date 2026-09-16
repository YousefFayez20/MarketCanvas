package org.workshop.marketcanvas.ai.application;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.workshop.marketcanvas.ai.domain.AnalysisResponse;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class AiAnalysisService {
    private final ChatClient chatClient;
    private final MarketContextBuilder marketContextBuilder;
    private final Resource promptTemplate;
    public AiAnalysisService(ChatClient.Builder chatClientBuilder, MarketContextBuilder marketContextBuilder,
                             @Value("classpath:prompts/market-analyst.txt")Resource promptTemplate){
        this.chatClient = chatClientBuilder.build();
        this.marketContextBuilder = marketContextBuilder;
        this.promptTemplate = promptTemplate;
    }
    public AnalysisResponse analyze(String question, List<String> tickers){
       String context =  marketContextBuilder.buildContext(tickers);
       String systemPrompt =loadPromptTemplate().replace("{context}", context);
        return chatClient.prompt().user(question).system(systemPrompt).call().entity(AnalysisResponse.class);    }
    public Flux<String> analyzeStream(String question, List<String> tickers){
        String context = marketContextBuilder.buildContext(tickers);
        String systemPrompt = loadPromptTemplate().replace("{context}",context);
        return chatClient.prompt().system(systemPrompt).user(question).user(question).stream().content();
    }
    private String loadPromptTemplate(){
        try(var inputStream = promptTemplate.getInputStream()){
            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e){
            throw new IllegalStateException ("Failed to load AI market analyst prompt template",
                    e
            );
        }
    }


}
