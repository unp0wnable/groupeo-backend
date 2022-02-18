package me.unp0wnable.groupeo.rest.http.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class JwtData {
    private UUID userID;
    private String nickName;
    private String role;
}
