INSERT INTO Player(id, name, ship_count)
select 1, 'Player1', 0
where not exists (select id from Player where id = 1);

INSERT INTO Player(id, name, ship_count)
select 2, 'Player2', 0
where not exists (select id from Player where id = 2);