package com.games.battleshipbe.repository;

import com.games.battleshipbe.entity.Game;
import com.games.battleshipbe.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findGameByPlayer1AndPlayer2AndWinnerNull(Player player1, Player player2);
}
