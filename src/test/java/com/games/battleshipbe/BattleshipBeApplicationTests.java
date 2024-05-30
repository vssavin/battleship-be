package com.games.battleshipbe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(scripts = {"classpath:test_schema.sql", "classpath:test_data.sql"})
class BattleshipBeApplicationTests {

    @Test
    void contextLoads() {

    }

}
