package jco.jcosaprfclink.exception;

import jco.jcosaprfclink.type.ErrorCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessExceptionHandler extends RuntimeException{
    private ErrorCode errorCode;
    private String errorMassage;

    public BusinessExceptionHandler(ErrorCode errorCode){
        this.errorCode = errorCode;
        this.errorMassage = errorCode.getDescription();
    }
}