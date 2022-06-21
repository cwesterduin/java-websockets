package com.websocket.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.websocket.models.Game;
import com.websocket.models.SessionUser;
import com.websocket.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

@Component
public class GameHandler {

    @Autowired
    private GameRepository gameRepository;

    public void process(WebSocketSession session, Map<Object, Object> message) throws IOException, JSONException {
        String protocol = (String) message.get("protocol");
        switch (protocol) {
            case "create":
                createGame(session, message);
                break;
            case "join":
                joinGame(session, message);
                break;
            case "leave":
                break;
            case "action":
                //only action for now is mouse move
                processAction(session, message);
                break;
        }

    }

    private void processAction(WebSocketSession session, Map<Object, Object> message) throws IOException, JSONException {
        Map<Object, Object> messageBody = (Map<Object, Object>) message.get("body");
        String gameId = (String) messageBody.get("id");
        Optional<Game> game = gameRepository.findById(gameId);
        if (game.isPresent()) {
            for (SessionUser sessionUser : game.get().getSessionUsers()) {
                if (!Objects.equals(sessionUser.getSessionId(), session.getId())) {
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("event", "mousemove");
                    responseJson.put("x", messageBody.get("x"));
                    responseJson.put("y", messageBody.get("y"));
                    responseJson.put("id", session.getId());
                    MyTextWebSocketHandler.sessions.get(sessionUser.getSessionId()).sendMessage(new TextMessage(responseJson.toString()));
                }
            }
        }
    }

    private void joinGame(WebSocketSession session, Map<Object, Object> message) throws IOException, JSONException {
        Map<Object, Object> messageBody = (Map<Object, Object>) message.get("body");
        String gameId = (String) messageBody.get("id");
        Optional<Game> game = gameRepository.findById(gameId);
        if (game.isPresent()) {
            JSONObject responseJson = new JSONObject();

            List<SessionUser> sessionUsers  = game.get().getSessionUsers();
            SessionUser newSessionUser = new SessionUser();
            newSessionUser.setSessionId(session.getId());
            sessionUsers.add(newSessionUser);
            game.get().setSessionUsers(sessionUsers);
            gameRepository.save(game.get());

            responseJson.put("event", "join");
            responseJson.put("userId", session.getId());
            ObjectWriter ow = new ObjectMapper().writer();
            String json = ow.writeValueAsString(game.get().getSessionUsers());
            responseJson.put("userIds",  json);

            for (SessionUser sessionUser : game.get().getSessionUsers()) {
                MyTextWebSocketHandler.sessions.get(sessionUser.getSessionId()).sendMessage(new TextMessage(responseJson.toString()));
            }

        }
    }

    private void createGame(WebSocketSession session, Map<Object, Object> message) throws IOException, JSONException {
        Map<Object, Object> messageBody = (Map<Object, Object>) message.get("body");

        String name = (String) messageBody.get("name");
        UUID gameId = UUID.randomUUID();

        Game game = new Game();
        SessionUser sessionUser = new SessionUser();
        sessionUser.setSessionId(session.getId());
        List<SessionUser> sessionUserList = new ArrayList<>();
        sessionUserList.add(sessionUser);
        game.setName(name);
        game.setId(gameId);
        game.setSessionUsers(sessionUserList);
        gameRepository.save(game);

        JSONObject responseJson = new JSONObject();
        responseJson.put("event", "created");
        responseJson.put("gameId", gameId);

        session.sendMessage(
                new TextMessage(responseJson.toString())
        );

    }

}
