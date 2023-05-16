package secure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.*;
import secure.Database;
import secure.model.*;

@Controller
@RequestMapping("/auth")
public class Auth {
    private static void addAuthCookie(HttpServletResponse response, User user) {
        Cookie cookie = new Cookie("user-id", user.id);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @GetMapping("/logout")
    public String getLogout(Model model, HttpServletRequest request, HttpServletResponse response) {
        var cookies = request.getCookies();
        for (var cookie : cookies) {
            if (cookie.getName().equals("user-id")) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
                break;
            }
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String getLogin(Model model, HttpServletRequest request) {
        String origin = request.getParameter("origin");
        model.addAttribute("origin", origin);
        return "auth/login";
    }

    @PostMapping("/login")
    public String postLogin(@RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @RequestParam(value = "origin", required = false) String origin, Model model, HttpServletResponse response)
            throws Exception {
        model.addAttribute("username", username);
        try (var db = new Database()) {
            var user = User.getUser(db, username, password);
            if (user == null) {
                model.addAttribute("error", "Invalid username or password");
                model.addAttribute("origin", origin);
                return "auth/login";
            }
            addAuthCookie(response, user);
            return "redirect:" + (origin == null || origin.isBlank() ? "/" : origin);
        }
    }

    @GetMapping("/register")
    public String getRegister(Model model, HttpServletRequest request) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String postRegister(@RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @RequestParam(value = "password-repeat") String passwordRepeat,
            @RequestParam(defaultValue = "false") boolean vendor,
            Model model, HttpServletResponse response)
            throws Exception {
        model.addAttribute("username", username);
        if (!password.equals(passwordRepeat)) {
            model.addAttribute("error", "The two passwords are not equal");
            return "auth/register";
        }
        try (var db = new Database()) {
            var user = User.create(db, username, password, vendor);
            if (user == null) {
                model.addAttribute("error", "Username already exists");
                return "auth/register";
            }
            addAuthCookie(response, user);
            return "redirect:/";
        }
    }
}
