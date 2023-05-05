package insecure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import insecure.Database;
import insecure.model.*;
import jakarta.servlet.http.*;

@Controller
public class Auth {
    @GetMapping("/logout")
    public String getLogout(Model model, HttpServletRequest request, HttpServletResponse response) {
        var cookies = request.getCookies();
        for (var cookie : cookies) {
            if (cookie.getName().equals("user-id")) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
                break;
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String getLogin(Model model, HttpServletRequest request) {
        return "login";
    }

    @PostMapping("/login")
    public String postLogin(@RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password, Model model, HttpServletResponse response) {
        try {
            try (var db = new Database()) {
                var user = User.getUser(db, username, password);
                if (user == null) {
                    model.addAttribute("error", "Invalid username or password");
                    return "login";
                }
                Cookie cookie = new Cookie("user-id", String.valueOf(user.id));
                response.addCookie(cookie);
                return "redirect:/";
            }
        } catch (Exception e) {
            return "server-error";
        }
    }

    @GetMapping("/register")
    public String getRegister(Model model, HttpServletRequest request) {
        return "register";
    }

    @PostMapping("/register")
    public String postRegister(@RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password, Model model, HttpServletResponse response) {
        try {
            try (var db = new Database()) {
                var user = User.create(db, username, password);
                if (user == null) {
                    model.addAttribute("error", "Username already exists");
                    return "register";
                }
                Cookie cookie = new Cookie("user-id", String.valueOf(user.id));
                response.addCookie(cookie);
                return "redirect:/";
            }
        } catch (Exception e) {
            return "server-error";
        }
    }
}
