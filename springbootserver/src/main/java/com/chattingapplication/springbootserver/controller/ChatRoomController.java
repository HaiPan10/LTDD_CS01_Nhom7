package com.chattingapplication.springbootserver.controller;

import java.util.List;
import java.util.Set;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chattingapplication.springbootserver.model.ChatRoom;
import com.chattingapplication.springbootserver.model.Message;
import com.chattingapplication.springbootserver.model.User;
import com.chattingapplication.springbootserver.service.interfaces.ChatRoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/chat_room")
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @PostMapping("/create_chat_room")
    public ChatRoom createChatRoom(@Valid @RequestBody ChatRoom chatRoom,
            BindingResult result) throws Exception {
        if (result.hasErrors()) {
            throw new Exception(result.getAllErrors().get(0).getDefaultMessage());
        }

        return chatRoomService.createChatRoom(chatRoom);
    }

    @GetMapping
    public List<ChatRoom> getChatRooms() {
        return chatRoomService.getChatRooms();
    }

    @PostMapping(path = "{room_id}/add_users")
    public String addUsers(@PathVariable(name = "room_id") Long chatRoomId, @RequestBody List<User> users)
            throws Exception {
        try {
            return chatRoomService.addUsers(chatRoomId, users);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

     @PostMapping(path = "/create_private_room/{user_id_created}/{user_id_targed}")
    public ChatRoom createPrivateRoom(@PathVariable Long user_id_created, @PathVariable Long user_id_targed)
            throws Exception {
        try {
            return chatRoomService.createPrivateRoom(user_id_created, user_id_targed);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @PostMapping(path = "{room_id}/add_user")
    public User addUser(@PathVariable(name = "room_id") Long chatRoomId,
            @RequestBody User user) throws Exception {
        try {
            System.out.println("target user: " + user);
            return chatRoomService.addUser(chatRoomId, user);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @DeleteMapping(path = "{room_id}")
    public void deleteChatRoom(@PathVariable(name = "room_id") Long chatRoomId) 
            throws Exception {
        try {
            chatRoomService.deleteChatRoom(chatRoomId);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping(path = "{user_id}")
    public Set<ChatRoom> getChatRoomsByUserId(@PathVariable(name = "user_id") Long userId){
        return chatRoomService.getChatRoomsByUserId(userId);
    }

    @GetMapping(path = "{room_id}/messages")
    public Set<Message> getMessagesByChatRoomId(@PathVariable(name = "room_id") Long chatRoomId) throws Exception {
        try {
            return chatRoomService.getMessagesByChatRoomId(chatRoomId);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping(path = "{current_user_id}/{target_user_id}/{is_private}")
    public ChatRoom getChatRoomByUsers(@PathVariable(name = "current_user_id") Long currentUserId, @PathVariable(name = "target_user_id") Long targetUserId, @PathVariable(name = "is_private") boolean isPrivate) throws Exception {
        try {
            return chatRoomService.getChatRoomByUsers(currentUserId, targetUserId, isPrivate);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
