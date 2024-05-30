package com.games.battleshipbe.entity;

import com.games.battleshipbe.geo.ShipOrientation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Ship")
@Getter
@Setter
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(name = "location_x")
    private Integer locationX;

    @Column(name = "location_y")
    private Integer locationY;

    private Integer length;

    @Enumerated(EnumType.STRING)
    private ShipOrientation orientation;

    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    private Boolean destroyed;

}
