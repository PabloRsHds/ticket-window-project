package br.com.queue.service.unit;

import br.com.queue.dtos.unit.CreateUnitDto;
import br.com.queue.dtos.unit.ResponseUnitDto;
import br.com.queue.dtos.unit.UpdateUnitDto;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.unit.UnitIsPresentException;
import br.com.queue.infra.unit.UnitNotFoundException;
import br.com.queue.repositories.unit.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private UnitService unitService;

    private Unit unit;

    @BeforeEach
    void setUp() {

        unit = new Unit();

        unit.setUnitId("unit-1");
        unit.setName("Unidade Centro");
        unit.setAddress("Rua Principal, 100");
        unit.setActive(true);

        unit.setCreatedAt(
                LocalDateTime.of(2026, 9, 10, 10, 0)
        );

        unit.setUpdatedAt(null);
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void shouldCreateUnitSuccessfully() {

        CreateUnitDto dto = new CreateUnitDto(
                "Nova Unidade",
                "Rua Nova, 123"
        );

        when(unitRepository.existsByName(dto.name()))
                .thenReturn(false);

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation -> {

                    Unit saved = invocation.getArgument(0);

                    saved.setUnitId("unit-2");

                    return saved;
                });

        ResponseUnitDto response =
                unitService.createUnit(dto);

        assertNotNull(response);

        assertEquals(
                "unit-2",
                response.unitId()
        );

        assertEquals(
                "Nova Unidade",
                response.name()
        );

        assertEquals(
                "Rua Nova, 123",
                response.address()
        );

        assertTrue(response.active());

        assertNotNull(response.createdAt());

        assertNull(response.updatedAt());

        verify(unitRepository)
                .existsByName("Nova Unidade");

        verify(unitRepository)
                .save(any(Unit.class));
    }

    @Test
    void shouldCreateUnitWithoutAddress() {

        CreateUnitDto dto = new CreateUnitDto(
                "Nova Unidade",
                null
        );

        when(unitRepository.existsByName(dto.name()))
                .thenReturn(false);

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation -> {

                    Unit saved = invocation.getArgument(0);

                    saved.setUnitId("unit-2");

                    return saved;
                });

        ResponseUnitDto response =
                unitService.createUnit(dto);

        assertNotNull(response);

        assertEquals(
                "Nova Unidade",
                response.name()
        );

        assertNull(response.address());

        assertTrue(response.active());
        assertNotNull(response.createdAt());
    }

    @Test
    void shouldNormalizeAddressWhenCreatingUnit() {

        CreateUnitDto dto = new CreateUnitDto(
                "Nova Unidade",
                "   Rua Principal, 123   "
        );

        when(unitRepository.existsByName(dto.name()))
                .thenReturn(false);

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation -> {

                    Unit saved = invocation.getArgument(0);
                    saved.setUnitId("unit-2");

                    return saved;
                });

        ResponseUnitDto response =
                unitService.createUnit(dto);

        assertEquals(
                "Rua Principal, 123",
                response.address()
        );
    }

    @Test
    void shouldConvertBlankAddressToNullWhenCreatingUnit() {

        CreateUnitDto dto = new CreateUnitDto(
                "Nova Unidade",
                "     "
        );

        when(unitRepository.existsByName(dto.name()))
                .thenReturn(false);

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation -> {

                    Unit saved = invocation.getArgument(0);
                    saved.setUnitId("unit-2");

                    return saved;
                });

        ResponseUnitDto response =
                unitService.createUnit(dto);

        assertNull(response.address());
    }

    @Test
    void shouldThrowExceptionWhenUnitNameAlreadyExists() {

        CreateUnitDto dto = new CreateUnitDto(
                "Unidade Centro",
                "Rua Principal, 100"
        );

        when(unitRepository.existsByName(dto.name()))
                .thenReturn(true);

        UnitIsPresentException exception =
                assertThrows(
                        UnitIsPresentException.class,
                        () -> unitService.createUnit(dto)
                );

        assertEquals(
                "Uma unidade com o nome 'Unidade Centro' já existe.",
                exception.getMessage()
        );

        verify(unitRepository)
                .existsByName("Unidade Centro");

        verify(unitRepository, never())
                .save(any(Unit.class));
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void shouldUpdateUnitSuccessfully() {

        UpdateUnitDto dto = new UpdateUnitDto(
                "unit-1",
                "Unidade Atualizada",
                "Rua Atualizada, 200",
                false
        );

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        when(unitRepository.existsByName("Unidade Atualizada"))
                .thenReturn(false);

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ResponseUnitDto response =
                unitService.updateUnit(dto);

        assertNotNull(response);

        assertEquals(
                "unit-1",
                response.unitId()
        );

        assertEquals(
                "Unidade Atualizada",
                response.name()
        );

        assertEquals(
                "Rua Atualizada, 200",
                response.address()
        );

        assertFalse(response.active());

        assertNotNull(response.updatedAt());

        verify(unitRepository)
                .findById("unit-1");

        verify(unitRepository)
                .existsByName("Unidade Atualizada");

        verify(unitRepository)
                .save(unit);
    }

    @Test
    void shouldUpdateOnlyName() {

        UpdateUnitDto dto = new UpdateUnitDto(
                "unit-1",
                "Novo Nome",
                null,
                null
        );

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        when(unitRepository.existsByName("Novo Nome"))
                .thenReturn(false);

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        unitService.updateUnit(dto);

        assertEquals(
                "Novo Nome",
                unit.getName()
        );

        assertEquals(
                "Rua Principal, 100",
                unit.getAddress()
        );

        assertTrue(unit.getActive());

        assertNotNull(unit.getUpdatedAt());
    }

    @Test
    void shouldUpdateOnlyAddress() {

        UpdateUnitDto dto = new UpdateUnitDto(
                "unit-1",
                null,
                "Nova Rua, 500",
                null
        );

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        unitService.updateUnit(dto);

        assertEquals(
                "Unidade Centro",
                unit.getName()
        );

        assertEquals(
                "Nova Rua, 500",
                unit.getAddress()
        );

        assertTrue(unit.getActive());

        assertNotNull(unit.getUpdatedAt());
    }

    @Test
    void shouldUpdateOnlyActiveStatus() {

        UpdateUnitDto dto = new UpdateUnitDto(
                "unit-1",
                null,
                null,
                false
        );

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        unitService.updateUnit(dto);

        assertEquals(
                "Unidade Centro",
                unit.getName()
        );

        assertEquals(
                "Rua Principal, 100",
                unit.getAddress()
        );

        assertFalse(unit.getActive());

        assertNotNull(unit.getUpdatedAt());
    }

    @Test
    void shouldNotUpdateBlankName() {

        UpdateUnitDto dto = new UpdateUnitDto(
                "unit-1",
                "   ",
                null,
                null
        );

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        unitService.updateUnit(dto);

        assertEquals(
                "Unidade Centro",
                unit.getName()
        );

        assertNull(unit.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingUnit() {

        UpdateUnitDto dto = new UpdateUnitDto(
                "unit-999",
                "Nova Unidade",
                null,
                null
        );

        when(unitRepository.findById("unit-999"))
                .thenReturn(Optional.empty());

        UnitNotFoundException exception =
                assertThrows(
                        UnitNotFoundException.class,
                        () -> unitService.updateUnit(dto)
                );

        assertEquals(
                "Unidade não encontrada com ID: unit-999",
                exception.getMessage()
        );

        verify(unitRepository, never())
                .save(any(Unit.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToExistingName() {

        UpdateUnitDto dto = new UpdateUnitDto(
                "unit-1",
                "Outra Unidade",
                null,
                null
        );

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        when(unitRepository.existsByName("Outra Unidade"))
                .thenReturn(true);

        UnitIsPresentException exception =
                assertThrows(
                        UnitIsPresentException.class,
                        () -> unitService.updateUnit(dto)
                );

        assertEquals(
                "Uma unidade com o nome 'Outra Unidade' já existe.",
                exception.getMessage()
        );

        verify(unitRepository, never())
                .save(any(Unit.class));
    }

    @Test
    void shouldAllowKeepingTheSameName() {

        UpdateUnitDto dto = new UpdateUnitDto(
                "unit-1",
                "Unidade Centro",
                "Novo Endereço",
                null
        );

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ResponseUnitDto response =
                unitService.updateUnit(dto);

        assertEquals(
                "Unidade Centro",
                response.name()
        );

        assertEquals(
                "Novo Endereço",
                response.address()
        );

        verify(unitRepository, never())
                .existsByName(anyString());
    }

    @Test
    void shouldNotChangeUpdatedAtWhenNothingChanges() {

        UpdateUnitDto dto = new UpdateUnitDto(
                "unit-1",
                null,
                null,
                null
        );

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        when(unitRepository.save(any(Unit.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ResponseUnitDto response =
                unitService.updateUnit(dto);

        assertEquals(
                "Unidade Centro",
                response.name()
        );

        assertNull(response.updatedAt());

        verify(unitRepository)
                .save(unit);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void shouldDeleteUnitSuccessfully() {

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        ResponseUnitDto response =
                unitService.deleteUnit("unit-1");

        assertNotNull(response);

        assertEquals(
                "unit-1",
                response.unitId()
        );

        assertEquals(
                "Unidade Centro",
                response.name()
        );

        assertEquals(
                "Rua Principal, 100",
                response.address()
        );

        assertTrue(response.active());

        verify(unitRepository)
                .findById("unit-1");

        verify(unitRepository)
                .delete(unit);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingUnit() {

        when(unitRepository.findById("unit-999"))
                .thenReturn(Optional.empty());

        UnitNotFoundException exception =
                assertThrows(
                        UnitNotFoundException.class,
                        () -> unitService.deleteUnit("unit-999")
                );

        assertEquals(
                "Unidade não encontrada com ID: unit-999",
                exception.getMessage()
        );

        verify(unitRepository, never())
                .delete(any(Unit.class));
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    void shouldGetUnitByIdSuccessfully() {

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        ResponseUnitDto response =
                unitService.getUnitById("unit-1");

        assertNotNull(response);

        assertEquals(
                "unit-1",
                response.unitId()
        );

        assertEquals(
                "Unidade Centro",
                response.name()
        );

        assertEquals(
                "Rua Principal, 100",
                response.address()
        );

        assertTrue(response.active());

        assertEquals(
                "10/09/2026 10:00",
                response.createdAt()
        );

        assertNull(response.updatedAt());

        verify(unitRepository)
                .findById("unit-1");
    }

    @Test
    void shouldGetUnitByIdWithUpdatedAt() {

        unit.setUpdatedAt(
                LocalDateTime.of(
                        2026,
                        9,
                        10,
                        12,
                        30
                )
        );

        when(unitRepository.findById("unit-1"))
                .thenReturn(Optional.of(unit));

        ResponseUnitDto response =
                unitService.getUnitById("unit-1");

        assertEquals(
                "10/09/2026 12:30",
                response.updatedAt()
        );
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistingUnit() {

        when(unitRepository.findById("unit-999"))
                .thenReturn(Optional.empty());

        UnitNotFoundException exception =
                assertThrows(
                        UnitNotFoundException.class,
                        () -> unitService.getUnitById("unit-999")
                );

        assertEquals(
                "Unidade não encontrada com ID: unit-999",
                exception.getMessage()
        );
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void shouldGetAllUnitsWithSearch() {

        ResponseUnitDto dto = new ResponseUnitDto(
                "unit-1",
                "Unidade Centro",
                "Rua Principal, 100",
                true,
                "10/09/2026 10:00",
                null
        );

        Page<ResponseUnitDto> page =
                new PageImpl<>(List.of(dto));

        when(unitRepository.findAllWithSearch(
                eq("Centro"),
                eq(PageRequest.of(0, 10))
        )).thenReturn(page);

        Page<ResponseUnitDto> result =
                unitService.getAllUnits(
                        0,
                        10,
                        " Centro "
                );

        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "Unidade Centro",
                result.getContent().get(0).name()
        );

        verify(unitRepository)
                .findAllWithSearch(
                        eq("Centro"),
                        eq(PageRequest.of(0, 10))
                );
    }

    @Test
    void shouldUseNullWhenSearchIsNull() {

        Page<ResponseUnitDto> page =
                new PageImpl<>(
                        Collections.emptyList()
                );

        when(unitRepository.findAllWithSearch(
                isNull(),
                eq(PageRequest.of(0, 10))
        )).thenReturn(page);

        Page<ResponseUnitDto> result =
                unitService.getAllUnits(
                        0,
                        10,
                        null
                );

        assertNotNull(result);

        assertTrue(result.isEmpty());

        verify(unitRepository)
                .findAllWithSearch(
                        isNull(),
                        eq(PageRequest.of(0, 10))
                );
    }

    @Test
    void shouldUseNullWhenSearchIsBlank() {

        Page<ResponseUnitDto> page =
                new PageImpl<>(
                        Collections.emptyList()
                );

        when(unitRepository.findAllWithSearch(
                isNull(),
                eq(PageRequest.of(1, 20))
        )).thenReturn(page);

        Page<ResponseUnitDto> result =
                unitService.getAllUnits(
                        1,
                        20,
                        "     "
                );

        assertNotNull(result);

        verify(unitRepository)
                .findAllWithSearch(
                        isNull(),
                        eq(PageRequest.of(1, 20))
                );
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    @Test
    void shouldConvertUnitToResponse() {

        unit.setUpdatedAt(
                LocalDateTime.of(
                        2026,
                        9,
                        10,
                        15,
                        45
                )
        );

        ResponseUnitDto response =
                invokeToResponse(unit);

        assertEquals(
                "unit-1",
                response.unitId()
        );

        assertEquals(
                "Unidade Centro",
                response.name()
        );

        assertEquals(
                "Rua Principal, 100",
                response.address()
        );

        assertTrue(response.active());

        assertEquals(
                "10/09/2026 10:00",
                response.createdAt()
        );

        assertEquals(
                "10/09/2026 15:45",
                response.updatedAt()
        );
    }

    @Test
    void shouldReturnNullCreatedAtWhenCreatedAtIsNull() {

        unit.setCreatedAt(null);

        ResponseUnitDto response =
                invokeToResponse(unit);

        assertNull(response.createdAt());
        assertNull(response.updatedAt());
    }

    // =========================================================
    // HELPER
    // =========================================================

    /*
     * toResponse é private no serviço.
     *
     * Os testes públicos já cobrem esse método indiretamente,
     * mas este helper permite testar diretamente a conversão.
     */
    private ResponseUnitDto invokeToResponse(Unit unit) {

        try {

            var method =
                    UnitService.class.getDeclaredMethod(
                            "toResponse",
                            Unit.class
                    );

            method.setAccessible(true);

            return (ResponseUnitDto)
                    method.invoke(unitService, unit);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}
