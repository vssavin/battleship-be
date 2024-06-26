Тестовое задание – back-end для игры в «морской бой»

Правила игры: на поле 10 на 10 располагаются корабли длиной от 1 до 4 ячеек по вертикали или по горизонтали. Корабли не могут соприкасаться (занимать соседние ячейки).


https://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D1%80%D1%81%D0%BA%D0%BE%D0%B9_%D0%B1%D0%BE%D0%B9_(%D0%B8%D0%B3%D1%80%D0%B0)

Требуется реализовать обработку следующих запросов:
* startGame. Начать новую игру. В ответ возвращает идентификатор игры.
* putShip. При подготовке к игре, разместить корабль, на игровом поле одного из двух игроков. Информация сохраняется в базе данных.
* shoot. В ходе игры выстрел – проверка наличия корабля в определенном поле. В ответ возвращает один из вариантов: ранен, убит, мимо, победа.

В случае ошибки при обработке запроса, в ответ должна приходить информация об ошибке достаточно детальная для ее устранения.

При перезапуске сервера информация не должна теряться.

Стек технологий:
* Java
* Maven
* Какая-либо СУБД (например, PostgreSQL)
* Spring / JavaEE


---

В решении реализована обработка запросов для классического варианта игры с полем 10x10.

При обработке запроса на "game/startGame" производится проверка, что все корабли установлены и после этого производится 
поиск оппонента (у него тоже должны быть установлены все корабли). Если прошлая игра не была завершена, то возвращается
ее идентификатор, иначе создается новый.

Запросы на "/game/startGame" и "game/shoot" возвращают ответ в формате JSON.

Все ответы в формате JSON содержат поле "success", принимающее значение true только в случае успешного выполнения запроса, иначе - значение false;
Так же все ответы содержат поле "message" и "data". Поле "message" содержит сообщение об ошибке, если поле success=false.
Поле "data" содержит данные, специфические для каждого запроса.

Для запроса на "game/startGame" в поле "data" содержится поле "gameId", содержащее значение идентификатора игры.

Для запроса на "game/shoot" в поле "data" содержится поле "status", принимающее одно из значений: ранен, убит, мимо, победа.

Для запроса на "game/startGame" тело запроса должно быть в формате JSON и содержать поле "id" указывающего на игрока инициализирующего начало игры.

Пример тела запроса: ``{"id": 1}``

Для запроса на "game/putShip" тело запроса должно быть в формате JSON и содержать поля, идентифицирующие игрока, который
устанавливает корабль, местоположение корабля (поле ``location``, содержащее координаты
``x`` и ``y``; начальные координаты ``x=0`` и ``y=0`` находятся в нижнем левом углу), количество секций корабля, и положение (горизонтальное или вертикальное). Поле "orientation" может принимать одно из двух значений "HORIZONTAL" или "VERTICAL".
Пример тела запроса:

``
{
"playerId": 1,
"location": {"x": 9, "y": 0},
"length": 1,
"orientation": "HORIZONTAL"
}
``
