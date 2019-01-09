package com.codstrainingapp.trainingapp.services;

import com.codstrainingapp.trainingapp.models.Post;
import com.codstrainingapp.trainingapp.models.User;
import com.codstrainingapp.trainingapp.repositories.UsersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class UserService {

    private UsersRepository usersDao;

    @Autowired
    public UserService(UsersRepository usersDao) {
        this.usersDao = usersDao;
    }

    public List<User> findAll() {
        return usersDao.findAll();
    }

    public User findOne(long id) {
        return usersDao.findOne(id);
    }

    public User findByUsername(String username) {
        return usersDao.findByUsername(username);
    }

    public User findByEmail(String email) {
        return usersDao.findByEmail(email);
    }

    public void save(User user) {
        usersDao.save(user);
    }

    public User update(long id, String username, String email, String bio) {
        User updatedUser = usersDao.findOne(id);
        updatedUser.setUsername(username);
        updatedUser.setEmail(email);
        updatedUser.setBio(bio);
        return updatedUser;
    }

    public void delete(User user) {
        usersDao.delete(user);
    }
    public void deleteSession() {
//        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public ObjectNode toJson(User user) throws JsonProcessingException {

        List<Post> posts = user.getPosts();
        System.out.println(user.getPosts());
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode userNode = mapper.valueToTree(user);
        ArrayNode postArray = mapper.valueToTree(posts);
        userNode.putArray("posts").addAll(postArray);
        JsonNode result = mapper.createObjectNode().set("user", userNode);
        return (ObjectNode) result;
    }

    public Object fromJson(String jsonString, Object valueType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, (JavaType) valueType);
    }


}