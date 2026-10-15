package com.catmiih.delivery.delivery.tracking.infrastructure.timeEstimation;

import com.catmiih.delivery.delivery.tracking.domain.model.Coordinates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class NominatimGeocodingService implements GeocodingService {

    private final RestClient restClient;
    private final String baseUrl;
    private final ViaCepService viaCepService;

    public NominatimGeocodingService(
            RestClient.Builder builder,
            @Value("${geocoding.nominatim-url}") String baseUrl, ViaCepService viaCepService
    ) {
        this.restClient = builder
                .defaultHeader(
                        "User-Agent",
                        "DeliveryCourseProject/1.0 (contact: emilyleme.dev@gmail.com)"
                )
                .build();

        this.baseUrl = baseUrl;
        this.viaCepService = viaCepService;
    }

    @Override
    public Coordinates geocode(
            String zipCode,
            String street,
            String number
    ) {
        ViaCepService.ViaCepResponse address =
                viaCepService.findAddress(zipCode);

        String fullStreet = number + " " + street;

        var uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/search")
                .queryParam("street", fullStreet)
                .queryParam("postalcode", zipCode)
                .queryParam("city", address.localidade())
                .queryParam("state", address.uf())
                .queryParam("country", "Brazil")
                .queryParam("format", "jsonv2")
                .queryParam("limit", 1)
                .build()
                .encode()
                .toUri();

        try {
            List<NominatimResult> results = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<
                            List<NominatimResult>>() {});

            if (results == null || results.isEmpty()) {
                throw new IllegalArgumentException(
                        "Endereço não encontrado: "
                                + zipCode + ", " + street + ", " + number
                );
            }

            NominatimResult result = results.get(0);

            return new Coordinates(
                    Double.parseDouble(result.lat()),
                    Double.parseDouble(result.lon())
            );

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "Falha ao consultar o serviço de geocodificação", e
            );
        }
    }

    private record NominatimResult(
            String lat,
            String lon
    ) {}
}
