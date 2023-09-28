package com.example.clearsolution.controler;

import com.example.clearsolution.dto.User;
import com.example.clearsolution.exception.ClearSolutionException;
import com.example.clearsolution.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Integer userId,
                                           @Valid @RequestBody User updatedUser) {
        return ResponseEntity.ok(userService.updateUser(userId, updatedUser));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<User> updateUserFiled(@PathVariable Integer userId, @RequestBody User updatedUser) {
        return ResponseEntity.ok(userService.updateUserField(userId, updatedUser));
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/range")
    public ResponseEntity<List<User>> searchUsersByBirthDateRange(
            @RequestParam("fromDate") LocalDate fromDate,
            @RequestParam("toDate") LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isBefore(toDate)) {
            List<User> users = userService.getUsersByBirthDateRange(fromDate, toDate);
            return ResponseEntity.ok(users);
        }
        throw new ClearSolutionException
                ("Invalid dates. The 'from' date must be less than the 'to' date.");
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
      return ResponseEntity.ok(userService.getListOfUsers());
    }
}
