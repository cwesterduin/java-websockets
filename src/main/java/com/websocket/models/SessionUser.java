package com.websocket.models;

import lombok.Data;

@Data
public class SessionUser {
    private String sessionId;
    private String name;
    private String color;
}
