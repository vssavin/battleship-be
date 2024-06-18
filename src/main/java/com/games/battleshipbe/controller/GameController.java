package com.games.battleshipbe.controller;

import com.games.battleshipbe.controller.response.ResponseObject;
import com.games.battleshipbe.controller.response.ShootDataResponse;
import com.games.battleshipbe.controller.response.StartDataResponse;
import com.games.battleshipbe.dto.PlayerDTO;
import com.games.battleshipbe.dto.ShipDTO;
import com.games.battleshipbe.dto.ShootDTO;
import com.games.battleshipbe.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping(value = "/startGame")
    public ResponseEntity<ResponseObject> start(@RequestBody PlayerDTO playerDTO) {
        try {
            return getOkResponseEntity(new StartDataResponse(gameService.startGame(playerDTO.id())));
        } catch (Exception e) {
            return getErrorResponseEntity(e);
        }
    }

    @PostMapping("/putShip")
    public ResponseEntity<ResponseObject> putShip(@RequestBody ShipDTO shipDTO) {
        try {
            gameService.putShip(shipDTO);
            return getOkResponseEntity(null);
        } catch (Exception e) {
            return getErrorResponseEntity(e);
        }
    }

    @PutMapping("/shoot")
    public ResponseEntity<ResponseObject> shoot(@RequestBody ShootDTO shootDTO) {
        try {
            return getOkResponseEntity(new ShootDataResponse(gameService.shoot(shootDTO)));
        } catch (Exception e) {
            return getErrorResponseEntity(e);
        }
    }

    private ResponseEntity<ResponseObject> getOkResponseEntity(Object data) {
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(data);
        return ResponseEntity.ok(responseObject);
    }

    private ResponseEntity<ResponseObject> getErrorResponseEntity(Throwable throwable) {
        ResponseObject responseObject = new ResponseObject();
        if (throwable != null) {
            responseObject.setMessage(throwable.getMessage() + "\n" +
                    Arrays.asList(throwable.getStackTrace()).toString().replace(",", "\n"));
        }
        responseObject.setSuccess(false);
        return ResponseEntity.badRequest().body(responseObject);
    }
}
