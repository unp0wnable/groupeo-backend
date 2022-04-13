package me.unp0wnable.groupeo.rest.dtos.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ErrorsDto {
    /** Error global en la ejecución */
    private String globalError;
    
    /** Lista con los campos que produjeron el error */
    @JsonInclude(Include.NON_NULL)
    private List<FieldErrorDto> fieldErrors;
    
    public ErrorsDto(String globalError) {
        this.globalError = globalError;
    }
    
    public ErrorsDto(List<FieldErrorDto> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
