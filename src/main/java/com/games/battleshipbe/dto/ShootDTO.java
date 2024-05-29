package com.games.battleshipbe.dto;

import com.games.battleshipbe.geo.ShipLocation;

public record ShootDTO(Long gameId, Long playerId, ShipLocation location) {
}
