package br.com.queue.service.attendance;

import br.com.queue.dtos.attendance.allAttendances.ResponseAllAttendances;
import br.com.queue.dtos.attendance.finish.ResponseAttendanceDto;
import br.com.queue.dtos.attendance.finish.ResponseFinishAttendanceDto;
import br.com.queue.dtos.attendance.start.FinishAttendanceDto;
import br.com.queue.dtos.attendance.start.StartAttendanceDto;
import br.com.queue.dtos.tokenDto.ResponseCurrentToken;
import br.com.queue.entities.attendance.Attendance;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;
import br.com.queue.enums.PriorityLevel;
import br.com.queue.enums.Role;
import br.com.queue.enums.TicketStatus;
import br.com.queue.infra.attendances.AttendanceInvalidStateException;
import br.com.queue.infra.attendances.AttendanceNotFoundException;
import br.com.queue.infra.ticket.TicketNotFoundException;
import br.com.queue.repositories.attendance.AttendanceRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private JwtAuthenticationToken token;

    @Mock
    private ResponseCurrentToken currentToken;

    @Mock
    private Unit unit;

    @Mock
    private User user;

    @InjectMocks
    private AttendanceService attendanceService;


    // =========================================================
    // START ATTENDANCE
    // =========================================================

    @Test
    void deveIniciarAtendimento() {

        StartAttendanceDto dto = new StartAttendanceDto("ticket-1");

        Ticket ticket = mock(Ticket.class);

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        when(ticketRepository.findByTicketIdAndUnitId(
                "ticket-1",
                "unit-1"
        )).thenReturn(Optional.of(ticket));

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        when(user.getRole())
                .thenReturn(Role.ATTENDANT);

        when(ticket.getStatus())
                .thenReturn(TicketStatus.CALLED);

        when(ticket.getTicketId())
                .thenReturn("ticket-1");

        when(ticket.getCode())
                .thenReturn("001");

        ResponseAttendanceDto response =
                attendanceService.startAttendance(token, dto);

        assertNotNull(response);
        assertEquals("ticket-1", response.ticketId());
        assertEquals("001", response.ticketCode());

        verify(ticket).setStatus(TicketStatus.IN_PROGRESS);
        verify(ticketRepository).save(ticket);
        verify(attendanceRepository).save(any(Attendance.class));
    }


    @Test
    void deveIniciarAtendimentoComAdmin() {

        StartAttendanceDto dto = new StartAttendanceDto("ticket-1");

        Ticket ticket = mock(Ticket.class);

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        when(ticketRepository.findByTicketIdAndUnitId(
                "ticket-1",
                "unit-1"
        )).thenReturn(Optional.of(ticket));

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        when(user.getRole())
                .thenReturn(Role.ADMIN);

        when(ticket.getStatus())
                .thenReturn(TicketStatus.CALLED);

        when(ticket.getTicketId())
                .thenReturn("ticket-1");

        when(ticket.getCode())
                .thenReturn("001");

        ResponseAttendanceDto response =
                attendanceService.startAttendance(token, dto);

        assertNotNull(response);
        assertEquals("ticket-1", response.ticketId());

        verify(ticket).setStatus(TicketStatus.IN_PROGRESS);
        verify(ticketRepository).save(ticket);
        verify(attendanceRepository).save(any(Attendance.class));
    }


    @Test
    void deveLancarExcecaoQuandoUsuarioNaoTemPermissao() {

        StartAttendanceDto dto = new StartAttendanceDto("ticket-1");

        Ticket ticket = mock(Ticket.class);

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        when(ticketRepository.findByTicketIdAndUnitId(
                "ticket-1",
                "unit-1"
        )).thenReturn(Optional.of(ticket));

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        when(user.getRole())
                .thenReturn(Role.RECEPTION);

        assertThrows(
                AttendanceInvalidStateException.class,
                () -> attendanceService.startAttendance(token, dto)
        );

        verify(ticketRepository)
                .findByTicketIdAndUnitId("ticket-1", "unit-1");

        verify(userRepository)
                .findById("user-1");

        verify(ticket, never()).setStatus(any());

        verify(ticketRepository, never()).save(any());

        verify(attendanceRepository, never()).save(any());
    }


    @Test
    void deveLancarExcecaoQuandoTicketNaoEstaWaiting() {

        StartAttendanceDto dto = new StartAttendanceDto("ticket-1");

        Ticket ticket = mock(Ticket.class);

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        when(ticketRepository.findByTicketIdAndUnitId(
                "ticket-1",
                "unit-1"
        )).thenReturn(Optional.of(ticket));

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        when(user.getRole())
                .thenReturn(Role.ATTENDANT);

        when(ticket.getStatus())
                .thenReturn(TicketStatus.IN_PROGRESS);

        assertThrows(
                AttendanceInvalidStateException.class,
                () -> attendanceService.startAttendance(token, dto)
        );

        verify(ticket, never()).setStatus(any());
        verify(ticketRepository, never()).save(any());
        verify(attendanceRepository, never()).save(any());
    }


    @Test
    void deveLancarExcecaoQuandoTicketNaoExisteAoIniciar() {

        StartAttendanceDto dto = new StartAttendanceDto("ticket-1");

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        when(ticketRepository.findByTicketIdAndUnitId(
                "ticket-1",
                "unit-1"
        )).thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> attendanceService.startAttendance(token, dto)
        );

        verify(ticketRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }


    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExisteAoIniciar() {

        StartAttendanceDto dto = new StartAttendanceDto("ticket-1");

        Ticket ticket = mock(Ticket.class);

        when(unitContext.getCurrentToken(token))
                .thenReturn(currentToken);

        when(currentToken.unit())
                .thenReturn(unit);

        when(unit.getUnitId())
                .thenReturn("unit-1");

        when(ticketRepository.findByTicketIdAndUnitId(
                "ticket-1",
                "unit-1"
        )).thenReturn(Optional.of(ticket));

        when(token.getName())
                .thenReturn("user-1");

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                br.com.queue.infra.user.UserNotFoundException.class,
                () -> attendanceService.startAttendance(token, dto)
        );

        verify(ticket, never()).setStatus(any());
        verify(ticketRepository, never()).save(any());
        verify(attendanceRepository, never()).save(any());
    }


    // =========================================================
    // FINISH ATTENDANCE
    // =========================================================

    @Test
    void deveFinalizarAtendimento() {

        FinishAttendanceDto dto =
                new FinishAttendanceDto(
                        "ticket-1",
                        "Cliente atendido"
                );

        Ticket ticket = mock(Ticket.class);
        Attendance attendance = mock(Attendance.class);
        Customer customer = mock(Customer.class);
        ServiceManagement serviceManagement = mock(ServiceManagement.class);

        LocalDateTime startedAt =
                LocalDateTime.now().minusMinutes(5);

        LocalDateTime createdAt =
                LocalDateTime.now().minusMinutes(10);

        when(ticketRepository.findByTicketId("ticket-1"))
                .thenReturn(Optional.of(ticket));

        when(attendanceRepository.findByTicket(ticket))
                .thenReturn(Optional.of(attendance));

        when(attendance.getTicket())
                .thenReturn(ticket);

        when(ticket.getCode())
                .thenReturn("001");

        when(ticket.getStatus())
                .thenReturn(TicketStatus.WAITING);

        when(ticket.getPriority())
                .thenReturn(PriorityLevel.values()[0]);

        when(ticket.getCustomer())
                .thenReturn(customer);

        when(customer.getName())
                .thenReturn("Cliente");

        when(ticket.getServiceManagement())
                .thenReturn(serviceManagement);

        when(serviceManagement.getName())
                .thenReturn("Atendimento");

        when(ticket.getCreatedAt())
                .thenReturn(createdAt);

        when(attendance.getStartedAt())
                .thenReturn(startedAt);

        when(attendance.getResolution())
                .thenReturn("Cliente atendido");

        ResponseFinishAttendanceDto response =
                attendanceService.finishAttendance(dto);

        assertNotNull(response);
        assertEquals(
                "Cliente atendido",
                response.resolution()
        );

        verify(attendance)
                .setResolution("Cliente atendido");

        verify(attendance)
                .setFinishedAt(any(LocalDateTime.class));

        verify(ticket)
                .setStatus(TicketStatus.FINISHED);

        verify(attendanceRepository)
                .save(attendance);

        verify(ticketRepository)
                .save(ticket);

        verify(messagingTemplate)
                .convertAndSend(
                        eq("/topic/tickets/history"),
                        Optional.ofNullable(any())
                );
    }


    @Test
    void deveLancarExcecaoQuandoTicketNaoExisteAoFinalizar() {

        FinishAttendanceDto dto =
                new FinishAttendanceDto(
                        "ticket-1",
                        "Cliente atendido"
                );

        when(ticketRepository.findByTicketId("ticket-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> attendanceService.finishAttendance(dto)
        );

        verify(attendanceRepository, never())
                .findByTicket(any());

        verify(ticketRepository, never())
                .save(any());

        verify(attendanceRepository, never())
                .save(any());
    }


    @Test
    void deveLancarExcecaoQuandoAttendanceNaoExisteAoFinalizar() {

        FinishAttendanceDto dto =
                new FinishAttendanceDto(
                        "ticket-1",
                        "Cliente atendido"
                );

        Ticket ticket = mock(Ticket.class);

        when(ticketRepository.findByTicketId("ticket-1"))
                .thenReturn(Optional.of(ticket));

        when(attendanceRepository.findByTicket(ticket))
                .thenReturn(Optional.empty());

        assertThrows(
                AttendanceNotFoundException.class,
                () -> attendanceService.finishAttendance(dto)
        );

        verify(attendanceRepository, never())
                .save(any());

        verify(ticketRepository, never())
                .save(any());

        verify(messagingTemplate, never())
                .convertAndSend(anyString(), Optional.ofNullable(any()));
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void deveBuscarTodosOsAtendimentos() {

        Attendance attendance = mock(Attendance.class);
        Ticket ticket = mock(Ticket.class);

        when(attendance.getTicket())
                .thenReturn(ticket);

        when(ticket.getTicketId())
                .thenReturn("ticket-1");

        when(ticket.getCode())
                .thenReturn("001");

        when(attendance.getResolution())
                .thenReturn("Cliente atendido");

        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime finishedAt = LocalDateTime.now();

        when(attendance.getStartedAt())
                .thenReturn(startedAt);

        when(attendance.getFinishedAt())
                .thenReturn(finishedAt);

        Page<Attendance> page =
                new PageImpl<>(List.of(attendance));

        when(attendanceRepository.findAll(any(PageRequest.class)))
                .thenReturn(page);

        Page<ResponseAllAttendances> response =
                attendanceService.getAllAttendances(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());

        ResponseAllAttendances result =
                response.getContent().get(0);

        assertEquals("ticket-1", result.ticketId());
        assertEquals("001", result.ticketCode());
        assertEquals("Cliente atendido", result.resolution());
        assertEquals(startedAt, result.startedAt());
        assertEquals(finishedAt, result.finishedAt());
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void deveDeletarAtendimento() {

        Attendance attendance = mock(Attendance.class);

        when(attendanceRepository.findByAttendanceId("attendance-1"))
                .thenReturn(Optional.of(attendance));

        attendanceService.deleteAttendance("attendance-1");

        verify(attendanceRepository)
                .delete(attendance);
    }


    @Test
    void deveLancarExcecaoQuandoAtendimentoNaoExiste() {

        when(attendanceRepository.findByAttendanceId("attendance-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                AttendanceNotFoundException.class,
                () -> attendanceService.deleteAttendance("attendance-1")
        );

        verify(attendanceRepository, never())
                .delete(any());
    }
}