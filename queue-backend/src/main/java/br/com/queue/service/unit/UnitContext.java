package br.com.queue.service.unit;

import br.com.queue.dtos.tokenDto.ResponseCurrentToken;
import br.com.queue.infra.unit.UnitNotFoundException;
import br.com.queue.infra.user.UserNotFoundException;
import br.com.queue.repositories.unit.UnitRepository;
import br.com.queue.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnitContext {

    private final UnitRepository unitRepository;
    private final UserRepository userRepository;

    public ResponseCurrentToken getCurrentToken(JwtAuthenticationToken token) {

        String unitId = token.getToken().getClaimAsString("UNIT_ID");
        String role = token.getToken().getClaimAsString("SCOPE");
        String userId = token.getName();

        return new ResponseCurrentToken(
                this.unitRepository.findById(unitId).orElseThrow(
                        () -> new UnitNotFoundException("Unidade não encontrada.")),
                role,
                this.userRepository.findByUserId(userId).orElseThrow(
                        () -> new UserNotFoundException("Usuário não encontrado."))
        );
    }
}
