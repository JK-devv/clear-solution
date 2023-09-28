package com.example.clearsolution.service;

import com.example.clearsolution.dto.User;

import java.time.LocalDate;
import java.util.List;

public interface UserService {
    User createUser(User user);
    User updateUser(Integer userId, User updatedUser);
    User updateUserField(Integer userId, User updatedUser);
    void deleteUser(Integer userId);
    List<User> getUsersByBirthDateRange(LocalDate startDate, LocalDate endDate);
    List<User> getListOfUsers();
}
