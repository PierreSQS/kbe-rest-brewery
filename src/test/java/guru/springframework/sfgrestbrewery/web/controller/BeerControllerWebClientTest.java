package guru.springframework.sfgrestbrewery.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class BeerControllerWebClientTest {

    // Mock the service
    BeerService beerService = mock(BeerService.class);

    // Set up the WebTestClient Mock
    private final WebTestClient webTestClient =
            MockMvcWebTestClient.bindToController(new BeerController(beerService)).build();

    // Initialize Jackson Object Mapper
    ObjectMapper objectMapper = new ObjectMapper();

    BeerDto validBeer;

    @BeforeEach
    public void setUp() {
        validBeer = BeerDto.builder().id(UUID.randomUUID())
                .beerName("Beer1")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_2_UPC)
                .build();
    }

    @Test
    void getBeer() {
        given(beerService.getById(any(UUID.class), anyBoolean())).willReturn(validBeer);

        webTestClient.get().uri("/api/v1/beer/" + validBeer.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isEqualTo(validBeer.getId().toString())
                .jsonPath("$.beerName", is("Beer1"));
    }

    @Test
    void handlePost() throws Exception {
        //given
        BeerDto beerDto = validBeer;
        beerDto.setId(null);
        BeerDto savedDto = BeerDto.builder().id(UUID.randomUUID()).beerName("New Beer").build();
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);

        given(beerService.saveNewBeer(any())).willReturn(savedDto);

        webTestClient.post().uri("/api/v1/beer/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(beerDtoJson)
                .exchange().expectStatus()
                .isCreated();
    }

    @Test
    void handleUpdate() throws Exception {
        //given
        BeerDto beerDto = validBeer;
        beerDto.setId(null);
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);

        //when
        webTestClient.put().uri("/api/v1/beer/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(beerDtoJson).exchange()
                .expectStatus().isNoContent();

        then(beerService).should().updateBeer(any(), any());

    }
}