package top.zhangpy.mychat.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.zhangpy.mychat.util.auth.JwtAuthenticationInterceptor;

@Configuration
public class JwtInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/contact/**",
                        "/chat/**",
                        "/group/**",
                        "/user/**",
                        "/profile/**"
                )
                .excludePathPatterns("/login",
                        "/register",
                        "/swagger-ui.html",
                        "/swagger-resources/**"

                );
    }

    @Bean
    public JwtAuthenticationInterceptor authenticationInterceptor() {
        return new JwtAuthenticationInterceptor();
    }
}
