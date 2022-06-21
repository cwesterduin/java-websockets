package com.websocket.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;
import java.util.UUID;

@RedisHash("Game")
@Data
public class Game {
    @Id private UUID id;
    private String name;
    private List<SessionUser> sessionUsers;
}
