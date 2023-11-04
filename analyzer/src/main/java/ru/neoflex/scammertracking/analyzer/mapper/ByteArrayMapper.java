package ru.neoflex.scammertracking.analyzer.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ByteArrayMapper {

    private final ObjectMapper objectMapper;

    public byte[] mapObjectToByteArray(Object object) {
        log.info("Input mapObjectToByteArray");

        byte[] paymentResultBytes = null;
        try (
                ByteArrayOutputStream boas = new ByteArrayOutputStream();
                ObjectOutputStream ois = new ObjectOutputStream(boas)
        ) {
            PaymentResponseDto pay = (PaymentResponseDto) object;
            ois.writeObject(pay);
            paymentResultBytes = boas.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Output mapObjectToByteArray");
        return paymentResultBytes;
    }
}
