package br.com.queue.service.serviceManagement;

import br.com.queue.dtos.serviceManagement.create.CreateServiceManagementDto;
import br.com.queue.dtos.serviceManagement.update.UpdateServiceManagementDto;
import br.com.queue.dtos.tokenDto.ResponseCurrentToken;
import br.com.queue.entities.department.Department;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.unit.Unit;
import br.com.queue.repositories.department.DepartmentRepository;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
import br.com.queue.repositories.user.UserRepository;
import br.com.queue.service.unit.UnitContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceManagementServiceTest {

    @Mock
    private ServiceManagementRepository serviceRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private JwtAuthenticationToken token;

    @Mock
    private Jwt jwt;

    @Mock
    private ResponseCurrentToken currentToken;

    @Mock
    private Unit unit;

    @Mock
    private Department department;

    @InjectMocks
    private ServiceManagementService service;

    @Test
    void deveCriarServico() {

        CreateServiceManagementDto dto =
                new CreateServiceManagementDto(
                        "Atendimento",
                        "ATD",
                        "Atendimento geral",
                        "Financeiro"
                );

        ServiceManagement entity = new ServiceManagement();

        entity.setServiceManagementId("service-1");
        entity.setName("Atendimento");
        entity.setCode("ATD");
        entity.setDescription("Atendimento geral");
        entity.setDepartment(department);
        entity.setActive(true);

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(departmentRepository.findByDepartmentId(dto.departmentId()))
                .thenReturn(Optional.of(department));

        when(token.getToken())
                .thenReturn(jwt);

        when(jwt.getClaimAsString("SCOPE"))
                .thenReturn("USER");

        when(serviceRepository.save(any(ServiceManagement.class)))
                .thenReturn(entity);

        when(department.getDepartmentId())
                .thenReturn("department-1");

        when(department.getName())
                .thenReturn("Financeiro");

        var response = service.createServiceManagement(token, dto);

        assertNotNull(response);

        verify(serviceRepository).save(any(ServiceManagement.class));
    }

    // Att
    @Test
    void deveAtualizarServico() {

        var departmentEntity = new Department();
        departmentEntity.setDepartmentId("department-1");
        departmentEntity.setName("Financeiro");
        departmentEntity.setActive(true);

        var serviceEntity = new ServiceManagement();

        serviceEntity.setServiceManagementId("service-1");
        serviceEntity.setName("Antigo");
        serviceEntity.setCode("OLD");
        serviceEntity.setActive(true);
        serviceEntity.setDepartment(departmentEntity);

        UpdateServiceManagementDto dto =
                new UpdateServiceManagementDto(
                        "service-1",
                        "Novo nome",
                        null,
                        null,
                        null
                );

        when(serviceRepository.findByServiceManagementId("service-1"))
                .thenReturn(Optional.of(serviceEntity));

        service.updateServiceManagement(dto);

        assertEquals("Novo nome", serviceEntity.getName());

        verify(serviceRepository).save(serviceEntity);
    }

    @Test
    void deveDeletarServico() {

        ServiceManagement entity = new ServiceManagement();

        entity.setServiceManagementId("service-1");
        entity.setName("Atendimento");
        entity.setCode("ATD");
        entity.setActive(true);
        entity.setDepartment(department);

        when(serviceRepository.findByServiceManagementId("service-1"))
                .thenReturn(Optional.of(entity));

        when(department.getDepartmentId())
                .thenReturn("department-1");

        when(department.getName())
                .thenReturn("Financeiro");

        service.deleteServiceManagement("service-1");

        verify(serviceRepository)
                .deleteUserServicesByServiceId("service-1");

        verify(serviceRepository)
                .delete(entity);
    }

    @Test
    void deveBuscarServicoPorId() {

        ServiceManagement entity = new ServiceManagement();

        entity.setServiceManagementId("service-1");
        entity.setName("Atendimento");
        entity.setCode("ATD");
        entity.setActive(true);
        entity.setDepartment(department);
        entity.setCreatedAt(java.time.LocalDateTime.now());

        when(serviceRepository.findByServiceManagementId("service-1"))
                .thenReturn(Optional.of(entity));

        when(department.getDepartmentId())
                .thenReturn("department-1");

        when(department.getName())
                .thenReturn("Financeiro");

        var response =
                service.getServiceManagementById("service-1");

        assertNotNull(response);
        assertEquals("service-1", response.serviceManagementId());
        assertEquals("Atendimento", response.name());
    }

    @Test
    void deveListarServicosAtivosParaUsuario() {

        ServiceManagement ativo = new ServiceManagement();

        ativo.setServiceManagementId("service-1");
        ativo.setName("Atendimento");
        ativo.setActive(true);
        ativo.setDepartment(department);

        ServiceManagement inativo = new ServiceManagement();

        inativo.setServiceManagementId("service-2");
        inativo.setName("Cadastro");
        inativo.setActive(false);
        inativo.setDepartment(department);

        when(serviceRepository.findAll())
                .thenReturn(java.util.List.of(ativo, inativo));

        when(department.getName())
                .thenReturn("Financeiro");

        var response =
                service.servicesForCreatedUser();

        assertEquals(1, response.size());
        assertEquals("Atendimento", response.get(0).name());
    }
}