package com.games.battleshipbe;

import com.games.battleshipbe.entity.Player;
import com.games.battleshipbe.entity.Ship;
import com.games.battleshipbe.entity.ShipSection;
import com.games.battleshipbe.geo.ShipLocation;
import com.games.battleshipbe.geo.ShipOrientation;
import com.games.battleshipbe.repository.PlayerRepository;
import com.games.battleshipbe.repository.ShipRepository;
import com.games.battleshipbe.repository.ShipSectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TestSupportService {

    private final ShipRepository shipRepository;

    private final ShipSectionRepository shipSectionRepository;

    private final PlayerRepository playerRepository;

    public TestSupportService(ShipRepository shipRepository, ShipSectionRepository shipSectionRepository,
                              PlayerRepository playerRepository) {
        this.shipRepository = shipRepository;
        this.shipSectionRepository = shipSectionRepository;
        this.playerRepository = playerRepository;
    }

    List<ShipLocation> getPlayer2AllSectionLocations() {
        Optional<Player> optionalPlayer = playerRepository.findById(2L);
        if (optionalPlayer.isEmpty()) {
            throw new IllegalStateException("Player with id=2 not found!");
        }
        Player player = optionalPlayer.get();
        List<ShipLocation> shipLocations = new ArrayList<>();

        shipRepository.findShipsByPlayer(player).forEach(ship ->
                shipSectionRepository.findShipSectionsByShip(ship).forEach(shipSection -> {
            ShipLocation location = new ShipLocation(shipSection.getLocationX(), shipSection.getLocationY());
            shipLocations.add(location);
        }));

        return shipLocations;
    }

    void installAllShipsForPlayer1() {
        Optional<Player> optionalPlayer = playerRepository.findById(1L);
        if (optionalPlayer.isEmpty()) {
            throw new IllegalStateException("Player with id=1 not found!");
        }
        Player player = optionalPlayer.get();

        saveShip(player, 3, 9, 1, ShipOrientation.HORIZONTAL);
        saveShip(player, 5, 7, 1, ShipOrientation.HORIZONTAL);
        saveShip(player, 1, 3, 1, ShipOrientation.HORIZONTAL);
        saveShip(player, 9, 0, 1, ShipOrientation.HORIZONTAL);

        saveShip(player, 8, 7, 2, ShipOrientation.VERTICAL);
        saveShip(player, 8, 3, 2, ShipOrientation.VERTICAL);
        saveShip(player, 6, 0, 2, ShipOrientation.VERTICAL);

        saveShip(player, 0, 0, 3, ShipOrientation.VERTICAL);
        saveShip(player, 3, 3, 3, ShipOrientation.HORIZONTAL);

        saveShip(player, 1, 5, 4, ShipOrientation.VERTICAL);

    }

    void installAllShipsForPlayer2() {
        Optional<Player> optionalPlayer = playerRepository.findById(2L);
        if (optionalPlayer.isEmpty()) {
            throw new IllegalStateException("Player with id=1 not found!");
        }
        Player player = optionalPlayer.get();

        saveShip(player, 1, 4, 1, ShipOrientation.HORIZONTAL);
        saveShip(player, 2, 1, 1, ShipOrientation.HORIZONTAL);
        saveShip(player, 7, 1, 1, ShipOrientation.HORIZONTAL);
        saveShip(player, 3, 9, 1, ShipOrientation.HORIZONTAL);

        saveShip(player, 1, 7, 2, ShipOrientation.HORIZONTAL);
        saveShip(player, 4, 0, 2, ShipOrientation.VERTICAL);
        saveShip(player, 8, 3, 2, ShipOrientation.VERTICAL);

        saveShip(player, 4, 4, 3, ShipOrientation.VERTICAL);
        saveShip(player, 6, 4, 3, ShipOrientation.VERTICAL);

        saveShip(player, 5, 8, 4, ShipOrientation.VERTICAL);
    }

    ShipLocation getLocationForAnyOneSectionShip(Long playerId) {
        return getLocationForAnyShip(playerId, 1);
    }

    ShipLocation getLocationForAnyTwoSectionShip(Long playerId) {
        return getLocationForAnyShip(playerId, 2);
    }

    ShipLocation getEmptyLocation(Long playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isEmpty()) {
            throw new IllegalStateException("Player with id=" + playerId + " not found!");
        }

        Player player = optionalPlayer.get();

        List<ShipSection> shipSectionList = shipSectionRepository
                .findByPlayerAndLocationXBetweenAndLocationYBetween(player, 0, 9, 0, 9);

        int[][] fieldArray = new int[10][10];

        shipSectionList.forEach(shipSection -> fieldArray[shipSection.getLocationX()][shipSection.getLocationY()] = 1);

        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (fieldArray[x][y] == 0) {
                    return new ShipLocation(x, y);
                }
            }
        }

        throw new IllegalStateException("Не найдено пустое место на поле!");
    }

    ShipLocation getLocationForAnyShip(Long playerId, int shipLength) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isEmpty()) {
            throw new IllegalStateException("Player with id=" + playerId + " not found!");
        }

        Player player = optionalPlayer.get();

        List<Ship> shipList = shipRepository.findShipsByPlayer(player);
        Optional<Ship> optionalShip = shipList.stream().filter(ship -> ship.getLength() == shipLength).findAny();

        if (optionalShip.isEmpty()) {
            throw new IllegalStateException("Ships for player id =" + playerId + " not found!");
        }

        return new ShipLocation(optionalShip.get().getLocationX(), optionalShip.get().getLocationY());
    }

    private Ship saveShip(Player player, int locationX, int locationY, int length, ShipOrientation orientation) {
        Ship ship = new Ship();
        ship.setDestroyed(false);
        ship.setPlayer(player);
        ship.setLength(length);
        ship.setOrientation(orientation);
        ship.setLocationX(locationX);
        ship.setLocationY(locationY);
        ship = shipRepository.save(ship);
        saveShipSections(ship, player);
        player.setShipCount(player.getShipCount() + 1);
        return ship;
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
}
