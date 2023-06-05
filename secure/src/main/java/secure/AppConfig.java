package secure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import secure.middleware.*;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CheckCsrfToken());
        registry.addInterceptor(new PerformAuth());
        registry.addInterceptor(new RejectAuth())
                .addPathPatterns("/auth/login", "/auth/register");
        registry.addInterceptor(new RequireAuth()).excludePathPatterns("/auth/login", "/auth/register", "/public/**", "/");
    }
}
