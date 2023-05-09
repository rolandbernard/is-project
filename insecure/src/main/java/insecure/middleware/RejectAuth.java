package insecure.middleware;

import org.springframework.web.servlet.*;

import insecure.model.User;
import jakarta.servlet.http.*;

public class RejectAuth implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var user = (User) request.getAttribute("user");
        if (user != null) {
            response.sendRedirect("/");
            return false;
        } else {
            return true;
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
