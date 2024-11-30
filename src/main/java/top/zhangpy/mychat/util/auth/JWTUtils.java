package top.zhangpy.mychat.util.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;
import top.zhangpy.mychat.entity.vo.Result;

import java.util.Calendar;
import java.util.List;

@Component
public class JWTUtils {

    private static final String SECRET = "mychat";

    public static String getJWTToken(String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 7);
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("userId", userId);
        return builder.withIssuedAt(Calendar.getInstance().getTime())
                .withExpiresAt(calendar.getTime())
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static DecodedJWT verifyToken(String token, String userId) {
        try {
            return JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
        } catch (JWTDecodeException e) {
            throw new RuntimeException("Token is invalid" + e.getMessage());
        }
    }

    public static boolean isTokenExpired(String token) {
        DecodedJWT jwt = JWT.decode(token);
        if (jwt.getExpiresAt() == null) {
            throw new RuntimeException("Token does not contain an expiration date");
        }
        return jwt.getExpiresAt().before(Calendar.getInstance().getTime());
    }


    public static String getAudience(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            List<String> audienceList = jwt.getAudience();
            if (audienceList == null || audienceList.isEmpty()) {
                throw new RuntimeException("Audience is missing in the token");
            }
            return audienceList.get(0);
        } catch (JWTDecodeException e) {
            throw new RuntimeException("Failed to decode token: " + e.getMessage());
        }
    }


    public static Claim getClaimByName(String token, String name) {
        Claim claim = JWT.decode(token).getClaim(name);
        if (claim.isNull()) {
            throw new RuntimeException("Claim '" + name + "' is missing in the token");
        }
        return claim;
    }

    public static Result checkToken(String userId, String token) {
        if (userId == null) {
            return Result.fail(408, "userId is null", null);
        }
        if (token == null) {
            return Result.fail(409, "token is null", null);
        }

        boolean isTokenExpired = JWTUtils.isTokenExpired(token);
        if (isTokenExpired) {
            return Result.fail(410, "token is expired", null);
        }

        String userIdFromToken;
        try {
            userIdFromToken = JWTUtils.getClaimByName(token, "userId").asString();
        } catch (RuntimeException e) {
            return Result.fail(412, "Failed to decode token: " + e.getMessage(), null);
        }

        if (!userId.equals(userIdFromToken)) {
            return Result.fail(411, "userId does not match with token audience", null);
        }
        return null;
    }
}
