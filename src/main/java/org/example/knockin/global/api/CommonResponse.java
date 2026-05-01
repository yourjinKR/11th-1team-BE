package org.example.knockin.global.api;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.http.*;
import org.springframework.util.Assert;

@Getter
public class CommonResponse<T> {
    private HttpStatusCode status;
    private T body;
    private final ErrorResponse error;

    public CommonResponse(T body, HttpStatusCode status, ErrorResponse error) {
        this.error = error;
        this.body = body;
        this.status = status;
    }

    public interface BodyBuilder {
        <T> CommonResponse<T> body(@Nullable T body);
    }

    public static BodyBuilder status(HttpStatusCode status) {
        Assert.notNull(status, "HttpStatusCode must not be null");
        return new CommonResponse.DefaultBuilder(status);
    }

    public static BodyBuilder status(int status) {
        return new CommonResponse.DefaultBuilder(status);
    }

    private static class DefaultBuilder implements BodyBuilder {
        private final HttpStatusCode statusCode;

        public DefaultBuilder(int statusCode) {
            this(HttpStatusCode.valueOf(statusCode));
        }

        public DefaultBuilder(HttpStatusCode statusCode) {
            this.statusCode = statusCode;
        }

        public <T> CommonResponse<T> body(@Nullable T body) {
            return new CommonResponse<>(body, this.statusCode, null);
        }
    }
}
