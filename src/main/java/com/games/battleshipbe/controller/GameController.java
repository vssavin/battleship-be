package com.games.battleshipbe.controller;

import com.games.battleshipbe.controller.response.ResponseObject;
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
    public ResponseEntity<Long> start() {
        return ResponseEntity.ok(gameService.startGame());
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
        try {
            gameService.shoot(shootDTO);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            ResponseObject responseObject = new ResponseObject();
            responseObject.setMessage(e.getMessage());
            responseObject.setSuccess(false);
            return ResponseEntity.badRequest().body(responseObject);
        }
    }
}
