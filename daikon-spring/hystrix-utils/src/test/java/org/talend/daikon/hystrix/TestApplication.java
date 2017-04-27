package org.talend.daikon.hystrix;

import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.daikon.exception.TalendRuntimeException;

@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder.create().build();
    }

    @ControllerAdvice
    public static class TalendExceptionController {

        @ExceptionHandler(TalendRuntimeException.class)
        @ResponseBody
        public String handleError(TalendRuntimeException e) {
            final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes != null && attributes instanceof ServletRequestAttributes) {
                HttpServletResponse response = ((ServletRequestAttributes) attributes).getResponse();
                response.setStatus(e.getCode().getHttpStatus());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            }
            final StringWriter message = new StringWriter();
            e.writeTo(message);
            return message.toString();
        }
    }

}
