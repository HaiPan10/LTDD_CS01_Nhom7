package com.chattingapplication.springbootserver.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Long id;

    @Size(max = 255)
    @NotBlank(message = "message should not be blank")
    private String content;

    private LocalDateTime createAt;
    private User user;
    private ChatRoom chatRoom;

    public Message(Long id, String content, LocalDateTime createAt, User user) {
        this.id = id;
        this.content = content;
        this.createAt = createAt;
        this.user = user;
    }
}
