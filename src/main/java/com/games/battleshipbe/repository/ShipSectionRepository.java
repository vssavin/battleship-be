package com.games.battleshipbe.repository;

import com.games.battleshipbe.entity.Player;
import com.games.battleshipbe.entity.Ship;
import com.games.battleshipbe.entity.ShipSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipSectionRepository extends JpaRepository<ShipSection, Long> {
    Optional<ShipSection> findShipSectionByPlayerAndLocationXAndLocationY(Player player, Integer locationX, Integer locationY);

    List<ShipSection> findShipSectionsByShip(Ship ship);

    List<ShipSection> findByPlayerAndLocationXBetweenAndLocationYBetween(Player player, Integer minX, Integer maxX, Integer minY, Integer maxY);

    void deleteByPlayer(Player player);
}
