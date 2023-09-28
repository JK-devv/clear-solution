package com.example.clearsolution.service;

import com.example.clearsolution.dto.User;
import com.example.clearsolution.exception.ClearSolutionException;
import com.example.clearsolution.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = UserServiceImpl.class)
@ActiveProfiles("test")
class UserServiceImplTest {
    @Autowired
    private UserServiceImpl userService;
    @MockBean
    private UserRepository userRepository;

    @Test
    void shouldCreateUser() {
        User expected = User.builder()
                .email("test@gmail.com")
                .firstName("First")
                .lastName("Last")
                .phoneNumber("1234567890")
                .birthDate(LocalDate.parse("1999-12-01"))
                .address("some street")
                .build();

        when(userRepository.save(expected))
                .thenReturn(expected);

        User actual = userService.createUser(expected);

        assertEquals(expected, actual);
    }

    @Test
    void shouldUpdateUser() {
        Integer userId = 1;
        User existingUser = User.builder()
                .id(userId)
                .email("oldemail@example.com")
                .lastName("old last name")
                .firstName("old first name")
                .birthDate(LocalDate.parse("1995-11-14"))
                .build();

        User updatedUser = User.builder()
                .email("newemail@example.com")
                .lastName("new last name")
                .firstName("new first name")
                .birthDate(LocalDate.parse("1995-11-14"))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(userId, updatedUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(updatedUser.getEmail(), savedUser.getEmail());
        assertEquals(updatedUser, result);

        Mockito.verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void shouldNotUpdateUserWhenUserNotFound() {
        Integer userId = 1;
        User updatedUser = User.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(
                ClearSolutionException.class,
                () -> userService.updateUser(userId, updatedUser),
                "Can not find user by id: 1");

        Mockito.verify(userRepository).findById(userId);

    }

    @Test
    void shouldUpdateUserField() {
        Integer userId = 1;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setFirstName("oldemail@example.com");

        User updatedUser = new User();
        updatedUser.setEmail("newemail@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUserField(userId, updatedUser);

        Mockito.verify(userRepository).findById(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(updatedUser.getEmail(), savedUser.getEmail());
        assertEquals(updatedUser, result);
    }

    @Test
    void shouldNotUpdateUserFieldAgeLimitNotMet() {
        Integer userId = 1;
        User existingUser = User.builder()
                .id(userId)
                .birthDate(LocalDate.parse("2000-01-01"))
                .build();

        User updatedUser = User.builder()
                .birthDate(LocalDate.parse("2015-01-01"))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertThrows(
                ClearSolutionException.class,
                () -> userService.updateUserField(userId, updatedUser),
                "Date of birth must be in the past"
        );

        Mockito.verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void shouldDeleteUserSuccess() {
        Integer userId = 1;
        userService.deleteUser(userId);
        Mockito.verify(userRepository).deleteById(userId);
    }

    @Test
    void getUsersByBirthDateRange() {
        LocalDate startDate = LocalDate.of(1990, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 12, 31);
        User first = User.builder()
                .id(1)
                .firstName("User 1 ")
                .birthDate(LocalDate.parse("1995-05-10"))
                .build();
        User second = User.builder()
                .id(2)
                .firstName("User 2")
                .birthDate(LocalDate.parse("1998-08-15"))
                .build();
        List<User> usersInDateRange = Arrays.asList(first, second);

        when(userRepository.findByBirthDateBetween(startDate, endDate))
                .thenReturn(usersInDateRange);

        List<User> result = userService.getUsersByBirthDateRange(startDate, endDate);

        assertEquals(usersInDateRange, result);

        Mockito.verify(userRepository).findByBirthDateBetween(startDate, endDate);
    }

    @Test
    void getListOfUsers() {
        User first = User.builder()
                .id(1)
                .firstName("User 1 ")
                .birthDate(LocalDate.parse("1995-05-10"))
                .build();
        User second = User.builder()
                .id(2)
                .firstName("User 2")
                .birthDate(LocalDate.parse("1998-08-15"))
                .build();

        when(userRepository.findAll()).thenReturn(List.of(first, second));

        List<User> listOfUsers = userService.getListOfUsers();
        assertEquals(2,listOfUsers.size());
    }
}