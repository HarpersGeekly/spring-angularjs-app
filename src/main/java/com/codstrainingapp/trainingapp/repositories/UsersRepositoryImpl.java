package com.codstrainingapp.trainingapp.repositories;

import com.codstrainingapp.trainingapp.models.Post;
import com.codstrainingapp.trainingapp.models.User;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository("usersRepository")
public class UsersRepositoryImpl extends AbstractDao<Long, User> implements UsersRepository {

    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        Criteria criteria = createEntityCriteria();
        List<User> users = (List<User>) criteria.list();
        for(User u : users) {
//            Hibernate.initialize(u.getPosts());
        }
        return users;
    }

    public User findByUsername(String username) {
        Criteria criteria = createEntityCriteria();
        criteria.add(Restrictions.eq("username", username));
        return (User) criteria.uniqueResult();
    }

//    public User findByUsername(String username) {
//        Query query = createCustomQuery("FROM User WHERE username = " + username);
//        System.out.println("user: " + (User) query.list());
//        return (User) query.list();
//    }

    public User findByEmail(String email) {
        Criteria criteria = createEntityCriteria();
        criteria.add(Restrictions.eq("email", email));
        return (User) criteria.uniqueResult();
    }

    public User findOne(long id) {
        //        Hibernate.initialize(user.getPosts());
//        List<Post> posts = user.getPosts();
//        for(Post p : posts) {
//            Hibernate.initialize(p.getPostVotes());
//        }
//        Hibernate.initialize(user.getPostVotes());
        return getByKey(id);
    }

    public void saveUser(User user) {
        save(user);
    }

}
