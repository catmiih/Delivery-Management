package com.catmiih.delivery.delivery.tracking.infrastructure;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ViaCepService {

    private final RestClient restClient;

    public ViaCepService(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://viacep.com.br")
                .build();
    }

    public ViaCepResponse findAddress(String zipCode) {
        String normalizedZipCode = zipCode.replaceAll("\\D", "");

        if (!normalizedZipCode.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "CEP deve conter 8 dígitos: " + zipCode
            );
        }

        try {
            ViaCepResponse response = restClient.get()
                    .uri("/ws/{zipCode}/json/", normalizedZipCode)
                    .retrieve()
                    .body(ViaCepResponse.class);

            if (response == null) {
                throw new IllegalArgumentException(
                        "CEP não encontrado: " + zipCode
                );
            }

            return response;

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "Falha ao consultar o ViaCEP", e
            );
        }
    }

    public record ViaCepResponse(
            String cep,
            String logradouro,
            String bairro,
            String localidade,
            String uf
    ) {}
}
