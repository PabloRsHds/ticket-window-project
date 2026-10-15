package br.com.queue.service.user;

import br.com.queue.dtos.user.ResponseUserDto;
import br.com.queue.dtos.user.create.CreateUserDto;
import br.com.queue.dtos.user.get_user.ResponseUserInfoDto;
import br.com.queue.dtos.user.metrics.ResponseUserDashBoardDto;
import br.com.queue.dtos.user.update.UpdateUserDto;
import br.com.queue.dtos.user.users.ResponseAllUsersDto;
import br.com.queue.entities.department.Department;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;
import br.com.queue.enums.Role;
import br.com.queue.infra.department.DepartmentNotFoundException;
import br.com.queue.infra.user.UserNotFoundException;
import br.com.queue.infra.user.UserValidationException;
import br.com.queue.repositories.department.DepartmentRepository;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
import br.com.queue.repositories.user.UserRepository;
import br.com.queue.service.unit.UnitContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ServiceManagementRepository serviceManagementRepository;
    private final DepartmentRepository departmentRepository;
    private final UnitContext unitContext;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================== CREATE ===========================================================

    @Transactional
    public ResponseUserDto createUser(JwtAuthenticationToken token, CreateUserDto dto) {
        log.info("Criando usuário: {}", dto.username());

        // Verifico em que unidade o usuario está logado,
        // e passo ao usuario que for criado para o sistema
        var currentToken = this.unitContext.getCurrentToken(token);
        this.validateCreateUser(dto);
        var entity = this.toEntity(dto, currentToken.unit());

        log.info("Usuário criado com sucesso: {}, ID: {}", entity.getUsername(), entity.getUserId());
        return this.toResponse(this.userRepository.save(entity));
    }

    public void validateCreateUser(CreateUserDto dto) {
        log.debug("Validando dados para criação do usuário: {}", dto.username());

        var verifyDepartment = this.departmentRepository.existsByDepartmentId(dto.departmentId());
        var verifyUsername = this.userRepository.existsByUsername(dto.username());
        var verifyEmail = this.userRepository.existsByEmail(dto.email());
        var verifyPhone = this.userRepository.existsByPhone(dto.phone());
        var existsByCounterNumberAndDepartment = this.userRepository.existsByCounterNumberAndDepartment(
                dto.counterNumber(),
                dto.departmentId()
        );

        if (!Objects.equals(dto.role(), "ADMIN")
                && !verifyDepartment) {
            throw new DepartmentNotFoundException("Não é possível criar um usuário sem associá-lo a um departamento");
        }

        if (verifyUsername) {
            log.warn("Tentativa de criar usuário com username já existente: {}", dto.username());
            throw new UserValidationException("Este usuário já está cadastrado.");
        }

        if (verifyEmail
                && dto.email() != null) {
            log.warn("Tentativa de criar usuário com e-mail já existente: {}", dto.email());
            throw new UserValidationException("Este e-mail já está cadastrado.");
        }

        if (verifyPhone
                &&  dto.phone() != null) {
            log.warn("Tentativa de criar usuário com telefone já existente: {}", dto.phone());
            throw new UserValidationException("Este telefone já está cadastrado.");
        }

        if (existsByCounterNumberAndDepartment
                && dto.counterNumber() != null) {
            log.warn("Tentativa de criar usuário com guichê já alocado: {}", dto.counterNumber());
            throw new UserValidationException("Já possuí um usuário alocado para este guichê.");
        }

        log.debug("Validação concluída com sucesso para o usuário: {}", dto.username());
    }

    public User toEntity(CreateUserDto dto, Unit unit) {
        log.debug("Convertendo CreateUserDto para entidade User: {}", dto.username());

        Set<ServiceManagement> services =
                serviceManagementRepository.findAllByServiceManagementIdIn(dto.serviceIds());

        var department = this.findDepartmentById(dto.departmentId());

        var entity = new User();
        entity.setUsername(dto.username());
        entity.setName(dto.name());
        entity.setSurname(dto.surname());
        entity.setPhone(this.normalizeOptionalField(dto.phone()));
        entity.setEmail(this.normalizeOptionalField(dto.email()));
        entity.setPassword(passwordEncoder.encode(dto.password()));
        entity.setRole(Role.valueOf(dto.role()));
        entity.setCounterNumber(dto.counterNumber());
        entity.setActive(true);
        entity.setDepartment(department);
        entity.setServices(services);
        entity.setUnit(unit);
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    // ===============================================================================================================

    // ============================================== UPDATE =========================================================
    @Transactional
    public ResponseUserDto updateUser(UpdateUserDto dto) {
        log.info("Atualizando usuário: {}", dto.userId());

        var entity = this.findUser(dto.userId());

        this.validateUpdateUser(dto, entity);
        this.updateEntity(dto, entity);

        log.info("Usuário atualizado com sucesso: {}, ID: {}", entity.getUsername(), entity.getUserId());
        return this.toResponse(this.userRepository.save(entity));
    }

    private void validateUpdateUser(UpdateUserDto dto, User entity) {
        log.debug("Validando dados para atualização do usuário: {}", entity.getUserId());

        if (dto.departmentId() != null
                && !dto.departmentId().isBlank()
                && !this.departmentRepository.existsByDepartmentId(dto.departmentId())) {

            log.warn("Tentativa de atualizar usuário com username já existente: {}", dto.username());
            throw new DepartmentNotFoundException("Este departamento não existe");
        }

        if (dto.username() != null
                && !dto.username().isBlank()
                && !dto.username().equals(entity.getUsername())
                && this.userRepository.existsByUsername(dto.username())) {

            log.warn("Tentativa de atualizar usuário com username já existente: {}", dto.username());
            throw new UserValidationException("Este usuário já está cadastrado.");
        }

        if (dto.email() != null
                && !dto.email().isBlank()
                && !dto.email().equals(entity.getEmail())
                && this.userRepository.existsByEmail(dto.email())) {

            log.warn("Tentativa de atualizar usuário com e-mail já existente: {}", dto.email());
            throw new UserValidationException("Este e-mail já está cadastrado.");
        }

        if (dto.phone() != null
                && !dto.phone().isBlank()
                && !dto.phone().equals(entity.getPhone())
                && this.userRepository.existsByPhone(dto.phone())) {

            log.warn("Tentativa de atualizar usuário com telefone já existente: {}", dto.phone());
            throw new UserValidationException("Este telefone já está cadastrado.");
        }

        if (dto.counterNumber() != null
                && !dto.counterNumber().equals(entity.getCounterNumber())
                && this.userRepository.existsByCounterNumberAndDepartment(
                        dto.counterNumber(), dto.departmentId())) {

            log.warn("Tentativa de atualizar usuário com guichê já alocado: {}", dto.counterNumber());
            throw new UserValidationException("Já possui um usuário alocado para este guichê.");
        }

        log.debug("Validação de atualização concluída para o usuário: {}", entity.getUserId());
    }

    private void updateEntity(UpdateUserDto dto, User entity) {
        log.debug("Atualizando campos do usuário: {}", entity.getUserId());

        boolean hasChanges = false;

        if (dto.departmentId() != null && !dto.departmentId().isBlank()) {
            entity.setDepartment(this.findDepartmentById(dto.departmentId()));
            hasChanges = true;
        }

        if (dto.username() != null && !dto.username().isBlank()) {
            entity.setUsername(dto.username());
            hasChanges = true;
        }

        if (dto.name() != null && !dto.name().isBlank()) {
            entity.setName(dto.name());
            hasChanges = true;
        }

        if (dto.surname() != null && !dto.surname().isBlank()) {
            entity.setSurname(dto.surname());
            hasChanges = true;
        }

        if (dto.phone() != null && !dto.phone().isBlank()) {
            entity.setPhone(dto.phone());
            hasChanges = true;
        }

        if (dto.email() != null && !dto.email().isBlank()) {
            entity.setEmail(dto.email());
            hasChanges = true;
        }

        if (dto.role() != null && !dto.role().isBlank()) {
            entity.setRole(Role.valueOf(dto.role()));
            hasChanges = true;

            if (!Role.ATTENDANT.name().equals(dto.role())) {
                entity.setCounterNumber(null);
                log.debug("Role alterada para {}, removendo contador", dto.role());
            }
        }

        if (dto.counterNumber() != null) {
            entity.setCounterNumber(dto.counterNumber());
            hasChanges = true;
        }

        if (dto.active() != null) {
            entity.setActive(dto.active());
            hasChanges = true;
        }

        if (dto.serviceIds() != null) {
            Set<ServiceManagement> services = new HashSet<>(
                    this.serviceManagementRepository.findAllByServiceManagementIdIn(dto.serviceIds())
            );
            entity.setServices(services);
            hasChanges = true;
            log.debug("Serviços do usuário atualizados: {}", dto.serviceIds().size() + " serviços");
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            entity.setPassword(passwordEncoder.encode(dto.password()));
            hasChanges = true;
            log.debug("Senha do usuário atualizada");
        }

        if (hasChanges) {
            entity.setUpdatedAt(LocalDateTime.now());
            log.debug("Campos do usuário {} foram atualizados", entity.getUserId());
        } else {
            log.debug("Nenhuma alteração detectada para o usuário {}", entity.getUserId());
        }
    }

    // ==============================================================================================================

    // ================================================= DELETE =====================================================
    @Transactional
    public ResponseUserDto deleteUser(String userId) {
        log.info("Deletando usuário: {}", userId);

        var entity = this.findUser(userId);
        var response = this.toResponse(entity);
        this.userRepository.delete(entity);

        log.info("Usuário deletado com sucesso: {}, ID: {}", entity.getUsername(), userId);
        return response;
    }
    // ==============================================================================================================

    // ============================================== ALL USERS =====================================================
    public Page<ResponseAllUsersDto> getAllUsers(JwtAuthenticationToken token, int page, int size, String search) {

        var currentToken = this.unitContext.getCurrentToken(token);

        String normalizedSearch = (search == null || search.isBlank())
                ? null
                : search.trim();

        String departmentId = null;

        if ("MANAGER".equals(currentToken.role())) {
            departmentId = currentToken.user()
                    .getDepartment()
                    .getDepartmentId();
        }

        log.debug("Buscando usuários - unidade: {}, página: {}, tamanho: {}, busca: {}",
                currentToken.unit().getUnitId(), page, size, normalizedSearch);

        return this.userRepository.findAllWithSearch(
                currentToken.unit().getUnitId(),
                departmentId,
                normalizedSearch,
                PageRequest.of(page, size));
    }
    // ==============================================================================================================

    // ========================================== USER INFO =========================================================
    public ResponseUserInfoDto getUserById(String userId) {
        log.debug("Buscando usuário por ID: {}", userId);

        return this.toInfoResponse(this.findUser(userId));
    }
    // ==============================================================================================================

    // ====================================== GET USER BY TOKEN =====================================================
    public ResponseUserInfoDto getUserByToken(JwtAuthenticationToken token) {
        log.debug("Buscando usuário pelo token: {}", token.getName());

        return this.toInfoResponse(this.findUser(token.getName()));
    }
    // ==============================================================================================================

    // ========================================== GET STATISTICS ====================================================
    public ResponseUserDashBoardDto getStatistics(JwtAuthenticationToken token) {
        var currentToken = this.unitContext.getCurrentToken(token);

        String departmentId = null;

        if ("MANAGER".equals(currentToken.role())) {

            var department = currentToken.user().getDepartment();

            if (department == null) {
                throw new DepartmentNotFoundException(
                        "Você não possui um departamento associado"
                );
            }

            departmentId = department.getDepartmentId();
        }

        log.debug("Buscando estatísticas de usuários para unidade: {}", currentToken.unit().getUnitId());
        var countTotalUsersStatistics = this.userRepository.countTotalUsersStatisticsDto(
                currentToken.unit().getUnitId(),
                departmentId);
        var userPercentagesStatistics = this.userRepository.getUserPercentagesStatisticsDto(
                currentToken.unit().getUnitId(),
                departmentId);
        var usersCreatedByMonthStatistics = this.userRepository.countUsersCreatedByMonth(currentToken.unit().getUnitId());
        var countServicesByUsers = this.userRepository.countServicesByUserStatistics(currentToken.unit().getUnitId());
        var countRoleByUsers = this.userRepository.countUsersByRoleStatistics(currentToken.unit().getUnitId());

        log.debug("Estatísticas coletadas: total={}, percentuais ativos={}, percentuais inativos={}, por mês={}",
                countTotalUsersStatistics,
                userPercentagesStatistics != null ? userPercentagesStatistics.percentageActive() : 0,
                userPercentagesStatistics != null ? userPercentagesStatistics.percentageInactive() : 0,
                usersCreatedByMonthStatistics != null ? usersCreatedByMonthStatistics.size() : 0);

        return new ResponseUserDashBoardDto(
                countTotalUsersStatistics,
                userPercentagesStatistics,
                usersCreatedByMonthStatistics,
                countServicesByUsers,
                countRoleByUsers
        );
    }
    // ===============================================================================================================


    // ============================================== IMAGE ==========================================================

    @Transactional
    public void updatePhoto(JwtAuthenticationToken token, MultipartFile photo) throws IOException {

        if (photo == null || photo.isEmpty()) {
            throw new UserValidationException("Nenhuma imagem foi enviada.");
        }

        String contentType = photo.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UserValidationException("O arquivo deve ser uma imagem.");
        }

        if (photo.getSize() > 1024 * 1024) {
            throw new UserValidationException("A imagem deve ter no máximo 1 MB.");
        }

        var user = this.findUser(token.getName());

        user.setPhoto(photo.getBytes());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    public byte[] getPhoto(JwtAuthenticationToken token) {

        User user = this.findUser(token.getName());

        if (user.getPhoto() == null) {
            throw new UserValidationException("Usuário não possui foto.");
        }

        return user.getPhoto();
    }

    // ===============================================================================================================

    // Serviços auxiliares
    public ResponseUserDto toResponse(User entity) {
        var updateAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().format(DATE_FORMATTER)
                : null;

        return new ResponseUserDto(
                entity.getUserId(),
                entity.getUsername(),
                entity.getName(),
                entity.getSurname(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getRole().name(),
                entity.getCounterNumber(),
                entity.getActive(),
                entity.getCreatedAt().format(DATE_FORMATTER),
                updateAt
        );
    }

    public ResponseUserInfoDto toInfoResponse(User entity) {
        var updateAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().format(DATE_FORMATTER)
                : null;

        var departmentId = entity.getDepartment() != null
                ? entity.getDepartment().getDepartmentId()
                : null;

        var departmentName = entity.getDepartment() != null
                ? entity.getDepartment().getName()
                : null;

        return new ResponseUserInfoDto(
                entity.getUserId(),
                entity.getUsername(),
                entity.getName(),
                entity.getSurname(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getRole().name(),
                entity.getCounterNumber(),
                entity.getActive(),
                entity.getCreatedAt().format(DATE_FORMATTER),
                updateAt,
                entity.getServices()
                        .stream()
                        .map(ServiceManagement::getName)
                        .collect(Collectors.toSet()),
                departmentId,
                departmentName,
                entity.getPhoto() != null
        );
    }

    private User findUser(String userId) {
        return this.userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado com ID: {}", userId);
                    return new UserNotFoundException("Usuário não encontrado com ID: " + userId);
                });
    }

    private String normalizeOptionalField(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public Department findDepartmentById(String departmentId) {

        Optional<Department> department = this.departmentRepository
                .findByDepartmentId(departmentId);

        if (department.isPresent()) {
            return department.get();
        }

        throw new DepartmentNotFoundException("Departamento não encontrado");
    }
}