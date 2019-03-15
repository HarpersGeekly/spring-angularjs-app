package com.codstrainingapp.trainingapp.services;

import com.codstrainingapp.trainingapp.models.Comment;
import com.codstrainingapp.trainingapp.models.CommentDTO;
import com.codstrainingapp.trainingapp.models.Post;
import com.codstrainingapp.trainingapp.models.PostDTO;
import com.codstrainingapp.trainingapp.repositories.CommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CommentService {

    private CommentsRepository commentsDao;

    @Autowired
    public CommentService(CommentsRepository commentsDao) {
        this.commentsDao = commentsDao;
    }

    public List<CommentDTO> findAllByPostId(Long id) {
        List<Comment> comments = commentsDao.findAllByPostId(id);
        return getCommentDTOS(comments);
    }

    public List<CommentDTO> findAllByUserId(Long id) {
        List<Comment> comments = commentsDao.findAllByUserId(id);
        return getCommentDTOS(comments);
    }

    public List<CommentDTO> getCommentDTOS(List<Comment> comments) {
        List<CommentDTO> dtos = new ArrayList<>();
        for(Comment c : comments) {
            CommentDTO converted = convertToCommentDTO(c);
            dtos.add(converted);
        }
        return dtos;
    }

    public CommentDTO findOne(Long id) {
        Comment comment = commentsDao.findOne(id);
        return convertToCommentDTO(comment);
    }

    public CommentDTO saveComment(CommentDTO dto) {
        Comment entity = convertToComment(dto);
        commentsDao.saveComment(entity);
        return convertToCommentDTO(entity);
    }

    public void delete(CommentDTO dto) {
        Comment comment = convertToComment(dto);
        commentsDao.delete(comment);
    }

    private CommentDTO convertToCommentDTO(Comment comment){
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setBody(comment.getBody());
        dto.setUser(comment.getUser());
        dto.setDate(comment.getDate());
        return dto;
    }

    private Comment convertToComment(CommentDTO commentDto){
        Comment entity = new Comment();
        entity.setId(commentDto.getId());
        entity.setUser(commentDto.getUser());
        entity.setBody(commentDto.getBody());
        entity.setDate(commentDto.getDate());
        entity.setPost(commentDto.getPost());
        return entity;
    }
}
