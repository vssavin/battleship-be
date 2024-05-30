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

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping(value = "/start")
    public ResponseEntity<ResponseObject> start(@RequestBody PlayerDTO playerDTO) {
        ResponseObject responseObject = new ResponseObject();
        responseObject.setData(new StartDataResponse(gameService.startGame(playerDTO.id())));
        return ResponseEntity.ok(responseObject);
    }

    @PostMapping("/putShip")
    public ResponseEntity<ResponseObject> putShip(@RequestBody ShipDTO shipDTO) {
        try {
            gameService.putShip(shipDTO);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            ResponseObject responseObject = new ResponseObject();
            responseObject.setMessage(e.getMessage());
            responseObject.setSuccess(false);
            return ResponseEntity.badRequest().body(responseObject);
        }
    }

    @PutMapping("/shoot")
    public ResponseEntity<Object> shoot(@RequestBody ShootDTO shootDTO) {
        ResponseObject responseObject = new ResponseObject();
        try {
            ShootDataResponse shootDataResponse = new ShootDataResponse(gameService.shoot(shootDTO));
            responseObject.setData(shootDataResponse);
            return ResponseEntity.ok(responseObject);
        } catch (Exception e) {
            responseObject.setMessage(e.getMessage());
            responseObject.setSuccess(false);
            return ResponseEntity.badRequest().body(responseObject);
        }
    }
}
