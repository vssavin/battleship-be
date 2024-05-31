package com.games.battleshipbe;

import com.games.battleshipbe.controller.response.ResponseObject;
import com.games.battleshipbe.dto.PlayerDTO;
import com.games.battleshipbe.dto.ShipDTO;
import com.games.battleshipbe.dto.ShootDTO;
import com.games.battleshipbe.geo.ShipLocation;
import com.games.battleshipbe.geo.ShipOrientation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:test_schema.sql", "classpath:test_data.sql"})
class BattleshipBeApplicationTests {
    private static final String START_ENDPOINT = "/game/start";
    private static final String PUT_SHIP_ENDPOINT = "/game/putShip";
    private static final String SHOOT_ENDPOINT = "/game/shoot";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestSupportService testSupportService;

    @Test
    void shouldStartGameResponseNotSuccess_WhenAllShipsNotInstalled() {
        ResponseObject response = restTemplate.postForObject("http://localhost:" + port + START_ENDPOINT,
                new PlayerDTO(1L, ""), ResponseObject.class);
        assertFalse(response.isSuccess(),
                "Игра не должна начаться пока не будут установлены все корабли!\nСообщение бэкенда: " + response.getMessage());
    }

    @Test
    void shouldStartGameResponseSuccess_WhenAllShipsInstalled() {
        testSupportService.installAllShipsForPlayer1();
        testSupportService.installAllShipsForPlayer2();

        ResponseObject response = restTemplate.postForObject("http://localhost:" + port + START_ENDPOINT,
                new PlayerDTO(1L, ""), ResponseObject.class);
        assertTrue(response.isSuccess(),
                "Игра должна начаться когда будут установлены все корабли!\nСообщение бэкенда: " + response.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideWrongCoordinatesRange")
    void shouldPutShipResponseNotSuccess_WhenWrongCoordinates(Integer x, Integer y) {
        ShipLocation shipLocation = new ShipLocation(x, y);
        ResponseObject response = restTemplate.postForObject("http://localhost:" + port + PUT_SHIP_ENDPOINT,
                new ShipDTO(1L, shipLocation, 1, ShipOrientation.VERTICAL), ResponseObject.class);
        assertFalse(response.isSuccess(),
                "Не должно быть возможности установить корабль за пределами поля!\nСообщение бэкенда: " + response.getMessage());
    }

    @Test
    void shouldPutShipResponseNotSuccess_WhenNearbyPlaceSpecified() {
        ShipLocation shipLocation = new ShipLocation(1, 1);
        ResponseObject response = restTemplate.postForObject("http://localhost:" + port + PUT_SHIP_ENDPOINT,
                new ShipDTO(1L, shipLocation, 1, ShipOrientation.VERTICAL), ResponseObject.class);
        assertTrue(response.isSuccess(),
                "Корабль должен быть установлен в указанном месте: " + shipLocation + "\nСообщение бэкенда: " + response.getMessage());

        List<ShipLocation> nearbyLocations = new ArrayList<>();
        nearbyLocations.add(new ShipLocation(0,0));
        nearbyLocations.add(new ShipLocation(0,1));
        nearbyLocations.add(new ShipLocation(0,2));
        nearbyLocations.add(new ShipLocation(1,2));
        nearbyLocations.add(new ShipLocation(2,2));
        nearbyLocations.add(new ShipLocation(2,1));
        nearbyLocations.add(new ShipLocation(2,0));
        nearbyLocations.add(new ShipLocation(1,0));

        nearbyLocations.forEach(location -> {
            ResponseObject resp = restTemplate.postForObject("http://localhost:" + port + PUT_SHIP_ENDPOINT,
                    new ShipDTO(1L, location, 1, ShipOrientation.VERTICAL), ResponseObject.class);
            assertFalse(resp.isSuccess(),
                    "Не должно быть возможности установить корабль рядом с другим кораблем!\nСообщение бэкенда: " + response.getMessage());
        });
    }

    @Test
    void shouldShootResponsePlayerWin_WhenAllShipsDestroyed() {
        Long player1Id = 1L;
        Long player2Id = 2L;

        testSupportService.installAllShipsForPlayer1();
        testSupportService.installAllShipsForPlayer2();

        Integer gameId = getGameId(player1Id);

        List<ShipLocation> shipLocations = testSupportService.getPlayer2AllSectionLocations();

        ResponseObject response = null;
        for(ShipLocation shipLocation: shipLocations) {
            ShootDTO shootDTO = new ShootDTO(gameId.longValue(), player1Id, shipLocation);
            response = shoot(player1Id, shootDTO);

            if (response != null && response.getData() == null) {
                shoot(player2Id, shootDTO);
                response = shoot(player1Id, shootDTO);
            }
        }

        String status = ((Map<String,String>)response.getData()).get("status");

        assertEquals("победа", status.toLowerCase());

    }

    @Test
    void shouldShootResponseDestroyed_WhenShipDestroyed() {
        Long player1Id = 1L;
        Long player2Id = 2L;

        testSupportService.installAllShipsForPlayer1();
        testSupportService.installAllShipsForPlayer2();

        Integer gameId = getGameId(player1Id);

        ShipLocation shipLocation = testSupportService.getLocationForAnyOneSectionShip(player2Id);

        ShootDTO shootDTO = new ShootDTO(gameId.longValue(), player1Id, shipLocation);
        ResponseObject response = shoot(player1Id, shootDTO);

        if (response != null && response.getData() == null) {
            shoot(player2Id, shootDTO);
            response = shoot(player1Id, shootDTO);
        }

        String status = ((Map<String,String>)response.getData()).get("status");

        assertEquals("убит", status.toLowerCase());

    }

    @Test
    void shouldShootResponseHit_WhenHitShipAndNotDestroyed() {
        Long player1Id = 1L;
        Long player2Id = 2L;

        testSupportService.installAllShipsForPlayer1();
        testSupportService.installAllShipsForPlayer2();

        Integer gameId = getGameId(player1Id);

        ShipLocation shipLocation = testSupportService.getLocationForAnyTwoSectionShip(player2Id);

        ShootDTO shootDTO = new ShootDTO(gameId.longValue(), player1Id, shipLocation);
        ResponseObject response = shoot(player1Id, shootDTO);

        if (response != null && response.getData() == null) {
            shoot(player2Id, shootDTO);
            response = shoot(player1Id, shootDTO);
        }

        String status = ((Map<String,String>)response.getData()).get("status");

        assertEquals("ранен", status.toLowerCase());

    }

    @Test
    void shouldShootResponseMissed() {
        Long player1Id = 1L;
        Long player2Id = 2L;

        testSupportService.installAllShipsForPlayer1();
        testSupportService.installAllShipsForPlayer2();

        Integer gameId = getGameId(player1Id);

        ShipLocation shipLocation = testSupportService.getEmptyLocation(player2Id);

        ShootDTO shootDTO = new ShootDTO(gameId.longValue(), player1Id, shipLocation);
        ResponseObject response = shoot(player1Id, shootDTO);

        if (response != null && response.getData() == null) {
            shoot(player2Id, shootDTO);
            response = shoot(player1Id, shootDTO);
        }

        String status = ((Map<String,String>)response.getData()).get("status");

        assertEquals("мимо", status.toLowerCase());

    }

    private ResponseObject shoot(Long shooterId, ShootDTO shootDTO) {
        HttpEntity<ShootDTO> requestEntity = new HttpEntity<>(new ShootDTO(shootDTO.gameId(), shooterId, shootDTO.location()));
        ResponseEntity<ResponseObject> exchangeResult = restTemplate.exchange(
                "http://localhost:" + port + SHOOT_ENDPOINT, HttpMethod.PUT,
                requestEntity, new ParameterizedTypeReference<>() {});
        return exchangeResult.getBody();
    }

    private Integer getGameId(Long playerId) {
        ResponseObject response = restTemplate.postForObject("http://localhost:" + port + START_ENDPOINT,
                new PlayerDTO(playerId, ""), ResponseObject.class);
        return ((Map<String,Integer>)response.getData()).get("gameId");
    }

    private static Stream<Arguments> provideWrongCoordinatesRange() {
        return Stream.of(
                Arguments.of(0, -1),
                Arguments.of(-1, 0),
                Arguments.of(-1, -1),
                Arguments.of(0, 10),
                Arguments.of(10, 0),
                Arguments.of(10, 10)
        );
    }

}
