package br.com.queue.service.schedule;

import br.com.queue.dtos.schedule.create.CreateScheduleDto;
import br.com.queue.dtos.schedule.update.UpdateScheduleDto;
import br.com.queue.dtos.tokenDto.ResponseCurrentToken;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.schedule.Schedule;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.unit.Unit;
import br.com.queue.enums.PriorityLevel;
import br.com.queue.enums.ScheduleStatus;
import br.com.queue.repositories.customer.CustomerRepository;
import br.com.queue.repositories.schedule.ScheduleRepository;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
import br.com.queue.repositories.ticket.TicketRepository;
import br.com.queue.service.unit.UnitContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ServiceManagementRepository serviceManagementRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private JwtAuthenticationToken token;

    @Mock
    private ResponseCurrentToken currentToken;

    @Mock
    private Unit unit;

    @Mock
    private Customer customer;

    @Mock
    private ServiceManagement serviceManagement;

    @InjectMocks
    private SchedulingService service;

    @Test
    void deveCriarAgendamento() {

        CreateScheduleDto dto = new CreateScheduleDto(
                "customer-1",
                "service-1",
                "NORMAL",
                LocalDateTime.of(2026, 9, 10, 10, 0)
        );

        Schedule schedule = new Schedule();

        schedule.setScheduleId("schedule-1");
        schedule.setCustomer(customer);
        schedule.setServiceManagement(serviceManagement);
        schedule.setPriority(PriorityLevel.NORMAL);
        schedule.setScheduledDate(dto.scheduledDate());
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        schedule.setCreatedAt(LocalDateTime.now());

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.of(customer));

        when(serviceManagementRepository.findByServiceManagementId("service-1"))
                .thenReturn(Optional.of(serviceManagement));

        when(scheduleRepository.save(any(Schedule.class)))
                .thenReturn(schedule);

        when(customer.getCustomerId())
                .thenReturn("customer-1");

        when(customer.getName())
                .thenReturn("João");

        when(serviceManagement.getServiceManagementId())
                .thenReturn("service-1");

        when(serviceManagement.getName())
                .thenReturn("Atendimento");

        var response = service.createSchedule(token, dto);

        assertNotNull(response);
        assertEquals("schedule-1", response.scheduleId());

        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void deveAtualizarAgendamento() {

        Schedule schedule = new Schedule();

        schedule.setScheduleId("schedule-1");
        schedule.setCustomer(customer);
        schedule.setServiceManagement(serviceManagement);
        schedule.setPriority(PriorityLevel.NORMAL);
        schedule.setScheduledDate(LocalDateTime.of(2026, 9, 10, 10, 0));
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        schedule.setCreatedAt(LocalDateTime.now());

        UpdateScheduleDto dto = new UpdateScheduleDto(
                "schedule-1",
                null,
                null,
                "PRIORITY",
                null,
                null
        );

        when(scheduleRepository.findByScheduleId("schedule-1"))
                .thenReturn(Optional.of(schedule));

        when(customer.getCustomerId())
                .thenReturn("customer-1");

        when(customer.getName())
                .thenReturn("João");

        when(serviceManagement.getServiceManagementId())
                .thenReturn("service-1");

        when(serviceManagement.getName())
                .thenReturn("Atendimento");

        service.updateSchedule(dto);

        assertEquals(PriorityLevel.PRIORITY, schedule.getPriority());

        verify(scheduleRepository).save(schedule);
    }

    @Test
    void deveBuscarAgendamentoPorId() {

        Schedule schedule = new Schedule();

        schedule.setScheduleId("schedule-1");
        schedule.setCustomer(customer);
        schedule.setServiceManagement(serviceManagement);
        schedule.setPriority(PriorityLevel.NORMAL);
        schedule.setScheduledDate(LocalDateTime.of(2026, 9, 10, 10, 0));
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        schedule.setCreatedAt(LocalDateTime.now());

        when(scheduleRepository.findByScheduleId("schedule-1"))
                .thenReturn(Optional.of(schedule));

        when(customer.getCustomerId())
                .thenReturn("customer-1");

        when(customer.getName())
                .thenReturn("João");

        when(serviceManagement.getServiceManagementId())
                .thenReturn("service-1");

        when(serviceManagement.getName())
                .thenReturn("Atendimento");

        var response = service.getScheduleById("schedule-1");

        assertNotNull(response);
        assertEquals("schedule-1", response.scheduleId());
    }

    @Test
    void deveDeletarAgendamento() {

        Schedule schedule = new Schedule();

        schedule.setScheduleId("schedule-1");
        schedule.setCustomer(customer);
        schedule.setServiceManagement(serviceManagement);
        schedule.setPriority(PriorityLevel.NORMAL);
        schedule.setScheduledDate(LocalDateTime.of(2026, 9, 10, 10, 0));
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        schedule.setCreatedAt(LocalDateTime.now());

        when(scheduleRepository.findByScheduleId("schedule-1"))
                .thenReturn(Optional.of(schedule));

        when(customer.getCustomerId())
                .thenReturn("customer-1");

        when(customer.getName())
                .thenReturn("João");

        when(serviceManagement.getServiceManagementId())
                .thenReturn("service-1");

        when(serviceManagement.getName())
                .thenReturn("Atendimento");

        service.deleteSchedule("schedule-1");

        verify(scheduleRepository).delete(schedule);
    }

    @Test
    void deveBuscarTodosOsAgendamentos() {

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        var page = new PageImpl(List.of());

        when(scheduleRepository.findAllWithSearch(
                eq("unit-1"),
                isNull(),
                any(LocalDate.class),
                any()
        )).thenReturn(page);

        Page<?> response = service.getAllSchedules(
                token,
                0,
                10,
                null,
                LocalDate.of(2026, 9, 10)
        );

        assertNotNull(response);
    }

    @Test
    void deveBuscarEstatisticas() {

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        when(scheduleRepository.countTotalSchedulesStatisticsDto("unit-1"))
                .thenReturn(null);

        when(scheduleRepository.getSchedulePercentagesStatisticsDto("unit-1"))
                .thenReturn(null);

        when(scheduleRepository.countSchedulesCreatedByMonth("unit-1"))
                .thenReturn(List.of());

        when(scheduleRepository.countSchedulesCreatedByWeek("unit-1"))
                .thenReturn(List.of());

        when(scheduleRepository.countSchedulesByDepartment("unit-1"))
                .thenReturn(List.of());

        when(scheduleRepository.countSchedulesByService("unit-1"))
                .thenReturn(List.of());

        when(scheduleRepository.countSchedulesByPriority("unit-1"))
                .thenReturn(List.of());

        when(scheduleRepository.countSchedulesByHour("unit-1"))
                .thenReturn(List.of());

        var response = service.getScheduleStatistics(token);

        assertNotNull(response);
    }
}