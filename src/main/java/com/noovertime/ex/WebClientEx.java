package com.noovertime.ex;


import io.netty.channel.ChannelOption;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@SpringBootApplication
public class WebClientEx {
  // 참고
  // (1) https://www.baeldung.com/spring-5-webclient
  // (2) https://garyj.tistory.com/22
  //
  public static void main(String[] args) {
    final String baseUrl = "https://m.naver.com";

    // 세부설정
    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
        .responseTimeout(Duration.ofMillis(2000));

    // 기본 메모리 크기가 4K (262144 bits) 라서 이 보다 큰 결과가 오면 에러남
    // 테스트를 위해 길이제한 없도록 설정
    ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
        .build();

    // instance 생성
    WebClient client = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector( httpClient ))
        .exchangeStrategies(exchangeStrategies)
        .baseUrl(baseUrl)
        .build();

    // 요청 생성
    RequestBodyUriSpec requestBodyUriSpec = client.method(HttpMethod.GET);
    requestBodyUriSpec.accept(MediaType.TEXT_HTML);
    requestBodyUriSpec.acceptCharset(StandardCharsets.UTF_8);

    // 응답 처리
    Mono<String> responseMono = requestBodyUriSpec.retrieve()
            // 4xx, 5xx 응답이면 예외 발생
            .bodyToMono(String.class);

    // 1초간 대기
    String responseStr = responseMono.block( Duration.ofSeconds(1));

    // 출력
    log.info("응답 : {}", responseStr == null ? null : responseStr.substring(0, 100) + "........생략.. ");
  }
}