package com.example.clearsolution.service;

import com.example.clearsolution.dto.User;
import com.example.clearsolution.exception.ClearSolutionException;
import com.example.clearsolution.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.beans.BeanUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Value("${user.age.limit}")
    private int userAgeLimit;

    @Override
    public User createUser(User user) {
        if (isUserAboveAgeLimit(user.getBirthDate())) {
            return userRepository.save(user);
        } else {
            throw new ClearSolutionException
                    (String.format("User must be older %s ", userAgeLimit));
        }
    }

    @Override
    public User updateUser(Integer userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ClearSolutionException(
                                String.format("Can not find user by is : %s", userId)));

        BeanUtils.copyProperties(updatedUser, existingUser, "id");

        return userRepository.save(existingUser);
    }

    @Override
    public User updateUserField(Integer userId, User updatedUser) {
        User existedUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ClearSolutionException(
                                String.format("Can not find user by is : %s", userId)));
        existedUser.setAddress(updatedUser.getAddress() != null
                ? updatedUser.getAddress() : existedUser.getAddress());
        existedUser.setEmail(updatedUser.getEmail() != null
                ? updatedUser.getEmail() : existedUser.getEmail());
        existedUser.setBirthDate(updatedUser.getBirthDate() != null
                && isUserAboveAgeLimit(updatedUser.getBirthDate())
                ? updatedUser.getBirthDate() : existedUser.getBirthDate());
        existedUser.setFirstName(updatedUser.getFirstName() != null
                ? updatedUser.getFirstName() : existedUser.getFirstName());
        existedUser.setLastName(updatedUser.getLastName() != null
                ? updatedUser.getLastName() : existedUser.getLastName());
        existedUser.setPhoneNumber(updatedUser.getPhoneNumber() != null
                ? updatedUser.getPhoneNumber() : existedUser.getPhoneNumber());

        return userRepository.save(existedUser);
    }


    @Override
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public List<User> getUsersByBirthDateRange(LocalDate startDate, LocalDate endDate) {
        return userRepository.findByBirthDateBetween(startDate, endDate);
    }


    @Override
    public List<User> getListOfUsers() {
        return userRepository.findAll();
    }

    private boolean isUserAboveAgeLimit(LocalDate birthDate) {
        Period period = Period.between(birthDate, LocalDate.now());
        if (period.getYears() >= userAgeLimit) {
            return true;
        }
        throw new ClearSolutionException("Date of birth must be in the past");
    }
}

