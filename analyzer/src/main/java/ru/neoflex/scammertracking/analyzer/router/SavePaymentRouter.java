package ru.neoflex.scammertracking.analyzer.router;

import reactor.core.publisher.Flux;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;

public interface SavePaymentRouter {
    void savePayment(Flux<SavePaymentRequestDto> savePaymentDtoFlux);
}
