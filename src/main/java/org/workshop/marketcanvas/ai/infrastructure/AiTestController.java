package org.workshop.marketcanvas.ai.infrastructure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "*")
@Slf4j
public class AiTestController {
    private final ChatClient chatClient;

    public AiTestController(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestParam String question){
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
}
