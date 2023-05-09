package insecure.middleware;

import java.net.URLEncoder;

import org.springframework.web.servlet.*;

import insecure.Database;
import insecure.model.User;
import jakarta.servlet.http.*;

public class RequireAuth implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (cookie.getName().equals("user-id")) {
                    try (var database = new Database()) {
                        var user = User.getUser(database, Integer.parseInt(cookie.getValue()));
                        if (user != null) {
                            request.setAttribute("user", user);
                            return true;
                        } else {
                            cookie.setMaxAge(0);
                            response.addCookie(cookie);
                        }
                    }
                }
            }
        }
        response.sendRedirect("/login?origin=" + URLEncoder.encode(request.getRequestURI(), "UTF-8"));
        return false;
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
