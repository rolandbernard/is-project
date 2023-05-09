package insecure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import insecure.middleware.RejectAuth;
import insecure.middleware.RequireAuth;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RejectAuth())
                .addPathPatterns("/login", "/register");
        registry.addInterceptor(new RequireAuth()).excludePathPatterns("/login", "/register");
    }
}
