package com.example.clearsolution.controler;

import com.example.clearsolution.dto.User;
import com.example.clearsolution.exception.ClearSolutionException;
import com.example.clearsolution.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest
class UserControllerTest {
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    void createUser() {
        User newUser = User.builder()
                .email("test123@gmail.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userService.createUser(any(User.class))).thenReturn(newUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(newUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(newUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(newUser.getLastName()));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @SneakyThrows
    void shouldCreateUserValidationFailure() {
        User invalidUser = User.builder().build();
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message[0]").value("Email is required"));
    }

    @Test
    @SneakyThrows
    void shouldUpdateUser() {
        User updatedUser = new User();
        updatedUser.setEmail("new.email@gmail.com");
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");

        when(userService.updateUser(any(Integer.class),
                any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(updatedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(updatedUser.getLastName()));

        verify(userService).updateUser(eq(1), any(User.class));
    }

    @Test
    @SneakyThrows
    void updateUserFiled() {
        User updatedUser = new User();
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");

        when(userService.updateUserField(any(Integer.class),
                any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/users/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(updatedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(updatedUser.getLastName()));

        verify(userService).updateUserField(eq(1), any(User.class));
    }

    @Test
    @SneakyThrows
    void shouldNotUpdateUserFiled() {
        String exception = "Date of birth must be in the past";
        User updatedUser = User.builder()
                .birthDate(LocalDate.now().minusYears(17))
                .build();

        when(userService.updateUserField(any(Integer.class), any(User.class)))
                .thenThrow(new ClearSolutionException(exception));

        mockMvc.perform(patch("/api/users/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(exception));

        verify(userService, times(1)).updateUserField(eq(1), any(User.class));
    }

    @Test
    @SneakyThrows
    void deleteUser() {
        doNothing().when(userService).deleteUser(1);
        mockMvc.perform(delete("/api/users/{userId}", 1))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(eq(1));
    }

    @Test
    @SneakyThrows
    void searchUsersByBirthDateRange() {
        User first = User.builder()
                .id(1)
                .firstName("User 1")
                .birthDate(LocalDate.parse("1995-05-10"))
                .build();
        User second = User.builder()
                .id(2)
                .firstName("User 2")
                .birthDate(LocalDate.parse("1998-08-15"))
                .build();
        List<User> userList = Arrays.asList(first, second);

        when(userService.getUsersByBirthDateRange(LocalDate.parse("1990-01-01"),
                LocalDate.parse("1995-05-05"))).thenReturn(userList);

        mockMvc.perform(get("/api/users/range")
                        .param("fromDate", "1990-01-01")
                        .param("toDate", "1995-05-05"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("User 1"))
                .andExpect(jsonPath("$[1].firstName").value("User 2"));

        verify(userService, times(1))
                .getUsersByBirthDateRange(eq(LocalDate.parse("1990-01-01")),
                        eq(LocalDate.parse("1995-05-05")));
    }

    @Test
    @SneakyThrows
    void shouldNorSearchUsersByBirthDateRangeInvalidDates() {

        mockMvc.perform(get("/api/users/range")
                        .param("fromDate", "2022-01-01")
                        .param("toDate", "2021-05-05"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid dates. The 'from' date must be less than the 'to' date."));

        verify(userService, never()).getUsersByBirthDateRange(any(LocalDate.class),
                any(LocalDate.class));
    }

    @Test
    @SneakyThrows
    void getUsers() {
        User first = User.builder()
                .id(1)
                .firstName("User 1")
                .birthDate(LocalDate.parse("1995-05-10"))
                .build();
        User second = User.builder()
                .id(2)
                .firstName("User 2")
                .birthDate(LocalDate.parse("1998-08-15"))
                .build();
        List<User> userList = Arrays.asList(first, second);

        when(userService.getListOfUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("User 1"))
                .andExpect(jsonPath("$[1].firstName").value("User 2"));

        verify(userService).getListOfUsers();
    }
}