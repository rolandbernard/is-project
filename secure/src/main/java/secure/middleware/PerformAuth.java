package secure.middleware;

import org.springframework.web.servlet.*;

import jakarta.servlet.http.*;
import secure.Database;
import secure.Random;
import secure.model.User;

public class PerformAuth implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var session = request.getSession(true);
        if (session.getAttribute("csrf-token") == null) {
            var token = Random.instance().nextBytesBase64(32);
            session.setAttribute("csrf-token", token);
        } else if (session.getAttribute("user-id") != null) {
            try (var database = new Database()) {
                var user = User.getUser(database, (String) session.getAttribute("user-id"));
                if (user != null) {
                    request.setAttribute("user", user);
                    return true;
                } else {
                    session.removeAttribute("user-id");
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        modelAndView.getModel().put("user", request.getAttribute("user"));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}
