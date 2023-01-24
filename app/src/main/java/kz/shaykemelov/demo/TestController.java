package kz.shaykemelov.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RestController
public class TestController
{
    private final WebClient webClient;

    @Autowired
    public TestController(
            @Value("${test.base_url}")
            final String baseUrl)
    {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @GetMapping("/google")
    public Mono<String> google()
    {
        return webClient.get()
                .retrieve()
                .bodyToMono(String.class);
    }
}
