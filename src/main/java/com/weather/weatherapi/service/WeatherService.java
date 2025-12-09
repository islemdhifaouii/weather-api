package com.weather.weatherapi.service;

import com.weather.weatherapi.config.WebSocketConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WeatherService {

    private final WebClient webClient;
    private final WebSocketConfig webSocketConfig;

    @Value("${weather.apikey}")
    private String apiKey;

    @Value("${weather.city:Monastir}")
    private String city;

    public WeatherService(WebSocketConfig webSocketConfig) {
        this.webSocketConfig = webSocketConfig;
        this.webClient = WebClient.create("https://api.openweathermap.org");
    }

    @Scheduled(fixedRate = 30000)
    public void fetchAndBroadcast() {
        String url = String.format("/data/2.5/weather?q=%s&appid=%s&units=metric", city, apiKey);
        webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(weatherJson -> {
                    System.out.println("Broadcasting: " + weatherJson);
                    webSocketConfig.broadcastToAll(weatherJson);
                });
    }

    public String getWeatherForCity(String cityName) {
        String url = String.format("/data/2.5/weather?q=%s&appid=%s&units=metric", cityName, apiKey);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
    }
}
