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

import java.util.*;

@Service
@Transactional
public class BaseGameService implements GameService {

    private static final Map<Integer, Integer> ALLOWED_SHIP_COUNT_MAP = new HashMap<>();

    static {
        ALLOWED_SHIP_COUNT_MAP.put(1, 4);
        ALLOWED_SHIP_COUNT_MAP.put(2, 3);
        ALLOWED_SHIP_COUNT_MAP.put(3, 2);
        ALLOWED_SHIP_COUNT_MAP.put(4, 1);
    }

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

        checkShipParamsValid(shipDTO);

        Player player = checkPlayerValid(shipDTO.playerId());

        checkShipPlacement(shipDTO, player);

        return saveShip(player, shipDTO);

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

    private void checkShipPlacement(ShipDTO shipDTO, Player player) {
        String wrongPlaceMessage = "Недопустимое место установки корабля!";

        switch (shipDTO.orientation()) {
            case HORIZONTAL -> {
                if ((shipDTO.location().x() + shipDTO.length() - 1) > 9) {
                    throw new IllegalArgumentException(wrongPlaceMessage);
                }
            }

            case VERTICAL -> {
                if ((shipDTO.location().y() + shipDTO.length() - 1) > 9) {
                    throw new IllegalArgumentException(wrongPlaceMessage);
                }
            }
        }

        List<Ship> shipList = shipRepository.findShipsByPlayer(player);

        int maxShipCount = ALLOWED_SHIP_COUNT_MAP.get(shipDTO.length());
        long shipCount = shipList.stream().filter(ship -> ship.getLength().equals(shipDTO.length())).count();
        if (shipCount >= maxShipCount) {
            throw new IllegalArgumentException(String.format("Все корабли размером = %d уже установлены!", shipDTO.length()));
        }

        long countShipsAtPlace = shipList.stream()
                .filter(ship ->
                        ship.getLocationX() == shipDTO.location().x() && ship.getLocationY() == shipDTO.location().y())
                .count();
        if (countShipsAtPlace > 0) {
            throw new IllegalArgumentException("Указанное место занято другим кораблем!");
        }

        int minX = shipDTO.location().x() - 1;
        int minY = shipDTO.location().y() - 1;
        int maxX = 0;
        int maxY = 0;

        switch (shipDTO.orientation()) {
            case HORIZONTAL -> {
                maxX = minX + shipDTO.length() + 1;
                maxY = minY + 2;
            }

            case VERTICAL -> {
                maxY = minY + shipDTO.length() + 1;
                maxX = minX + 2;
            }
        }

        int countShipSections =
                shipSectionRepository.findByPlayerAndLocationXBetweenAndLocationYBetween(player, minX, maxX, minY, maxY)
                        .size();

        if (countShipSections != 0) {
            throw new IllegalArgumentException(wrongPlaceMessage);
        }

    }

    private Long saveShip(Player player, ShipDTO shipDTO) {
        Ship ship = new Ship();
        ship.setPlayer(player);
        ship.setLocationX(shipDTO.location().x());
        ship.setLocationY(shipDTO.location().y());
        ship.setLength(shipDTO.length());
        ship.setOrientation(shipDTO.orientation());
        ship.setDestroyed(false);
        ship = shipRepository.save(ship);
        int playerShipCount = player.getShipCount();
        playerShipCount++;
        player.setShipCount(playerShipCount);

        saveShipSections(ship, player);
        return ship.getId();
    }

    private void saveShipSections(Ship ship, Player player) {
        List<ShipSection> sectionList = new ArrayList<>();

        for (int i = 0; i < ship.getLength(); i++) {
            ShipSection section = new ShipSection();
            section.setShip(ship);
            section.setPlayer(player);
            section.setLocationX(ship.getLocationX());
            section.setLocationY(ship.getLocationY());
            section.setSectionNumber(i + 1);
            section.setDestroyed(false);

            switch (ship.getOrientation()) {
                case HORIZONTAL -> section.setLocationX(section.getLocationX() + i);
                case VERTICAL -> section.setLocationY(section.getLocationY() + i);
            }

            sectionList.add(section);
        }

        shipSectionRepository.saveAll(sectionList);
    }

    private void checkShipParamsValid(ShipDTO shipDTO) {
        if (shipDTO.location().x() < 0 || shipDTO.location().x() > 9) {
            throw new IllegalArgumentException("Неправильные координаты корабля! Допустимые значение: [0-9]");
        }

        if (shipDTO.location().y() < 0 || shipDTO.location().y() > 9) {
            throw new IllegalArgumentException("Неправильные координаты корабля! Допустимые значение: [0-9]");
        }

        if (shipDTO.length() < 1 || shipDTO.length() > 4) {
            throw new IllegalArgumentException("Неправильный размер корабля! Допустимые значение: [1-4]");
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