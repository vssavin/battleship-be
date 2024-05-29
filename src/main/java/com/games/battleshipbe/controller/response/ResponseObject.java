package com.games.battleshipbe.controller.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ResponseObject {
    private boolean success = true;
    private String message;
    private Object data;
}
