package com.games.battleshipbe.service;

import com.games.battleshipbe.dto.ShipDTO;
import com.games.battleshipbe.dto.ShootDTO;
import com.games.battleshipbe.entity.Game;
import com.games.battleshipbe.entity.Player;
import com.games.battleshipbe.entity.Ship;
import com.games.battleshipbe.entity.ShipSection;
import com.games.battleshipbe.repository.GameRepository;
import com.games.battleshipbe.repository.PlayerRepository;
import com.games.battleshipbe.repository.ShipRepository;
import com.games.battleshipbe.repository.ShipSectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BaseGameService implements GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final ShipRepository shipRepository;
    private final ShipSectionRepository shipSectionRepository;

    public BaseGameService(GameRepository gameRepository, PlayerRepository playerRepository,
                           ShipRepository shipRepository, ShipSectionRepository shipSectionRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.shipRepository = shipRepository;
        this.shipSectionRepository = shipSectionRepository;
    }

    @Override
    public Long startGame(Long playerId) {
        Player player = checkPlayerValid(playerId);
        checkAllShipsInstalled(player);
        Player opponent = findOpponent(player);
        Optional<Game> optionalGame = gameRepository.findGameByPlayer1AndPlayer2AndWinnerNull(player, opponent);
        if (optionalGame.isEmpty()) {
            Game game = new Game();
            game.setPlayer1(player);
            game.setPlayer2(opponent);
            game = gameRepository.save(game);
            return game.getId();
        } else {
            return optionalGame.get().getId();
        }
    }

    private Player findOpponent(Player player) {
        List<Player> playerList = playerRepository.findByShipCount(10);
        Optional<Player> opponentOptional = playerList.stream().filter(p -> !p.equals(player)).findAny();
        if (opponentOptional.isPresent()) {
            return opponentOptional.get();
        } else {
            throw new IllegalStateException("Не удалось найти противника!");
        }
    }

    @Override
    public Long putShip(ShipDTO shipDTO) {
        throw new UnsupportedOperationException("Unimplemented yet!");
    }

    @Override
    public String shoot(ShootDTO shootDTO) {
        Player shooter = checkPlayerValid(shootDTO.playerId());

        Optional<Game> optionalGame = gameRepository.findById(shootDTO.gameId());

        checkGameValid(optionalGame);
        Game game = optionalGame.get();

        Player targetPlayer = getTargetPlayer(shootDTO, game);

        Optional<ShipSection> optionalSection = shipSectionRepository.findShipSectionByPlayerAndLocationXAndLocationY(
                targetPlayer, shootDTO.location().x(), shootDTO.location().y());

        if (optionalSection.isEmpty()) {
            return "мимо";
        } else {
            ShipSection shipSection = optionalSection.get();
            shipSection.setDestroyed(true);
            if (isShipDestroyed(shipSection)) {
                if (!shipSection.getShip().getDestroyed()) {
                    shipSection.getShip().setDestroyed(true);
                }

                if (isAllShipsDestroyed(targetPlayer)) {
                    game.setWinner(shooter);
                    shipSectionRepository.deleteByPlayer(shooter);
                    shipSectionRepository.deleteByPlayer(targetPlayer);
                    shipRepository.deleteByPlayer(shooter);
                    shipRepository.deleteByPlayer(targetPlayer);
                    return "победа";
                }

                return "убит";
            }

            return "ранен";
        }

    }

    private void checkAllShipsInstalled(Player player) {
        List<Ship> shipList = shipRepository.findShipsByPlayer(player);
        boolean installed = shipList.stream().filter(ship -> ship.getLength() == 1).count() == 4;
        installed = installed && shipList.stream().filter(ship -> ship.getLength() == 2).count() == 3;
        installed = installed && shipList.stream().filter(ship -> ship.getLength() == 3).count() == 2;
        installed = installed && shipList.stream().filter(ship -> ship.getLength() == 4).count() == 1;

        if (!installed) {
            throw new IllegalStateException("Не установлены все корабли!");
        }
    }

    private Player checkPlayerValid(Long playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isEmpty()) {
            throw new IllegalArgumentException("Неправильный идентификатор игрока! Игрок не найден в базе данных!");
        }

        return optionalPlayer.get();
    }

    private boolean isAllShipsDestroyed(Player targetPlayer) {
        long aliveShipsCount = shipRepository.findShipsByPlayer(targetPlayer).stream().filter(ship -> !ship.getDestroyed()).count();
        return aliveShipsCount == 0;
    }

    private boolean isShipDestroyed(ShipSection shipSection) {
        Ship ship = shipSection.getShip();
        List<ShipSection> sectionList = shipSectionRepository.findShipSectionsByShip(ship);
        long aliveSectionCount = sectionList.stream().filter(section -> !section.getDestroyed()).count();
        return aliveSectionCount == 0;
    }


    private void checkGameValid(Optional<Game> optionalGame) {
        if (optionalGame.isEmpty()) {
            throw new IllegalArgumentException("Неправильный идентификатор игры! Указанная игра отсутствует!");
        }

        Game game = optionalGame.get();
        if (game.getWinner() != null) {
            throw new IllegalArgumentException("Передан неправильный идентификатор игры! Указанная игра окончена!");
        }
    }

    private Player getTargetPlayer(ShootDTO shootDTO, Game game) {
        Player player1 = game.getPlayer1();
        Player player2 = game.getPlayer2();

        if (player1.getId().equals(shootDTO.playerId())) {
            return player2;
        } else {
            return player1;
        }
    }
}
