package com.chattingapplication.springbootserver.service.implement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.chattingapplication.springbootserver.entity.ChatRoomEntity;
import com.chattingapplication.springbootserver.entity.MessageEntity;
import com.chattingapplication.springbootserver.entity.UserEntity;
import com.chattingapplication.springbootserver.model.ChatRoom;
import com.chattingapplication.springbootserver.model.Message;
import com.chattingapplication.springbootserver.model.User;
import com.chattingapplication.springbootserver.repository.ChatRoomRepository;
import com.chattingapplication.springbootserver.repository.MessageRepository;
import com.chattingapplication.springbootserver.repository.UserRepository;
import com.chattingapplication.springbootserver.service.interfaces.ChatRoomService;
import com.chattingapplication.springbootserver.service.interfaces.MessageService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = Exception.class)
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final MessageRepository messageRepository;

    @Override
    public List<ChatRoom> getChatRooms() {
        return chatRoomRepository.findAll().stream().map(r ->{
            ChatRoom room = new ChatRoom();
            room.setId(r.getId());
            room.setPrivate(r.isPrivate());
            room.setRoomName(r.getRoomName());
            List<Message> messages = messageService.getAllMessages(r.getId());
            if (messages.size() != 0) {
                room.setLatestMessage(messages.get(messages.size() - 1));
            }
            return room;
        }).collect(Collectors.toList());
    }

    // Create a chat room with empty users
    @Override
    public ChatRoom createChatRoom(ChatRoom chatRoom) throws Exception {
        try {
            ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
            BeanUtils.copyProperties(chatRoom, chatRoomEntity);
            chatRoomRepository.save(chatRoomEntity);
            chatRoom.setId(chatRoomEntity.getId());
            return chatRoom;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void deleteChatRoom(Long chatRoomId) throws Exception {
        try {
            ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatRoomId).get();
            chatRoomEntity.getUsers().stream().forEach(u -> {
                u.getChatRooms().remove(chatRoomEntity);
                userRepository.save(u);
            });
            chatRoomEntity.getUsers().clear();
            chatRoomRepository.save(chatRoomEntity);
            chatRoomRepository.delete(chatRoomEntity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String addUsers(Long chatRoomId, List<User> users) throws Exception {
        try {
            ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatRoomId).get();
            Set<UserEntity> userEntities = users.stream().map(u ->{
                return userRepository.findById(u.getId()).get();
            }).collect(Collectors.toSet());

            // adding user entities to a chat room
            chatRoomEntity.setUsers(userEntities);

            // adding chat room to each user 
            userEntities.stream().forEach(us -> {
                us.getChatRooms().add(chatRoomEntity);
            });

            chatRoomRepository.save(chatRoomEntity);
            userRepository.saveAll(userEntities);
            return "Success";
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        
    }

    @Override
    public User addUser(Long chatRoomId, User user) throws Exception {
        try {
            ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatRoomId).get();
            UserEntity userEntity = userRepository.findById(user.getId()).get();
            chatRoomEntity.getUsers().add(userEntity);
            userEntity.getChatRooms().add(chatRoomEntity);
            chatRoomRepository.save(chatRoomEntity);
            userRepository.save(userEntity);
            return user;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Set<ChatRoom> getChatRoomsByUserId(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).get();
        System.out.println(userEntity.getChatRooms());
        return userEntity.getChatRooms().stream().map(cr -> {
            ChatRoom chatRoom = new ChatRoom();
            BeanUtils.copyProperties(cr, chatRoom);
            List<Message> messages = messageService.getAllMessages(cr.getId());
            if (messages.size() != 0) {
                chatRoom.setLatestMessage(messages.get(messages.size() - 1));
            }
            if (cr.isPrivate()) {
                User targetUser = new User();
                BeanUtils.copyProperties(chatRoomRepository.findTargetUser(cr.getId(), userId), targetUser);
                chatRoom.setTargetUser(targetUser);
            }
            return chatRoom;
        }).collect(Collectors.toSet());
    }

    @Override
    public ChatRoom getChatRoomByUsers(Long currentUserId, Long targetUserId, boolean isPrivate) throws Exception {
        ChatRoom chatRoom = new ChatRoom();
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findByUserIds(currentUserId, targetUserId, isPrivate);
        if (chatRoomEntity == null) {
            throw new Exception("no room available!");
        } else {
            BeanUtils.copyProperties(chatRoomRepository.findByUserIds(currentUserId, targetUserId, isPrivate), chatRoom);
            List<Message> messages = messageService.getAllMessages(chatRoomEntity.getId());
            if (messages.size() != 0) {
                chatRoom.setLatestMessage(messages.get(messages.size() - 1));
            }
            User targetUser = new User();
            BeanUtils.copyProperties(userRepository.findById(targetUserId).get(), targetUser);
            chatRoom.setTargetUser(targetUser);
            return chatRoom;
        }
    }

    @Override
    public ChatRoom createPrivateRoom(Long user_id_created, Long user_id_targed, Message message) throws Exception {
        try {
            ChatRoom newChatRoom = createChatRoom(new ChatRoom(String.format("private_%d_%d", user_id_created, user_id_targed), true));
            ChatRoomEntity newChatRoomEntity = chatRoomRepository.findById(newChatRoom.getId()).get();
            UserEntity userEntityCreated = userRepository.findById(user_id_created).get();
            UserEntity userEntityTarget = userRepository.findById(user_id_targed).get();
            userEntityCreated.getChatRooms().add(newChatRoomEntity);
            userEntityTarget.getChatRooms().add(newChatRoomEntity);
            userRepository.save(userEntityCreated);
            userRepository.save(userEntityTarget);
            User targetUser = new User();
            BeanUtils.copyProperties(userEntityTarget, targetUser);
            newChatRoom.setTargetUser(targetUser);

            MessageEntity messageEntity = new MessageEntity();
            message.setCreateAt(LocalDateTime.now());
            BeanUtils.copyProperties(message, messageEntity);
            List<MessageEntity> messageEntities = new ArrayList<>();
            messageEntity.setChatRoom(newChatRoomEntity);
            messageEntity.setUser(userEntityCreated);
            messageEntity.setCreateAt(LocalDateTime.now());
            messageEntities.add(messageEntity);
            chatRoomRepository.save(newChatRoomEntity);
            message.setId(messageRepository.save(messageEntity).getId());

            User createdUser = new User();
            BeanUtils.copyProperties(userEntityCreated, createdUser);
            message.setUser(createdUser);
            newChatRoom.setLatestMessage(message);
            return newChatRoom;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
