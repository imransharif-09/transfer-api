package com.revolut.transfer.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ApiResponse {
    private final String body;
    private final int status;

    public ApiResponse(final int status) {
        this(status, null);
    }

    public ApiResponse(final int status, final String body) {
        this.status = status;
        this.body = body;
    }

    public JsonElement jsonElement() {
        return new JsonParser().parse(body);
    }

    public JsonElement getData() {
        return new JsonParser().parse(body).getAsJsonObject().get("data");
    }

    public int getStatus() {
        return status;
    }
}
