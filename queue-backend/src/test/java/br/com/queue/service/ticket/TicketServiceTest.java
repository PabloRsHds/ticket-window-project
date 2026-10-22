package br.com.queue.service.ticket;

import br.com.queue.dtos.ticket.ResponseTicketDto;
import br.com.queue.dtos.ticket.allTickets.ResponseAllTicketsDto;
import br.com.queue.dtos.ticket.attendance.ResponseTicketsForAttendance;
import br.com.queue.dtos.ticket.create.CreateTicketDto;
import br.com.queue.dtos.ticket.finishTicket.FinishTicketDto;
import br.com.queue.dtos.tokenDto.ResponseCurrentToken;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.schedule.Schedule;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;
import br.com.queue.enums.PriorityLevel;
import br.com.queue.enums.TicketStatus;
import br.com.queue.infra.ticket.TicketNotFoundException;
import br.com.queue.repositories.attendance.AttendanceRepository;
import br.com.queue.repositories.customer.CustomerRepository;
import br.com.queue.repositories.schedule.ScheduleRepository;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
import br.com.queue.repositories.ticket.TicketRepository;
import br.com.queue.repositories.user.UserRepository;
import br.com.queue.service.unit.UnitContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ServiceManagementRepository serviceManagementRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private JwtAuthenticationToken token;

    @Mock
    private ResponseCurrentToken currentToken;

    @Mock
    private Unit unit;

    @InjectMocks
    private TicketService service;


    @Test
    void deveCriarTicket() {

        CreateTicketDto dto = new CreateTicketDto(
                "customer-1",
                "service-1",
                "NORMAL",
                "schedule-1"
        );

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");

        Schedule schedule = new Schedule();

        ServiceManagement serviceManagement = new ServiceManagement();
        serviceManagement.setServiceManagementId("service-1");
        serviceManagement.setName("Atendimento");
        serviceManagement.setCode("AT");
        serviceManagement.setLastTicketNumber(0L);

        serviceManagement.setDepartment(
                new br.com.queue.entities.department.Department()
        );

        serviceManagement.getDepartment().setName("Departamento");

        when(unitContext.getCurrentToken(token)).thenReturn(currentToken);
        when(currentToken.unit()).thenReturn(unit);

        when(scheduleRepository.findById("schedule-1"))
                .thenReturn(Optional.of(schedule));

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.of(customer));

        when(serviceManagementRepository.findByServiceManagementId("service-1"))
                .thenReturn(Optional.of(serviceManagement));

        when(ticketRepository.findTicketByScheduleScheduleId("schedule-1"))
                .thenReturn(Optional.empty());

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseTicketDto response = service.createTicket(token, dto);

        assertNotNull(response);
        assertEquals("AT-01", response.code());
        assertEquals("João", response.customerName());
        assertEquals("NORMAL", response.priority());
        assertEquals("WAITING", response.status());

        verify(ticketRepository).save(any(Ticket.class));
        verify(serviceManagementRepository).save(serviceManagement);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/tickets"),
                any(ResponseTicketsForAttendance.class)
        );
    }


    @Test
    void deveAtualizarTicketExistente() {

        CreateTicketDto dto = new CreateTicketDto(
                "customer-1",
                "service-1",
                "NORMAL",
                "schedule-1"
        );

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");

        ServiceManagement serviceManagement = new ServiceManagement();
        serviceManagement.setServiceManagementId("service-1");
        serviceManagement.setName("Atendimento");
        serviceManagement.setCode("AT");

        br.com.queue.entities.department.Department department =
                new br.com.queue.entities.department.Department();
        department.setName("Departamento");
        serviceManagement.setDepartment(department);

        Ticket ticket = new Ticket();
        ticket.setTicketId("ticket-1");
        ticket.setCode("AT-01");
        ticket.setCustomer(customer);
        ticket.setServiceManagement(serviceManagement);
        ticket.setPriority(PriorityLevel.NORMAL);
        ticket.setStatus(TicketStatus.WAITING);
        ticket.setCreatedAt(LocalDateTime.now());

        when(unitContext.getCurrentToken(token)).thenReturn(currentToken);
        when(scheduleRepository.findById("schedule-1"))
                .thenReturn(Optional.of(new Schedule()));

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.of(customer));

        when(serviceManagementRepository.findByServiceManagementId("service-1"))
                .thenReturn(Optional.of(serviceManagement));

        when(ticketRepository.findTicketByScheduleScheduleId("schedule-1"))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        ResponseTicketDto response = service.createTicket(token, dto);

        assertNotNull(response);
        assertEquals("ticket-1", response.ticketId());

        verify(ticketRepository).save(ticket);
        verify(messagingTemplate, never()).convertAndSend(
                eq("/topic/tickets"),
                Optional.ofNullable(any())
        );
    }


    @Test
    void deveChamarTicket() {

        Ticket ticket = criarTicket();

        when(ticketRepository.findById("ticket-1"))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(new User()));

        ResponseTicketDto response = service.callTicket(token,"ticket-1");

        assertNotNull(response);
        assertEquals("CALLED", response.status());
        assertNotNull(ticket.getCalledAt());

        verify(ticketRepository).save(ticket);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/queue-display"),
                any(ResponseTicketDto.class)
        );
    }


    @Test
    void deveFinalizarTicket() {

        Ticket ticket = criarTicket();

        FinishTicketDto dto = new FinishTicketDto(
                "ticket-1",
                "FINISHED"
        );

        when(ticketRepository.findById("ticket-1"))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(new User()));

        ResponseTicketDto response = service.finishTicket(token, dto);

        assertNotNull(response);
        assertEquals(TicketStatus.FINISHED, ticket.getStatus());

        verify(ticketRepository).save(ticket);
    }


    @Test
    void deveCancelarTicket() {

        Ticket ticket = criarTicket();

        when(ticketRepository.findById("ticket-1"))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(new User()));

        ResponseTicketDto response = service.cancelTicket(token,"ticket-1");

        assertNotNull(response);
        assertEquals(TicketStatus.CANCELED, ticket.getStatus());

        verify(ticketRepository).save(ticket);
    }


    @Test
    void deveDeletarTicket() {

        Ticket ticket = criarTicket();

        when(ticketRepository.findById("ticket-1"))
                .thenReturn(Optional.of(ticket));

        ResponseTicketDto response = service.deleteTicket("ticket-1");

        assertNotNull(response);
        assertEquals("ticket-1", response.ticketId());

        verify(ticketRepository).delete(ticket);
    }


    @Test
    void deveBuscarTicketPorId() {

        Ticket ticket = criarTicket();

        when(ticketRepository.findById("ticket-1"))
                .thenReturn(Optional.of(ticket));

        ResponseTicketDto response = service.getTicketById("ticket-1");

        assertNotNull(response);
        assertEquals("ticket-1", response.ticketId());
        assertEquals("AT-01", response.code());
    }


    @Test
    void deveBuscarTodosOsTickets() {

        Ticket ticket = criarTicket();

        Page<Ticket> page =
                new PageImpl<>(List.of(ticket));

        when(ticketRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        Page<ResponseAllTicketsDto> response =
                service.getAllTickets(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("ticket-1", response.getContent().get(0).ticketId());
    }


    @Test
    void deveBuscarTicketsDoAtendente() {

        Ticket ticket = criarTicket();

        Page<Ticket> page =
                new PageImpl<>(List.of(ticket));

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        when(token.getName())
                .thenReturn("user-1");

        when(ticketRepository.getTicketsByAttendant(
                eq("unit-1"),
                eq("user-1"),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(page);

        Page<ResponseTicketsForAttendance> response =
                service.getTicketsByAttendant(token, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("ticket-1", response.getContent().get(0).ticketId());
    }


    @Test
    void deveBuscarHistoricoDoAtendente() {

        Ticket ticket = criarTicket();

        Page<Ticket> page =
                new PageImpl<>(List.of(ticket));

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        when(token.getName())
                .thenReturn("user-1");

        when(ticketRepository.getHistoryTicketsByAttendant(
                eq("unit-1"),
                eq("user-1"),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(page);

        Page<ResponseTicketsForAttendance> response =
                service.getHistoryTicketsByAttendant(token, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }


    @Test
    void deveChamarCliente() {

        Ticket ticket = criarTicket();

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(new User()));

        when(ticketRepository.findById("ticket-1"))
                .thenReturn(Optional.of(ticket));

        ResponseTicketDto response =
                service.callCustomer(token, "ticket-1");

        assertNotNull(response);
        assertEquals("ticket-1", response.ticketId());

        verify(messagingTemplate).convertAndSend(
                eq("/topic/queue-display/call"),
                any(ResponseTicketDto.class)
        );
    }


    @Test
    void deveRetornarExcecaoQuandoTicketNaoExiste() {

        when(ticketRepository.findById("ticket-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> service.getTicketById("ticket-1")
        );
    }


    private Ticket criarTicket() {

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");

        br.com.queue.entities.department.Department department =
                new br.com.queue.entities.department.Department();
        department.setName("Departamento");

        ServiceManagement serviceManagement =
                new ServiceManagement();

        serviceManagement.setServiceManagementId("service-1");
        serviceManagement.setName("Atendimento");
        serviceManagement.setCode("AT");
        serviceManagement.setDepartment(department);

        Ticket ticket = new Ticket();

        ticket.setTicketId("ticket-1");
        ticket.setCode("AT-01");
        ticket.setCallNumber(1);
        ticket.setStatus(TicketStatus.WAITING);
        ticket.setPriority(PriorityLevel.NORMAL);
        ticket.setCustomer(customer);
        ticket.setServiceManagement(serviceManagement);
        ticket.setCreatedAt(LocalDateTime.now());

        return ticket;
    }
}