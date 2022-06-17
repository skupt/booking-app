package org.example.booking.core.dao;

import org.example.booking.core.util.Storage;
import org.example.booking.intro.model.User;

import java.util.List;

public class UserDao {

    private Storage storage;

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public User getUserById(long userId) {
        return storage.getUserById(userId);
    }

    public User getUserByEmail(String email) {
        return storage.getUserByEmail(email);
    }

    public List<User> getUsersByName(String name, int pageSize, int pageNum) {
        return storage.getUsersByName(name, pageSize, pageNum);
    }

    public User createUser(User user) {
        return storage.createUser(user);
    }

    public User updateUser(User user) {
        return storage.updateUser(user);
    }

    public boolean deleteUser(long userId) {
        return storage.deleteUser(userId);
    }
}
