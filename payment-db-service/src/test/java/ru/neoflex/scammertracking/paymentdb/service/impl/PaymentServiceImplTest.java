package ru.neoflex.scammertracking.paymentdb.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.neoflex.scammertracking.paymentdb.config.PaymentConfiguration;
import ru.neoflex.scammertracking.paymentdb.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentAlreadyExistsException;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentNotFoundException;
import ru.neoflex.scammertracking.paymentdb.map.PaymentRowMapper;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@SpringJUnitConfig({PaymentConfiguration.class})
class PaymentServiceImplTest {

    private static final String PAYER_CARD_NUMBER = "123456";
    private static final String FAKE_CARD_NUMBER = "fake";
    private static final String RECEIVER_CARD_NUMBER = "654321";
    private static final long ID = 1;
    private static final Long DUPLICATE_ID = 1L;
    public static final Integer TEST_COORDINATE = 20;
    public static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private LogService logService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    public void getLastPaymentTest() {
        Coordinates coordinates = new Coordinates(TEST_COORDINATE, TEST_COORDINATE);
        final String query = "select * from payments where payer_card_number=? and date=(select max(date) from payments where payer_card_number=?) LIMIT 1";
        PaymentEntity paymentEntity = new PaymentEntity(ID, PAYER_CARD_NUMBER, RECEIVER_CARD_NUMBER, TEST_COORDINATE, TEST_COORDINATE, NOW);
        PaymentResponseDto paymentResponseDto = new PaymentResponseDto(1, PAYER_CARD_NUMBER, RECEIVER_CARD_NUMBER, coordinates, NOW);

        when(jdbcTemplate.queryForObject(Mockito.any(query.getClass()), Mockito.any(PaymentRowMapper.class), Mockito.any(PAYER_CARD_NUMBER.getClass()), Mockito.any(PAYER_CARD_NUMBER.getClass())))
                .thenReturn(paymentEntity);
        when(modelMapper.map(Mockito.any(), Mockito.eq(PaymentResponseDto.class)))
                .thenReturn(paymentResponseDto);

        PaymentResponseDto paymentResponse = paymentService.getLastPayment(PAYER_CARD_NUMBER);
        assertEquals(PAYER_CARD_NUMBER, paymentResponse.getPayerCardNumber());
    }

    @Test
    public void getLastPaymentErrorTest() {
        final String query = "select * from payments where payer_card_number=? and date=(select max(date) from payments where payer_card_number=?) LIMIT 1";
        PaymentEntity paymentEntityFake = new PaymentEntity(ID, FAKE_CARD_NUMBER, RECEIVER_CARD_NUMBER, TEST_COORDINATE, TEST_COORDINATE, NOW);

        when(jdbcTemplate.queryForObject(Mockito.any(query.getClass()), Mockito.any(PaymentRowMapper.class), Mockito.any(FAKE_CARD_NUMBER.getClass()), Mockito.any(FAKE_CARD_NUMBER.getClass())))
                .thenThrow(EmptyResultDataAccessException.class);

        assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.getLastPayment(FAKE_CARD_NUMBER);
        });
    }

    @Test
    public void savePaymentTest() {
        Coordinates coordinates = new Coordinates(TEST_COORDINATE, TEST_COORDINATE);
        SavePaymentRequestDto paymentRequest = new SavePaymentRequestDto(ID, PAYER_CARD_NUMBER, RECEIVER_CARD_NUMBER, coordinates, new Date());

        paymentService.savePayment(paymentRequest);
    }

    @Test
    public void savePaymentDuplicateTest() {
        Coordinates coordinates = new Coordinates(TEST_COORDINATE, TEST_COORDINATE);
        SavePaymentRequestDto paymentRequest = new SavePaymentRequestDto(DUPLICATE_ID, PAYER_CARD_NUMBER, RECEIVER_CARD_NUMBER, coordinates, new Date());

        final String query = "INSERT INTO payments VALUES(?,?,?,?,?,?)";

        when(jdbcTemplate.update(
                Mockito.any(query.getClass()),
                Mockito.any(DUPLICATE_ID.getClass()),
                Mockito.any(PAYER_CARD_NUMBER.getClass()),
                Mockito.any(RECEIVER_CARD_NUMBER.getClass()),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(new Date().getClass())
        )).thenThrow(DuplicateKeyException.class);

        assertThrows(PaymentAlreadyExistsException.class, () -> {
            paymentService.savePayment(paymentRequest);
        });
    }
}