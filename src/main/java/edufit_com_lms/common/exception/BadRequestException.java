package edufit_com_lms.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AppException{
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
    public BadRequestException(HttpStatus status, String message) {
        super(status, message);
    }
}
