package edufit_com_lms.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFound extends AppException {
    public  ResourceNotFound(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
