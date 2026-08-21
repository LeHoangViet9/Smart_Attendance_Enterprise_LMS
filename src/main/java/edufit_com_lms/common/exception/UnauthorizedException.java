package edufit_com_lms.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException{
    public UnauthorizedException(String message){
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
