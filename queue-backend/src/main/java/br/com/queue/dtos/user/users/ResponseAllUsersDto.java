package br.com.queue.dtos.user.users;

public record ResponseAllUsersDto(
        String userId,
        String username,
        String email,
        String role,
        Boolean active,
        String departmentName
) {
}
