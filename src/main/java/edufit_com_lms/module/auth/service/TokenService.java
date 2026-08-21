package edufit_com_lms.module.auth.service;

public interface TokenService {
    void saveRefreshToken(String email,String refreshToken);
    void revokeRefreshToken(String email);
    boolean isValidRefreshToken(String email,String tokenFromClient);
}
