package com.games.battleshipbe.service;

import com.games.battleshipbe.dto.ShipDTO;
import com.games.battleshipbe.dto.ShootDTO;

public interface GameService {

    Long startGame(Long playerId);

    Long putShip(ShipDTO shipDTO);

    String shoot(ShootDTO shootDTO);

}
