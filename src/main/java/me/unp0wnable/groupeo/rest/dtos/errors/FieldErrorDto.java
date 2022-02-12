package me.unp0wnable.groupeo.rest.dtos.errors;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
/** Datos un error en un campo o atributo de una instancia */
public class FieldErrorDto {
    /** Nombre del campo que provoca el error */
    private String fieldName;
    /** Mensaje detallado con el problema */
    private String message;
}
