package com.example.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

}

@Controller
@ResponseBody
class MyController {

    private final RestClient http;

    MyController(RestClient.Builder http) {
        this.http = http.build();
    }

    @GetMapping("/")
    String delay() {
        var note = Thread.currentThread() + ":";
        var response = this.http.get().uri("http://localhost:9000/delay/5")
                .retrieve()
                .body(String.class);
        note += Thread.currentThread();
        IO.println("\t" + note);
        return response;
    }
}