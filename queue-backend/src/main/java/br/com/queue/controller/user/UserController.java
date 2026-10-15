package br.com.queue.controller.user;

import br.com.queue.dtos.user.ResponseUserDto;
import br.com.queue.dtos.user.create.CreateUserDto;
import br.com.queue.dtos.user.get_user.ResponseUserInfoDto;
import br.com.queue.dtos.user.metrics.ResponseUserDashBoardDto;
import br.com.queue.dtos.user.update.UpdateUserDto;
import br.com.queue.dtos.user.users.ResponseAllUsersDto;
import br.com.queue.entities.user.User;
import br.com.queue.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ResponseUserDto> createUser(
            JwtAuthenticationToken token,
            @RequestBody CreateUserDto dto
    ) {

        var response = this.userService.createUser(token, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping
    public ResponseEntity<ResponseUserDto> update(
            @RequestBody UpdateUserDto dto
    ) {

        var response = this.userService.updateUser(dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(
            value = "/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> updatePhoto(
            JwtAuthenticationToken token,
            @RequestParam("photo") MultipartFile photo
    ) throws IOException {

        userService.updatePhoto(token, photo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(JwtAuthenticationToken token) {

        byte[] photo = userService.getPhoto(token);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(photo);
    }

    @GetMapping
    public ResponseEntity<Page<ResponseAllUsersDto>> getAllUsers(
            JwtAuthenticationToken token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search
    ) {

        var response = this.userService.getAllUsers(token, page, size, search);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseUserInfoDto> getUserById(
            @PathVariable String userId
    ) {

        var response = this.userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/token")
    public ResponseEntity<ResponseUserInfoDto> getUserByToken(
            JwtAuthenticationToken token
    ) {

        var response = this.userService.getUserByToken(token);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseUserDto> delete(@PathVariable String userId) {

        var response = this.userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ResponseUserDashBoardDto> getStatistics(JwtAuthenticationToken token) {

        var response = this.userService.getStatistics(token);
        return ResponseEntity.ok(response);
    }
}