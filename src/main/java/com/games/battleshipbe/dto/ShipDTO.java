package com.games.battleshipbe.dto;

import com.games.battleshipbe.geo.ShipLocation;
import com.games.battleshipbe.geo.ShipOrientation;

public record ShipDTO(Long playerId, ShipLocation location, int size, ShipOrientation orientation) {
}
