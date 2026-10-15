package br.com.queue.service.login;

import br.com.queue.dtos.loginDto.RequestLoginDto;
import br.com.queue.dtos.loginDto.ResponseUserForLogin;
import br.com.queue.dtos.tokenDto.ResponseTokens;
import br.com.queue.entities.user.User;
import br.com.queue.enums.Role;
import br.com.queue.infra.user.UserNotFoundException;
import br.com.queue.infra.user.UserPasswordInvalidException;
import br.com.queue.repositories.unit.UnitRepository;
import br.com.queue.repositories.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LoginService service;

    @Test
    void deveVerificarUsuario() {

        User user = new User();
        user.setUserId("user-1");
        user.setPassword("senha-hash");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmailOrUsername("admin@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456", "senha-hash"))
                .thenReturn(true);

        ResponseUserForLogin result = service.verifyUser(
                "unit-1",
                "admin@email.com",
                "123456"
        );

        assertNotNull(result);
        assertEquals("user-1", result.userId());
        assertEquals(Role.ADMIN.name(), result.role());

        verify(userRepository).findByEmailOrUsername("admin@email.com");
        verify(passwordEncoder).matches("123456", "senha-hash");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {

        when(userRepository.findByEmailOrUsername("teste@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.verifyUser(
                        "unit-1",
                        "teste@email.com",
                        "123456"
                )
        );
    }

    @Test
    void deveLancarExcecaoQuandoSenhaInvalida() {

        User user = new User();
        user.setUserId("user-1");
        user.setPassword("senha-hash");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmailOrUsername("admin@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("senha-errada", "senha-hash"))
                .thenReturn(false);

        assertThrows(
                UserPasswordInvalidException.class,
                () -> service.verifyUser(
                        "unit-1",
                        "admin@email.com",
                        "senha-errada"
                )
        );
    }

    @Test
    void deveFazerLogin() {

        User user = new User();
        user.setUserId("user-1");
        user.setPassword("senha-hash");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmailOrUsername("admin@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456", "senha-hash"))
                .thenReturn(true);

        Jwt jwt = mock(Jwt.class);
        JwtEncoderParameters parameters = mock(JwtEncoderParameters.class);

        JwtEncoder encoder = jwtEncoder;

        when(encoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        when(jwt.getTokenValue())
                .thenReturn("token-123");

        ResponseTokens result = service.login(
                new RequestLoginDto(
                        "unit-1",
                        "admin@email.com",
                        "123456"
                ),
                response
        );

        assertNotNull(result);
        assertEquals("token-123", result.accessToken());

        verify(jwtEncoder, times(2))
                .encode(any(JwtEncoderParameters.class));

        verify(response)
                .addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void deveFazerLogout() {

        service.logout(response);

        verify(response)
                .addHeader(eq("Set-Cookie"), contains("refreshToken="));
    }

    @Test
    void deveFazerRefreshDoToken() {

        Jwt jwt = mock(Jwt.class);

        User user = new User();
        user.setUserId("user-1");
        user.setPassword("senha-hash");
        user.setRole(Role.ADMIN);
        user.setActive(true);

        when(jwtDecoder.decode("refresh-token"))
                .thenReturn(jwt);

        when(jwt.getSubject())
                .thenReturn("user-1");

        when(jwt.getClaimAsString("UNIT_ID"))
                .thenReturn("unit-1");

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        JwtEncoderParameters parameters = mock(JwtEncoderParameters.class);

        Jwt encodedJwt = mock(Jwt.class);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(encodedJwt);

        when(encodedJwt.getTokenValue())
                .thenReturn("new-token");

        ResponseTokens result = service.refreshTokens(
                "refresh-token",
                response
        );

        assertNotNull(result);
        assertEquals("new-token", result.accessToken());

        verify(jwtDecoder).decode("refresh-token");
        verify(userRepository).findByUserId("user-1");
        verify(jwtEncoder, times(2))
                .encode(any(JwtEncoderParameters.class));
    }
}