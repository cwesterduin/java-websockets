package com.websocket.handlers;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MyTextWebSocketHandler extends TextWebSocketHandler {

    public static final HashMap<String, WebSocketSession> sessions = new HashMap<>();

    @Autowired
    GameHandler gameHandler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        JSONObject responseJson = new JSONObject();
        responseJson.put("event", "connected");
        responseJson.put("id", session.getId());
        session.sendMessage(new TextMessage(responseJson.toString()));
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        super.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        Map<Object, Object> jsonMessage = new Gson().fromJson(message.getPayload(), Map.class);
        gameHandler.process(session, jsonMessage);
    }

    public HashMap<String, WebSocketSession> getSessions() {
        return sessions;
    }
}
