package com.example.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@SpringBootApplication
public class AssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssistantApplication.class, args);
    }

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore).build();
    }
}


interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String description) {
}

@Controller
@ResponseBody
class AssistantController {

    private final ChatClient ai;

    AssistantController(
            QuestionAnswerAdvisor questionAnswerAdvisor,
            VectorStore vectorStore,
            ToolCallbackProvider scheduler ,
            DogRepository dogRepository,
            ChatClient.Builder ai) {

        if (false)
            dogRepository.findAll().forEach(dog -> {
                var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                        dog.id(), dog.name(), dog.description()
                ));
                vectorStore.add(List.of(dogument));
            });

        this.ai = ai
                .defaultAdvisors(questionAnswerAdvisor)
                .defaultTools(scheduler)
                .defaultSystem("""
                        you are an AI powered assistant to help people adopt a dog from the adoptions agency named Pooch Palace with locations in Thessaloniki,
                         Taipei, Utrecht, Seoul, Tokyo, Singapore, Paris, Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs availables will be presented below. If there is no information, then return a polite response suggesting wes don't have any dogs available. If somebody asks you about animals, and there's no information in the context, then feel free to source the answer from other places. If somebody asks for a time to pick up the dog, don't ask other questions: simply provide a time by consulting the tools you have available.\s
                        
                        """)
                .build();
    }

    @GetMapping("/ask")
    String ask(@RequestParam String question) {
        return this.ai
                .prompt(question)
                .call()
                .content();
        //            .entity(DogAdoptionSuggestion.class);
    }
}



record DogAdoptionSuggestion(int dogId) {
}