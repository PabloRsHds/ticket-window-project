package br.com.queue.service.customer;

import br.com.queue.dtos.customer.allCustomer.ResponseAllCustomersDto;
import br.com.queue.dtos.customer.create.CreateCustomerDto;
import br.com.queue.dtos.customer.getCustomer.ResponseCustomerById;
import br.com.queue.dtos.customer.photo.CustomerPhotoRequestDto;
import br.com.queue.dtos.customer.update.UpdateCustomerDto;
import br.com.queue.dtos.tokenDto.ResponseCurrentToken;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.customer.CustomerNotFoundException;
import br.com.queue.infra.customer.CustomerValidationException;
import br.com.queue.infra.user.UserValidationException;
import br.com.queue.repositories.customer.CustomerRepository;
import br.com.queue.service.unit.UnitContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private JwtAuthenticationToken token;

    @Mock
    private ResponseCurrentToken currentToken;

    @Mock
    private Unit unit;

    @InjectMocks
    private CustomerService service;

    @Test
    void deveCadastrarCliente() {

        CreateCustomerDto dto = new CreateCustomerDto(
                "João",
                "12345678900",
                "123456789",
                "21999999999",
                "joao@email.com"
        );

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");
        customer.setCpf("12345678900");
        customer.setRg("123456789");
        customer.setPhone("21999999999");
        customer.setEmail("joao@email.com");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUnit(unit);

        when(unitContext.getCurrentToken(token)).thenReturn(currentToken);
        when(currentToken.unit()).thenReturn(unit);

        when(customerRepository.existsByCpf(dto.cpf())).thenReturn(false);
        when(customerRepository.existsByRg(dto.rg())).thenReturn(false);
        when(customerRepository.existsByPhone(dto.phone())).thenReturn(false);
        when(customerRepository.existsByEmail(dto.email())).thenReturn(false);

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);

        var response = service.registerCustomer(token, dto);

        assertNotNull(response);
        assertEquals("customer-1", response.customerId());
        assertEquals("João", response.name());
        assertEquals("12345678900", response.cpf());

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void deveLancarExcecaoQuandoCpfJaExiste() {

        CreateCustomerDto dto = new CreateCustomerDto(
                "João",
                "12345678900",
                null,
                null,
                null
        );

        when(customerRepository.existsByCpf(dto.cpf()))
                .thenReturn(true);

        assertThrows(
                CustomerValidationException.class,
                () -> service.registerCustomer(token, dto)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deveAtualizarCliente() {

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");
        customer.setCpf("12345678900");
        customer.setRg("123456789");
        customer.setPhone("21999999999");
        customer.setEmail("joao@email.com");
        customer.setCreatedAt(LocalDateTime.now());

        UpdateCustomerDto dto = new UpdateCustomerDto(
                "customer-1",
                "João Silva",
                null,
                null,
                null,
                null
        );

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.of(customer));

        when(customerRepository.save(customer))
                .thenReturn(customer);

        var response = service.updateCustomer(dto);

        assertNotNull(response);
        assertEquals("João Silva", response.name());

        verify(customerRepository).save(customer);
    }

    @Test
    void deveBuscarClientePorId() {

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");
        customer.setCpf("12345678900");
        customer.setRg("123456789");
        customer.setPhone("21999999999");
        customer.setEmail("joao@email.com");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setTickets(new ArrayList<>());

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.of(customer));

        ResponseCustomerById response =
                service.getCustomerById("customer-1");

        assertNotNull(response);
        assertEquals("customer-1", response.customerId());
        assertEquals("João", response.name());
        assertNull(response.ticketCode());
    }

    @Test
    void deveBuscarClientePorIdComTicket() {

        Ticket ticket = new Ticket();
        ticket.setCode("A001");

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");
        customer.setCpf("12345678900");
        customer.setRg("123456789");
        customer.setPhone("21999999999");
        customer.setEmail("joao@email.com");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setTickets(List.of(ticket));

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.of(customer));

        var response = service.getCustomerById("customer-1");

        assertNotNull(response);
        assertEquals("A001", response.ticketCode());
    }

    @Test
    void deveDeletarCliente() {

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");
        customer.setCpf("12345678900");
        customer.setRg("123456789");
        customer.setPhone("21999999999");
        customer.setEmail("joao@email.com");
        customer.setCreatedAt(LocalDateTime.now());

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.of(customer));

        var response = service.deleteCustomer("customer-1");

        assertNotNull(response);
        assertEquals("customer-1", response.customerId());

        verify(customerRepository).delete(customer);
    }

    @Test void deveBuscarTodosOsClientes() {
        var customer = new ResponseAllCustomersDto(
                "customer-1",
                "João",
                "12345678900",
                "123456",
                "21999999999",
                "joao@email.com"
        );

        Page<ResponseAllCustomersDto> page = new PageImpl<>(List.of(customer));

        when(unitContext.getCurrentToken(token)).thenReturn(currentToken);
        when(currentToken.unit()).thenReturn(unit);
        when(unit.getUnitId()).thenReturn("unit-1");
        when(customerRepository.findAllWithSearch( eq("unit-1"), isNull(), any() )).thenReturn(page);
        Page<ResponseAllCustomersDto> response = service.getAllCustomers( token, 0, 10, null );
        assertNotNull(response); assertEquals(1, response.getTotalElements());
    }

    @Test
    void deveBuscarIdsENomes() {

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");

        when(customerRepository.findAll())
                .thenReturn(List.of(customer));

        var response = service.getCustomerIdsAndNames();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("customer-1", response.get(0).customerId());
        assertEquals("João", response.get(0).name());
    }

    @Test
    void deveAtualizarFoto() throws Exception {

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setName("João");
        customer.setCreatedAt(LocalDateTime.now());

        CustomerPhotoRequestDto dto =
                new CustomerPhotoRequestDto("customer-1");

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "foto.jpg",
                "image/jpeg",
                "imagem".getBytes()
        );

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.of(customer));

        service.updatePhoto(dto, photo);

        assertNotNull(customer.getPhoto());

        verify(customerRepository).save(customer);
    }

    @Test
    void deveLancarExcecaoQuandoFotoNaoForImagem() {

        CustomerPhotoRequestDto dto =
                new CustomerPhotoRequestDto("customer-1");

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "arquivo.txt",
                "text/plain",
                "teste".getBytes()
        );

        assertThrows(
                UserValidationException.class,
                () -> service.updatePhoto(dto, photo)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deveRetornarFotoDoCliente() {

        byte[] photo = "imagem".getBytes();

        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setPhoto(photo);

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.of(customer));

        byte[] response = service.getPhoto("customer-1");

        assertArrayEquals(photo, response);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoExiste() {

        when(customerRepository.findByCustomerId("customer-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomerNotFoundException.class,
                () -> service.getCustomerById("customer-1")
        );
    }
}