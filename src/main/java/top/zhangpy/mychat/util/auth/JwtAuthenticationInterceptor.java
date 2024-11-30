package top.zhangpy.mychat.util.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import top.zhangpy.mychat.service.UserService;

import java.lang.reflect.Method;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (token == null) {
            throw new RuntimeException("Token is null");
        }
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }
        else {
            String userId = JWTUtils.getClaimByName(token, "userId").asString();
            if (userId == null) {
                throw new RuntimeException("Token is invalid");
            }
            if (JWTUtils.isTokenExpired(token)) {
                throw new RuntimeException("Token is expired");
            }
//            DecodedJWT verifyToken = JWTUtils.verifyToken(token, userId);
            request.setAttribute("userId", userId);
            return true;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
    }
}
