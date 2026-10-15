package br.com.queue.service.user;

import br.com.queue.dtos.user.ResponseUserDto;
import br.com.queue.dtos.user.create.CreateUserDto;
import br.com.queue.dtos.user.get_user.ResponseUserInfoDto;
import br.com.queue.dtos.user.metrics.*;
import br.com.queue.dtos.user.update.UpdateUserDto;
import br.com.queue.dtos.user.users.ResponseAllUsersDto;
import br.com.queue.entities.department.Department;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;
import br.com.queue.enums.Role;
import br.com.queue.infra.user.UserNotFoundException;
import br.com.queue.infra.user.UserValidationException;
import br.com.queue.repositories.department.DepartmentRepository;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
import br.com.queue.repositories.user.UserRepository;
import br.com.queue.service.unit.UnitContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ServiceManagementRepository serviceManagementRepository;

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private UnitContext unitContext;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtAuthenticationToken token;

    @Mock
    private MultipartFile photo;

    @InjectMocks
    private UserService userService;

    private User user;
    private Unit unit;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setUnitId("unit-1");

        user = new User();
        user.setUserId("user-1");
        user.setUsername("pablo");
        user.setName("Pablo");
        user.setSurname("Renato");
        user.setPhone("21999999999");
        user.setEmail("pablo@email.com");
        user.setPassword("encoded-password");
        user.setRole(Role.ATTENDANT);
        user.setCounterNumber(1);
        user.setActive(true);
        user.setUnit(unit);
        user.setCreatedAt(LocalDateTime.of(2026, 9, 10, 10, 0));
        user.setUpdatedAt(null);
        user.setServices(new HashSet<>());
        user.setDepartment(new Department());
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void shouldCreateUserSuccessfully() {

        CreateUserDto dto = new CreateUserDto(
                "novo.usuario",
                "Novo",
                "Usuario",
                "21988888888",
                "novo@email.com",
                "123456",
                "ATTENDANT",
                2,
                new HashSet<>(),
                "department123"
        );

        when(departmentRepository.findByDepartmentId("department123"))
                .thenReturn(Optional.of(new Department()));

        when(unitContext.getCurrentToken(token).unit()).thenReturn(unit);
        when(departmentRepository.existsByDepartmentId(dto.departmentId())).thenReturn(true);
        when(userRepository.existsByUsername(dto.username())).thenReturn(false);
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByPhone(dto.phone())).thenReturn(false);
        when(userRepository.existsByCounterNumberAndDepartment(
                dto.counterNumber(), dto.departmentId())).thenReturn(false);

        when(serviceManagementRepository.findAllByServiceManagementIdIn(dto.serviceIds()))
                .thenReturn(Collections.emptySet());

        when(passwordEncoder.encode(dto.password()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseUserDto response = userService.createUser(token, dto);

        assertNotNull(response);
        assertEquals("novo.usuario", response.username());
        assertEquals("Novo", response.name());
        assertEquals("Usuario", response.surname());
        assertEquals("21988888888", response.phone());
        assertEquals("novo@email.com", response.email());
        assertEquals("ATTENDANT", response.role());
        assertEquals(2, response.counterNumber());
        assertEquals("department123", dto.departmentId());
        assertTrue(response.active());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExistsOnCreate() {

        CreateUserDto dto = new CreateUserDto(
                "pablo",
                "Pablo",
                "Renato",
                null,
                "pablo@email.com",
                "123456",
                "ATTENDANT",
                null,
                new HashSet<>(),
                "department123"
        );

        when(departmentRepository.existsByDepartmentId("department123"))
                .thenReturn(true);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(true);

        UserValidationException exception = assertThrows(
                UserValidationException.class,
                () -> userService.validateCreateUser(dto)
        );

        assertEquals(
                "Este usuário já está cadastrado.",
                exception.getMessage()
        );

        verify(userRepository).existsByUsername(dto.username());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsOnCreate() {

        CreateUserDto dto = new CreateUserDto(
                "novo",
                "Novo",
                "Usuario",
                null,
                "pablo@email.com",
                "123456",
                "ATTENDANT",
                null,
                new HashSet<>(),
                "department123"
        );

        when(departmentRepository.existsByDepartmentId("department123"))
                .thenReturn(true);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(userRepository.existsByEmail(dto.email()))
                .thenReturn(true);

        UserValidationException exception = assertThrows(
                UserValidationException.class,
                () -> userService.validateCreateUser(dto)
        );

        assertEquals(
                "Este e-mail já está cadastrado.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExistsOnCreate() {

        CreateUserDto dto = new CreateUserDto(
                "novo",
                "Novo",
                "Usuario",
                "21999999999",
                "novo@email.com",
                "123456",
                "ATTENDANT",
                null,
                new HashSet<>(),
                "department123"
        );

        when(departmentRepository.existsByDepartmentId("department123"))
                .thenReturn(true);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(userRepository.existsByEmail(dto.email()))
                .thenReturn(false);

        when(userRepository.existsByPhone(dto.phone()))
                .thenReturn(true);

        UserValidationException exception = assertThrows(
                UserValidationException.class,
                () -> userService.validateCreateUser(dto)
        );

        assertEquals(
                "Este telefone já está cadastrado.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenCounterAlreadyExistsOnCreate() {

        CreateUserDto dto = new CreateUserDto(
                "novo",
                "Novo",
                "Usuario",
                null,
                "novo@email.com",
                "123456",
                "ATTENDANT",
                1,
                new HashSet<>(),
                "department123"
        );

        when(departmentRepository.existsByDepartmentId("department123"))
                .thenReturn(true);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(userRepository.existsByEmail(dto.email()))
                .thenReturn(false);

        when(userRepository.existsByPhone(null))
                .thenReturn(false);

        when(userRepository.existsByCounterNumberAndDepartment(1, "department123" ))
                .thenReturn(true);

        UserValidationException exception = assertThrows(
                UserValidationException.class,
                () -> userService.validateCreateUser(dto)
        );

        assertEquals(
                "Já possuí um usuário alocado para este guichê.",
                exception.getMessage()
        );
    }

    // =========================================================
    // TO ENTITY
    // =========================================================

    @Test
    void shouldConvertCreateUserDtoToEntity() {

        HashSet<String> serviceIds = new HashSet<>();
        serviceIds.add("service-1");

        CreateUserDto dto = new CreateUserDto(
                "pablo",
                "Pablo",
                "Renato",
                " 21999999999 ",
                " pablo@email.com ",
                "123456",
                "ATTENDANT",
                1,
                serviceIds,
                "department123"
        );

        ServiceManagement service = new ServiceManagement();
        service.setName("Atendimento");

        when(serviceManagementRepository.findAllByServiceManagementIdIn(serviceIds))
                .thenReturn(Set.of(service));

        when(departmentRepository.findByDepartmentId("department123"))
                .thenReturn(Optional.of(new Department()));

        when(passwordEncoder.encode("123456"))
                .thenReturn("encoded");

        User result = userService.toEntity(dto, unit);

        assertEquals("pablo", result.getUsername());
        assertEquals("Pablo", result.getName());
        assertEquals("Renato", result.getSurname());

        assertEquals("21999999999", result.getPhone());
        assertEquals("pablo@email.com", result.getEmail());

        assertEquals("encoded", result.getPassword());
        assertEquals(Role.ATTENDANT, result.getRole());
        assertEquals(1, result.getCounterNumber());

        assertTrue(result.getActive());
        assertEquals(unit, result.getUnit());

        assertNotNull(result.getCreatedAt());
        assertEquals(Set.of(service), result.getServices());
    }

    @Test
    void shouldNormalizeEmptyOptionalFieldsToNull() {

        CreateUserDto dto = new CreateUserDto(
                "pablo",
                "Pablo",
                "Renato",
                "   ",
                "   ",
                "123456",
                "ATTENDANT",
                null,
                new HashSet<>(),
                "department123"
        );

        when(departmentRepository.findByDepartmentId("department123"))
                .thenReturn(Optional.of(new Department()));

        when(serviceManagementRepository.findAllByServiceManagementIdIn(anySet()))
                .thenReturn(Collections.emptySet());

        when(passwordEncoder.encode("123456"))
                .thenReturn("encoded");

        User result = userService.toEntity(dto, unit);

        assertNull(result.getPhone());
        assertNull(result.getEmail());
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void shouldUpdateUserSuccessfully() {

        UpdateUserDto dto = new UpdateUserDto(
                "user-1",
                "pablo.updated",
                "Pablo Updated",
                "Renato Updated",
                "21888888888",
                "updated@email.com",
                "new-password",
                "ATTENDANT",
                false,
                5,
                new HashSet<>(),
                "department123"
        );

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByUsername("pablo.updated"))
                .thenReturn(false);

        when(userRepository.existsByEmail("updated@email.com"))
                .thenReturn(false);

        when(userRepository.existsByPhone("21888888888"))
                .thenReturn(false);

        when(userRepository.existsByCounterNumberAndDepartment(5, "department123"))
                .thenReturn(false);

        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-encoded-password");

        when(serviceManagementRepository.findAllByServiceManagementIdIn(anySet()))
                .thenReturn(Collections.emptySet());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(departmentRepository.findByDepartmentId("department123"))
                .thenReturn(Optional.of(new Department()));

        when(departmentRepository.existsByDepartmentId("department123"))
                .thenReturn(true);

        ResponseUserDto response = userService.updateUser(dto);

        assertEquals("pablo.updated", response.username());
        assertEquals("Pablo Updated", response.name());
        assertEquals("Renato Updated", response.surname());
        assertEquals("21888888888", response.phone());
        assertEquals("updated@email.com", response.email());
        assertEquals(5, response.counterNumber());
        assertFalse(response.active());

        assertNotNull(response.updatedAt());

        assertEquals("new-encoded-password", user.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistOnUpdate() {

        UpdateUserDto dto = new UpdateUserDto(
                "not-found",
                "pablo",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "department123"
        );

        when(userRepository.findByUserId("not-found"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(dto)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExistsOnUpdate() {

        UpdateUserDto dto = new UpdateUserDto(
                "user-1",
                "another-user",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByUsername("another-user"))
                .thenReturn(true);

        UserValidationException exception = assertThrows(
                UserValidationException.class,
                () -> userService.updateUser(dto)
        );

        assertEquals(
                "Este usuário já está cadastrado.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsOnUpdate() {

        UpdateUserDto dto = new UpdateUserDto(
                "user-1",
                null,
                null,
                null,
                null,
                "existing@email.com",
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("existing@email.com"))
                .thenReturn(true);

        UserValidationException exception = assertThrows(
                UserValidationException.class,
                () -> userService.updateUser(dto)
        );

        assertEquals(
                "Este e-mail já está cadastrado.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExistsOnUpdate() {

        UpdateUserDto dto = new UpdateUserDto(
                "user-1",
                null,
                null,
                null,
                "21888888888",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByPhone("21888888888"))
                .thenReturn(true);

        UserValidationException exception = assertThrows(
                UserValidationException.class,
                () -> userService.updateUser(dto)
        );

        assertEquals(
                "Este telefone já está cadastrado.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRemoveCounterWhenRoleChangesFromAttendant() {

        UpdateUserDto dto = new UpdateUserDto(
                "user-1",
                null,
                null,
                null,
                null,
                null,
                null,
                "ADMIN",
                null,
                null,
                null,
                null
        );

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(dto);

        assertEquals(Role.ADMIN, user.getRole());
        assertNull(user.getCounterNumber());

        verify(userRepository).save(user);
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {

        UpdateUserDto dto = new UpdateUserDto(
                "user-1",
                "new-username",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByUsername("new-username"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(dto);

        assertEquals("new-username", user.getUsername());

        assertEquals("Pablo", user.getName());
        assertEquals("Renato", user.getSurname());
        assertEquals("21999999999", user.getPhone());
        assertEquals("pablo@email.com", user.getEmail());
        assertEquals(Role.ATTENDANT, user.getRole());
        assertEquals(1, user.getCounterNumber());
        assertTrue(user.getActive());

        assertNotNull(user.getUpdatedAt());
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void shouldDeleteUserSuccessfully() {

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        ResponseUserDto response = userService.deleteUser("user-1");

        assertNotNull(response);
        assertEquals("user-1", response.userId());
        assertEquals("pablo", response.username());

        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingUser() {

        when(userRepository.findByUserId("not-found"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser("not-found")
        );

        verify(userRepository, never()).delete(any());
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Test
    void shouldGetAllUsersWithSearch() {

        ResponseAllUsersDto dto = new ResponseAllUsersDto(
                "user-1",
                "pablo",
                "pablo@email.com",
                "ATTENDANT",
                true,
                "RH"
        );

        Page<ResponseAllUsersDto> page =
                new PageImpl<>(List.of(dto));

        when(unitContext.getCurrentToken(token).unit())
                .thenReturn(unit);

        when(userRepository.findAllWithSearch(
                eq("unit-1"),
                isNull(),
                eq("pablo"),
                any(PageRequest.class)
        )).thenReturn(page);

        Page<ResponseAllUsersDto> result =
                userService.getAllUsers(token, 0, 10, "  pablo  ");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("pablo", result.getContent().get(0).username());

        verify(userRepository).findAllWithSearch(
                eq("unit-1"),
                isNull(),
                eq("pablo"),
                eq(PageRequest.of(0, 10))
        );
    }

    @Test
    void shouldUseNullSearchWhenSearchIsBlank() {

        Page<ResponseAllUsersDto> page =
                new PageImpl<>(Collections.emptyList());

        when(unitContext.getCurrentToken(token).unit())
                .thenReturn(unit);

        when(userRepository.findAllWithSearch(
                eq("unit-1"),
                isNull(),
                isNull(),
                any(PageRequest.class)
        )).thenReturn(page);

        Page<ResponseAllUsersDto> result =
                userService.getAllUsers(token, 0, 10, "   ");

        assertNotNull(result);

        verify(userRepository).findAllWithSearch(
                eq("unit-1"),
                isNull(),
                isNull(),
                eq(PageRequest.of(0, 10))
        );
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    @Test
    void shouldGetUserByIdSuccessfully() {

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        ResponseUserInfoDto response =
                userService.getUserById("user-1");

        assertNotNull(response);

        assertEquals("user-1", response.userId());
        assertEquals("pablo", response.username());
        assertEquals("Pablo", response.name());
        assertEquals("Renato", response.surname());
        assertEquals("21999999999", response.phone());
        assertEquals("pablo@email.com", response.email());
        assertEquals("ATTENDANT", response.role());
        assertEquals(1, response.counterNumber());
        assertTrue(response.active());

        assertEquals(
                "10/09/2026 10:00",
                response.createdAt()
        );

        assertNull(response.updatedAt());
        assertTrue(response.serviceNames().isEmpty());
        assertFalse(response.hasPhoto());
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistingUser() {

        when(userRepository.findByUserId("not-found"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById("not-found")
        );
    }

    // =========================================================
    // GET USER BY TOKEN
    // =========================================================

    @Test
    void shouldGetUserByTokenSuccessfully() {

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        ResponseUserInfoDto response =
                userService.getUserByToken(token);

        assertNotNull(response);
        assertEquals("user-1", response.userId());
        assertEquals("pablo", response.username());

        verify(userRepository).findByUserId("user-1");
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    @Test
    void shouldGetUserStatisticsSuccessfully() {

        ResponseCountTotalUsersStatisticsDto total =
                new ResponseCountTotalUsersStatisticsDto(
                        10L,
                        8L,
                        2L,
                        2L
                );

        ResponseUserPercentagesStatisticsDto percentages =
                new ResponseUserPercentagesStatisticsDto(
                        new BigDecimal("80.00"),
                        new BigDecimal("20.00")
                );

        List<ResponseUsersCreatedByMonthStatisticsDto> createdByMonth =
                List.of(
                        new ResponseUsersCreatedByMonthStatisticsDto(
                                9,
                                "Setembro",
                                5L
                        )
                );

        List<ResponseServicesByUserStatisticsDto> servicesByUser =
                List.of(
                        new ResponseServicesByUserStatisticsDto(
                                "pablo",
                                3L
                        )
                );

        List<ResponseUsersByRoleStatisticsDto> roleStatistics =
                List.of(
                        new ResponseUsersByRoleStatisticsDto(
                                "ATTENDANT",
                                8L,
                                new BigDecimal("80.00")
                        )
                );

        when(unitContext.getCurrentToken(token).unit())
                .thenReturn(unit);

        when(userRepository.countTotalUsersStatisticsDto("unit-1", null))
                .thenReturn(total);

        when(userRepository.getUserPercentagesStatisticsDto("unit-1", null))
                .thenReturn(percentages);

        when(userRepository.countUsersCreatedByMonth("unit-1"))
                .thenReturn(createdByMonth);

        when(userRepository.countServicesByUserStatistics("unit-1"))
                .thenReturn(servicesByUser);

        when(userRepository.countUsersByRoleStatistics("unit-1"))
                .thenReturn(roleStatistics);

        ResponseUserDashBoardDto result =
                userService.getStatistics(token);

        assertNotNull(result);

        assertEquals(total, result.countTotalUsersStatistics());
        assertEquals(percentages, result.userPercentagesStatistics());
        assertEquals(createdByMonth, result.usersCreatedByMonthStatistics());
        assertEquals(servicesByUser, result.countServicesByUsers());
        assertEquals(roleStatistics, result.countRoleByUsers());

        verify(userRepository).countTotalUsersStatisticsDto("unit-1", null);
        verify(userRepository).getUserPercentagesStatisticsDto("unit-1", null);
        verify(userRepository).countUsersCreatedByMonth("unit-1");
        verify(userRepository).countServicesByUserStatistics("unit-1");
        verify(userRepository).countUsersByRoleStatistics("unit-1");
    }

    // =========================================================
    // PHOTO
    // =========================================================

    @Test
    void shouldUpdatePhotoSuccessfully() throws Exception {

        byte[] photoBytes = {1, 2, 3, 4};

        when(photo.isEmpty())
                .thenReturn(false);

        when(photo.getContentType())
                .thenReturn("image/jpeg");

        when(photo.getSize())
                .thenReturn((long) photoBytes.length);

        when(photo.getBytes())
                .thenReturn(photoBytes);

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        userService.updatePhoto(token, photo);

        assertArrayEquals(photoBytes, user.getPhoto());
        assertNotNull(user.getUpdatedAt());

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenPhotoIsNull() {

        UserValidationException exception =
                assertThrows(
                        UserValidationException.class,
                        () -> userService.updatePhoto(token, null)
                );

        assertEquals(
                "Nenhuma imagem foi enviada.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPhotoIsEmpty() {

        when(photo.isEmpty())
                .thenReturn(true);

        UserValidationException exception =
                assertThrows(
                        UserValidationException.class,
                        () -> userService.updatePhoto(token, photo)
                );

        assertEquals(
                "Nenhuma imagem foi enviada.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenFileIsNotImage() {

        when(photo.isEmpty())
                .thenReturn(false);

        when(photo.getContentType())
                .thenReturn("application/pdf");

        UserValidationException exception =
                assertThrows(
                        UserValidationException.class,
                        () -> userService.updatePhoto(token, photo)
                );

        assertEquals(
                "O arquivo deve ser uma imagem.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPhotoIsLargerThanOneMb() {

        when(photo.isEmpty())
                .thenReturn(false);

        when(photo.getContentType())
                .thenReturn("image/jpeg");

        when(photo.getSize())
                .thenReturn(1024L * 1024L + 1);

        UserValidationException exception =
                assertThrows(
                        UserValidationException.class,
                        () -> userService.updatePhoto(token, photo)
                );

        assertEquals(
                "A imagem deve ter no máximo 1 MB.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldGetPhotoSuccessfully() {

        byte[] photoBytes = {1, 2, 3};

        user.setPhoto(photoBytes);

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        byte[] result =
                userService.getPhoto(token);

        assertArrayEquals(photoBytes, result);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHavePhoto() {

        user.setPhoto(null);

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(user));

        UserValidationException exception =
                assertThrows(
                        UserValidationException.class,
                        () -> userService.getPhoto(token)
                );

        assertEquals(
                "Usuário não possui foto.",
                exception.getMessage()
        );
    }

    // =========================================================
    // RESPONSE CONVERSION
    // =========================================================

    @Test
    void shouldConvertUserToResponse() {

        user.setUpdatedAt(
                LocalDateTime.of(2026, 9, 10, 11, 30)
        );

        ResponseUserDto response =
                userService.toResponse(user);

        assertEquals("user-1", response.userId());
        assertEquals("pablo", response.username());
        assertEquals("Pablo", response.name());
        assertEquals("Renato", response.surname());
        assertEquals("21999999999", response.phone());
        assertEquals("pablo@email.com", response.email());
        assertEquals("ATTENDANT", response.role());
        assertEquals(1, response.counterNumber());
        assertTrue(response.active());

        assertEquals(
                "10/09/2026 10:00",
                response.createdAt()
        );

        assertEquals(
                "10/09/2026 11:30",
                response.updatedAt()
        );
    }

    @Test
    void shouldConvertUserToInfoResponseWithServicesAndPhoto() {

        ServiceManagement service1 = new ServiceManagement();
        service1.setName("Atendimento");

        ServiceManagement service2 = new ServiceManagement();
        service2.setName("Informação");

        user.setServices(
                new HashSet<>(Set.of(service1, service2))
        );

        user.setPhoto(new byte[]{1, 2, 3});

        ResponseUserInfoDto response =
                userService.toInfoResponse(user);

        assertEquals("user-1", response.userId());
        assertEquals("pablo", response.username());
        assertEquals("ATTENDANT", response.role());

        assertEquals(
                Set.of("Atendimento", "Informação"),
                response.serviceNames()
        );

        assertTrue(response.hasPhoto());
    }

    @Test
    void shouldReturnHasPhotoFalseWhenUserHasNoPhoto() {

        user.setPhoto(null);

        ResponseUserInfoDto response =
                userService.toInfoResponse(user);

        assertFalse(response.hasPhoto());
    }
}
