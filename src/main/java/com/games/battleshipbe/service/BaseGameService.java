package com.games.battleshipbe.service;

import com.games.battleshipbe.dto.ShipDTO;
import com.games.battleshipbe.dto.ShootDTO;
import org.springframework.stereotype.Service;

@Service
public class BaseGameService implements GameService {
    @Override
    public Long startGame() {
        throw new UnsupportedOperationException("Unimplemented yet!");
    }

    @Override
    public Long putShip(ShipDTO shipDTO) {
        throw new UnsupportedOperationException("Unimplemented yet!");
    }

    @Override
    public String shoot(ShootDTO shootDTO) {
        throw new UnsupportedOperationException("Unimplemented yet!");
    }
}
