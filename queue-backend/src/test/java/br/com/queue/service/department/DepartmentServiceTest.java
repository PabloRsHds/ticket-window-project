package br.com.queue.service.department;

import br.com.queue.dtos.department.create.CreateDepartmentDto;
import br.com.queue.dtos.department.update.UpdateDepartmentDto;
import br.com.queue.dtos.tokenDto.ResponseCurrentToken;
import br.com.queue.entities.department.Department;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.department.DepartmentNotFoundException;
import br.com.queue.repositories.department.DepartmentRepository;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
import br.com.queue.service.unit.UnitContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ServiceManagementRepository serviceManagementRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private JwtAuthenticationToken token;

    @Mock
    private Unit unit;

    @Mock
    private ServiceManagement serviceManagement;

    @Mock
    private ResponseCurrentToken currentToken;

    @InjectMocks
    private DepartmentService service;

    @Test
    void deveCriarDepartamento() {

        CreateDepartmentDto dto = new CreateDepartmentDto(
                "Financeiro",
                "Departamento financeiro"
        );

        Department department = new Department();
        department.setDepartmentId("department-1");
        department.setName("Financeiro");
        department.setDescription("Departamento financeiro");
        department.setActive(true);
        department.setUnit(unit);
        department.setCreatedAt(LocalDateTime.now());
        department.setServices(List.of());

        when(unitContext.getCurrentToken(token)).thenReturn(currentToken);
        when(currentToken.unit()).thenReturn(unit);
        when(departmentRepository.save(any(Department.class)))
                .thenReturn(department);

        var response = service.createDepartment(token, dto);

        assertNotNull(response);
        assertEquals("department-1", response.departmentId());
        assertEquals("Financeiro", response.name());
        assertEquals("Departamento financeiro", response.description());
        assertTrue(response.active());

        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void deveAtualizarDepartamento() {

        Department department = new Department();
        department.setDepartmentId("department-1");
        department.setName("Financeiro");
        department.setDescription("Antiga descrição");
        department.setActive(true);
        department.setCreatedAt(LocalDateTime.now());
        department.setServices(List.of());

        UpdateDepartmentDto dto = new UpdateDepartmentDto(
                "department-1",
                "Novo Financeiro",
                "Nova descrição",
                false
        );

        when(departmentRepository.findByDepartmentId("department-1"))
                .thenReturn(Optional.of(department));

        when(departmentRepository.save(department))
                .thenReturn(department);

        var response = service.updateDepartment(dto);

        assertNotNull(response);
        assertEquals("Novo Financeiro", response.name());
        assertEquals("Nova descrição", response.description());
        assertFalse(response.active());

        verify(departmentRepository).save(department);
    }

    @Test
    void deveBuscarTodosOsDepartamentos() {

        Department department = new Department();
        department.setDepartmentId("department-1");
        department.setName("Financeiro");
        department.setDescription("Departamento financeiro");
        department.setActive(true);
        department.setCreatedAt(LocalDateTime.now());
        department.setServices(List.of());

        Page<Department> page = new PageImpl<>(List.of(department));

        when(unitContext.getCurrentToken(token)).thenReturn(currentToken);
        when(currentToken.unit()).thenReturn(unit);
        when(unit.getUnitId()).thenReturn("unit-1");

        when(departmentRepository.findAllWithSearch(
                eq("unit-1"),
                isNull(),
                any()
        )).thenReturn(page);

        Page<?> response = service.getAllDepartments(
                token,
                0,
                10,
                null
        );

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());

        verify(departmentRepository).findAllWithSearch(
                eq("unit-1"),
                isNull(),
                any()
        );
    }

    @Test
    void deveDeletarDepartamento() {

        Department department = new Department();
        department.setDepartmentId("department-1");
        department.setName("Financeiro");
        department.setDescription("Departamento financeiro");
        department.setActive(true);
        department.setCreatedAt(LocalDateTime.now());
        department.setServices(List.of());

        when(departmentRepository.findByDepartmentId("department-1"))
                .thenReturn(Optional.of(department));

        var response = service.deleteDepartment("department-1");

        assertNotNull(response);
        assertEquals("department-1", response.departmentId());

        verify(departmentRepository).delete(department);
    }

    @Test
    void deveLancarExcecaoAoBuscarDepartamentoInexistente() {

        when(departmentRepository.findByDepartmentId("department-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                DepartmentNotFoundException.class,
                () -> service.updateDepartment(
                        new UpdateDepartmentDto(
                                "department-1",
                                "Financeiro",
                                "Descrição",
                                true
                        )
                )
        );
    }

    @Test
    void deveBuscarEstatisticas() {

        when(unitContext.getCurrentToken(token)).thenReturn(currentToken);
        when(currentToken.unit()).thenReturn(unit);
        when(unit.getUnitId()).thenReturn("unit-1");

        when(departmentRepository.countTotalDepartmentsStatisticsDto("unit-1"))
                .thenReturn(null);

        when(departmentRepository.countServicesByDepartmentStatisticsDto("unit-1"))
                .thenReturn(List.of());

        when(departmentRepository.getDepartmentPercentagesStatisticsDto("unit-1"))
                .thenReturn(null);

        when(departmentRepository.countDepartmentsCreatedByMonth("unit-1"))
                .thenReturn(List.of());

        var response = service.getStatistics(token);

        assertNotNull(response);

        verify(departmentRepository)
                .countTotalDepartmentsStatisticsDto("unit-1");

        verify(departmentRepository)
                .countServicesByDepartmentStatisticsDto("unit-1");

        verify(departmentRepository)
                .getDepartmentPercentagesStatisticsDto("unit-1");

        verify(departmentRepository)
                .countDepartmentsCreatedByMonth("unit-1");
    }
}