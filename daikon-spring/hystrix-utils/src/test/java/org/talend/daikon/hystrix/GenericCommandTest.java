// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.daikon.hystrix;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.function.Function;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;
import org.talend.daikon.exception.TalendRuntimeException;

import com.netflix.hystrix.HystrixCommandGroupKey;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "security.mode=genericCommandTest", "transformation.service.url=", "preparation.service.url=",
        "dataset.service.url=" })
public class GenericCommandTest {

    private static TalendRuntimeException lastException;

    @Value("${local.server.port}")
    public int port;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private HttpClient httpClient;

    private static RuntimeException error(Exception e) {
        lastException = (TalendRuntimeException) e;
        return new RuntimeException(e);
    }

    @After
    public void tearDown() throws Exception {
        lastException = null;
    }

    private TestCommand getCommand(String url, Function<Exception, RuntimeException> errorHandling) {
        return context.getBean(TestCommand.class, url, errorHandling);
    }

    @Test
    public void testSuccess() throws Exception {
        // Given
        final GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/success",
                GenericCommandTest::error);
        // When
        final String result = command.run();
        // Then
        assertThat(result, is("success"));
        assertThat(lastException, nullValue());
    }

    @Test
    public void testAuthenticationToken() throws Exception {
        // Given
        final GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/authentication/token",
                GenericCommandTest::error);
        // When
        final String result = command.run();
        // Then
        assertThat(result, is("#1234"));
        assertThat(lastException, nullValue());
    }

    @Test
    public void testSuccessWithMissingBehavior() throws Exception {
        // Given
        final GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/success_with_unknown",
                GenericCommandTest::error);
        // When
        final String result = command.run();
        // Then
        assertThat(result, nullValue()); // Missing behavior for 202 -> returns null.
        assertThat(lastException, nullValue());
    }

    @Test
    public void testFail_With_400() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/fail_with_400",
                GenericCommandTest::error);
        try {
            // When
            command.run();
        } catch (Exception e) {
            // Then
            // underlying was wrapped in another exception by error() method
            assertThat(e.getCause(), is(lastException));
            // underlying exception is a TDPException
            assertThat(lastException, isA(TalendRuntimeException.class));
            // Underlying exception is expected to be MISSING_ACTION_SCOPE
            assertThat(lastException.getCode().getCode(), is(GenericCommandTestService.TestErrorCodes.A_400_ERROR.getCode()));
            // And status is 400.
            assertThat(lastException.getCode().getHttpStatus(), is(400));
        }
    }

    @Test
    public void testFail_With_404() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/not_found",
                GenericCommandTest::error);
        try {
            // When
            command.run();
        } catch (Exception e) {
            // Then
            // underlying was wrapped in another exception by error() method
            assertThat(e.getCause(), is(lastException));
            // underlying exception is a TDPException
            assertThat(lastException, isA(TalendRuntimeException.class));
            // Underlying exception is expected to be UNEXPECTED_EXCEPTION
            assertThat(lastException.getCode().getCode(), is(InternalErrorCodes.UNEXPECTED_EXCEPTION.getCode()));
            // and thrown because of a 404 error
            assertThat(lastException.getCode().getHttpStatus(), is(404));
        }
    }

    @Test
    public void testFail_With_500() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/fail_with_500",
                GenericCommandTest::error);
        try {
            // When
            command.run();
        } catch (Exception e) {
            // Then
            // underlying was wrapped in another exception by error() method
            assertThat(e.getCause(), is(lastException));
            // underlying exception is a TDPException
            assertThat(lastException, isA(TalendRuntimeException.class));
            // Underlying exception is expected to be UNABLE_TO_SERIALIZE_TO_JSON
            assertThat(lastException.getCode().getCode(), is(GenericCommandTestService.TestErrorCodes.A_500_ERROR.getCode()));
            // And status is 500.
            assertThat(lastException.getCode().getHttpStatus(), is(500));
        }
    }

    @Test
    public void testFail_With_Unknown() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/fail_with_unknown",
                GenericCommandTest::error);
        try {
            // When
            command.run();
        } catch (Exception e) {
            // Then
            // underlying was wrapped in another exception by error() method
            assertThat(e.getCause(), is(lastException));
            // underlying exception is a TDPException
            assertThat(lastException, isA(TalendRuntimeException.class));
            // Underlying exception is expected to be UNEXPECTED_EXCEPTION
            assertThat(lastException.getCode().getProduct(), is(InternalErrorCodes.UNEXPECTED_EXCEPTION.getProduct()));
            assertThat(lastException.getCode().getCode(), is(InternalErrorCodes.UNEXPECTED_EXCEPTION.getCode()));
            // And status is 418.
            assertThat(lastException.getCode().getHttpStatus(), is(418));
        }
    }

    @Test
    public void testPassThrough() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/fail_with_500",
                Defaults.passthrough());
        try {
            // When
            command.run();
        } catch (TalendRuntimeException e) {
            // Then
            // error() wasn't called, lastException must be null
            assertThat(lastException, nullValue());
            // underlying is returned as is (passthrough method).
            assertThat(e.getCode().getCode(), is(GenericCommandTestService.TestErrorCodes.A_500_ERROR.getCode()));
        }
    }

    @Test
    public void testUnexpected() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/unexpected",
                Defaults.passthrough());
        try {
            // When
            command.run();
        } catch (TalendRuntimeException e) {
            // Then error() wasn't called, lastException must be null
            assertThat(lastException, nullValue());
            // underlying is returned as is (passthrough method).
            assertThat(e.getCode().getCode(), is(InternalErrorCodes.UNEXPECTED_EXCEPTION.getCode()));
            // TODO: this was commented since TDSPException overrides TalendRuntimeException#writeTo
            // final Iterator<Map.Entry<String, Object>> entries = e.getContext().entries().iterator();
            // assertThat(entries.hasNext(), is(true));
            // final Map.Entry<String, Object> next = entries.next();
            // assertThat(next.getKey(), is("message"));
            // assertThat(String.valueOf(next.getValue()), is("Unable to execute an operation"));
        }
    }

    // Test command
    @Component
    @Scope("prototype")
    private static class TestCommand extends GenericCommand<String> {

        protected TestCommand(String url, Function<Exception, RuntimeException> errorHandling) {
            super(HystrixCommandGroupKey.Factory.asKey("dataset"));
            execute(() -> new HttpGet(url));
            onError(errorHandling);
            on(HttpStatus.OK).then(Defaults.asString());
        }
    }

    @Component
    @ConditionalOnProperty(name = "security.mode", havingValue = "genericCommandTest", matchIfMissing = false)
    private static class TestSecurity implements HttpSecurityProvider {

        @Override
        public void addSecurityHeaders(HttpRequestBase request) {
            request.addHeader(AUTHORIZATION, "#1234");
        }
    }
}