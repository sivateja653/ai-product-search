package com.aiproductsearch.search_service.ai;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class HttpClients {

  @Bean("openaiClient")
  @Profile("prod")
  public WebClient openaiClient(EmbeddingPros props) {

    int t = props.openai().timeoutMs() == null ? 4000 : props.openai().timeoutMs();
    HttpClient http =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, t)
            .doOnConnected(
                conn -> {
                  conn.addHandlerLast(new ReadTimeoutHandler(t, TimeUnit.MILLISECONDS));
                  conn.addHandlerLast(new WriteTimeoutHandler(t, TimeUnit.MILLISECONDS));
                })
            .responseTimeout(Duration.ofMillis(t));
    return WebClient.builder()
        .baseUrl(props.openai().baseUrl())
        .clientConnector(new ReactorClientHttpConnector(http))
        .build();
  }
}
