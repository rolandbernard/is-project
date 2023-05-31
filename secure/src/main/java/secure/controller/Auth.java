package secure.controller;

import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.*;
import secure.*;
import secure.model.*;

@Controller
@RequestMapping("/auth")
public class Auth {
    private static void setAuthSession(HttpServletRequest request, User user) {
        var session = request.getSession();
        session.setAttribute("user", user);
    }

    @GetMapping("/logout")
    public String getLogout(Model model, HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession();
        session.removeAttribute("user");
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String getLogin(HttpSession session, Model model, HttpServletRequest request) {
        String origin = request.getParameter("origin");
        model.addAttribute("origin", origin);
        return "auth/login";
    }

    @PostMapping("/login")
    public String postLogin(HttpSession session, @RequestParam("csrf-token") String csrfToken,
            @RequestParam String username, @RequestParam String password, @RequestParam(required = false) String origin,
            Model model, HttpServletRequest request) throws Exception {
        model.addAttribute("username", username);
        try (var db = new Database()) {
            var user = User.getUser(db, username, password);
            if (user == null) {
                model.addAttribute("error", "Invalid username or password");
                model.addAttribute("origin", origin);
                return "auth/login";
            }
            setAuthSession(request, user);
            return "redirect:" + (origin == null || origin.isBlank() ? "/" : origin);
        }
    }

    @GetMapping("/register")
    public String getRegister(Model model, HttpServletRequest request, HttpSession session) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String postRegister(HttpSession session, @RequestParam String username, @RequestParam String password,
            @RequestParam("password-repeat") String passwordRepeat,
            @RequestParam(defaultValue = "false") boolean vendor, Model model, HttpServletRequest request)
            throws Exception {
        model.addAttribute("username", username);
        try (var db = new Database()) {
            var errors = Utils.validateUsername(username);
            errors.addAll(Utils.validatePassword(password, passwordRepeat));
            if (!User.isUsernameFree(db, username)) {
                errors.add("Username already exists");
            }
            if (errors.size() > 0) {
                model.addAttribute("errors", errors);
                return "auth/register";
            }
            var user = User.create(db, username, password, vendor);
            if (user == null) {
                model.addAttribute("errors", List.of("Failed to create user"));
                return "auth/register";
            }
            setAuthSession(request, user);
            return "redirect:/";
        }
    }

    @GetMapping("/password")
    public String postPassword(Model model, HttpServletRequest request, HttpSession session) throws Exception {
        return "auth/password";
    }

    @PostMapping("/password")
    public String postPassword(HttpSession session, @RequestParam String password,
            @RequestParam("new-password") String newPassword, @RequestParam("password-repeat") String passwordRepeat,
            Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            var user = (User) request.getAttribute("user");
            var errors = Utils.validatePassword(newPassword, passwordRepeat);
            if (!errors.isEmpty() || User.getUser(db, user.username, password) == null) {
                errors.add("The old password is wrong");
                model.addAttribute("errors", errors);
                return "auth/password";
            }
            User.changePassword(db, user, newPassword);
            return "redirect:/";
        }
    }
}
