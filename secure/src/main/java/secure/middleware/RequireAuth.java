package secure.middleware;

import java.net.URLEncoder;

import org.springframework.web.servlet.*;

import jakarta.servlet.http.*;
import secure.model.User;

public class RequireAuth implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var user = (User) request.getAttribute("user");
        if (user != null) {
            request.setAttribute("user", user);
            return true;
        } else {
            if (request.getMethod().equals("GET")) {
                var origin = request.getRequestURI().strip();
                response.sendRedirect(
                        "/auth/login?origin=" + URLEncoder.encode(origin.isBlank() ? "/" : origin, "UTF-8"));
            } else {
                response.sendRedirect("/auth/login");
            }
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}
