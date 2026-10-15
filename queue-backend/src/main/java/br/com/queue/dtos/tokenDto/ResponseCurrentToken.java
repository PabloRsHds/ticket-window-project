package br.com.queue.dtos.tokenDto;

import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;

public record ResponseCurrentToken(
        Unit unit,
        String role,
        User user
) {
}
