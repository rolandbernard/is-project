package secure.middleware;

import org.springframework.web.servlet.*;

import jakarta.servlet.http.*;
import secure.Random;

public class PerformAuth implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var session = request.getSession(true);
        if (session.getAttribute("csrf-token") == null) {
            var token = Random.instance().nextBytesBase64(32);
            session.setAttribute("csrf-token", token);
        }
        request.setAttribute("user", session.getAttribute("user"));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (modelAndView != null)
            modelAndView.getModel().put("user", request.getAttribute("user"));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}
