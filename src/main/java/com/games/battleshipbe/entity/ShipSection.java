package com.games.battleshipbe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ship_section")
@Getter
@Setter
public class ShipSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ship_id", referencedColumnName = "id")
    private Ship ship;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(name = "location_x")
    private Integer locationX;

    @Column(name = "location_y")
    private Integer locationY;

    @Column(name = "section_number")
    private Integer sectionNumber;

    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    private Boolean destroyed;

}
