package edufit_com_lms.common.exception;

import org.springframework.http.HttpStatus;

public class ForbbidenException extends AppException
{
    public ForbbidenException(String message)
    {
        super(HttpStatus.FORBIDDEN, message);
    }
}
