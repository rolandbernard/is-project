package secure.middleware;

import org.springframework.web.servlet.*;

import jakarta.servlet.http.*;

import java.net.URLEncoder;

public class CheckCsrfToken implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!request.getMethod().equals("GET")) {
            var session = request.getSession(true);
            var requestToken = request.getParameter("csrf-token");
            var sessionToken = session.getAttribute("csrf-token");
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                session.removeAttribute("user-id");
                session.removeAttribute("csrf-token");
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
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.getModel().put("csrfToken", request.getSession().getAttribute("csrf-token"));
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}
