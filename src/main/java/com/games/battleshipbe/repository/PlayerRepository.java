package com.games.battleshipbe.repository;

import com.games.battleshipbe.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByShipCount(Integer shipCount);
}
