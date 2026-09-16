package org.workshop.marketcanvas.ai.infrastructure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.workshop.marketcanvas.ai.application.AiAnalysisService;
import org.workshop.marketcanvas.ai.domain.AnalysisResponse;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "*")
@Slf4j
public class AiChatController {
    private final ChatClient chatClient;
    private final AiAnalysisService aiAnalysisService;

    public AiChatController(ChatClient.Builder chatClientBuilder, AiAnalysisService aiAnalysisService) {
        this.chatClient = chatClientBuilder.build();
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestParam String question) {
        try {
            String response = chatClient.prompt()
                    .user(question)
                    .call()
                    .content();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate AI content for question: {}", question, e);
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<AnalysisResponse> chat(@RequestBody AnalysisRequest request) {
        try {
            AnalysisResponse analysis = aiAnalysisService.analyze(request.question(), request.tickers());
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Failed to generate AI content for the request: {}", request, e);
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
        }
    }

        @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter stream (@RequestBody AnalysisRequest request){
           SseEmitter emitter = new SseEmitter(120_000L);
           Flux<String> tokens = aiAnalysisService.analyzeStream(request.question(),request.tickers());
           tokens.subscribe(
                   token ->{
                       try{
                           emitter.send(SseEmitter.event().name("token").data(token));

                       } catch (IOException e) {
                           emitter.completeWithError(e);
                       }

                   },
                   error ->{
                       try {
                           emitter.send(SseEmitter.event().name("error").data(error.getMessage()));
                       } catch (IOException e) {
                           emitter.completeWithError(e);
                       }
                   },
                   () ->{
                       try {
                           emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                       } catch (IOException ignored) {}
                       emitter.complete();
                   }
           );
           return emitter;
        }

    }

