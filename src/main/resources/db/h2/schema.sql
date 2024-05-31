DROP TABLE IF EXISTS PUBLIC.SHIP_SECTION;
DROP TABLE IF EXISTS PUBLIC.SHIP;
DROP TABLE IF EXISTS PUBLIC.GAME;
DROP TABLE IF EXISTS PUBLIC.PLAYER;

create table IF NOT EXISTS PUBLIC.PLAYER
(
    ID         BIGINT auto_increment
        primary key,
    NAME       CHARACTER VARYING(255),
    SHIP_COUNT INTEGER
);

create table PUBLIC.GAME
(
    ID          BIGINT auto_increment
        primary key,
    PLAYER_1_ID BIGINT,
    PLAYER_2_ID BIGINT,
    SHOOTER_ID  BIGINT,
    WINNER_ID   BIGINT,
    constraint FKDAOA02UM96NKW564TRBQ0JHXG
        foreign key (WINNER_ID) references PUBLIC.PLAYER,
    constraint FKHB6LO8WIMWBYXSCRUFRNEDMSA
        foreign key (PLAYER_1_ID) references PUBLIC.PLAYER,
    constraint FKI0O6UXBQXP8DSMWYHSRCYSLW8
        foreign key (PLAYER_2_ID) references PUBLIC.PLAYER,
    constraint FKOMNPYJ08EV1H9R5QFIYT6LR4E
        foreign key (SHOOTER_ID) references PUBLIC.PLAYER
);

create table IF NOT EXISTS PUBLIC.SHIP
(
    ID          BIGINT auto_increment
        primary key,
    DESTROYED   INTEGER,
    LENGTH      INTEGER,
    LOCATION_X  INTEGER,
    LOCATION_Y  INTEGER,
    ORIENTATION ENUM ('HORIZONTAL', 'VERTICAL'),
    PLAYER_ID   BIGINT,
    constraint FK8ASGV7B220O680UJJDILEMHON
        foreign key (PLAYER_ID) references PUBLIC.PLAYER,
    check ("DESTROYED" IN (0, 1))
);

create table IF NOT EXISTS PUBLIC.SHIP_SECTION
(
    ID             BIGINT auto_increment
        primary key,
    DESTROYED      INTEGER,
    LOCATION_X     INTEGER,
    LOCATION_Y     INTEGER,
    SECTION_NUMBER INTEGER,
    PLAYER_ID      BIGINT,
    SHIP_ID        BIGINT,
    constraint FKAI1J05FMPI8STBGH6VONC8KY9
        foreign key (PLAYER_ID) references PUBLIC.PLAYER,
    constraint FKOIB9GQ9NBL7TFRGLI1EFGKXG1
        foreign key (SHIP_ID) references PUBLIC.SHIP,
    check ("DESTROYED" IN (0, 1))
);

