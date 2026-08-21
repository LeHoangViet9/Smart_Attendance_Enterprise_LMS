package edufit_com_lms.module.auth.service.impl;

import edufit_com_lms.module.auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final StringRedisTemplate stringRedisTemplate;
    private static final long REFRESH_TOKEN_EXPIRATION = 7;

    @Override
    public void saveRefreshToken(String email, String refreshToken) {
        String key = "RT" + email;
        stringRedisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRATION, TimeUnit.DAYS);
    }

    @Override
    public void revokeRefreshToken(String email) {
        String key = "RT" + email;
        stringRedisTemplate.delete(key);
    }

    @Override
    public boolean isValidRefreshToken(String email, String tokenFromClient) {
        String key = "RT" + email;
        String tokenFromRedis = stringRedisTemplate.opsForValue().get(key);
        if (tokenFromRedis != null && tokenFromRedis.equals(tokenFromClient)) {
            return true;
        }
        return false;
    }
}
