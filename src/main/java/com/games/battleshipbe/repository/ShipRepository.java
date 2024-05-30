package com.games.battleshipbe.repository;

import com.games.battleshipbe.entity.Player;
import com.games.battleshipbe.entity.Ship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipRepository extends JpaRepository<Ship, Long> {

    List<Ship> findShipsByPlayer(Player player);
    void deleteByPlayer(Player player);
}
