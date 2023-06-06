package secure.controller;

import java.net.URI;
import java.net.URISyntaxException;
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
    @GetMapping("/logout")
    public String getLogout(Model model, HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession();
        session.removeAttribute("user");
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String getLogin(HttpSession session, Model model, HttpServletRequest request) {
        var user = (User) request.getAttribute("user");
        String origin = request.getParameter("origin");
        if (user != null) {
            if (validateOriginRedirect(origin)) {
                return "redirect:" + origin;
            } else {
                return "redirect:/";
            }
        }
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
                Thread.sleep(1000);
                model.addAttribute("error", "Invalid username or password");
                model.addAttribute("origin", origin);
                return "auth/login";
            }
            setAuthSession(request, user);
            if (validateOriginRedirect(origin)) {
                return "redirect:" + origin;
            } else {
                return "redirect:/";
            }
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
            var errors = validateUsername(username);
            errors.addAll(validatePassword(password, passwordRepeat));
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
    public String postPassword(HttpSession session, @RequestParam("old-password") String password,
            @RequestParam("password") String newPassword, @RequestParam("password-repeat") String passwordRepeat,
            Model model, HttpServletRequest request) throws Exception {
        var user = (User) request.getAttribute("user");
        try (var db = new Database()) {
            var errors = validatePassword(newPassword, passwordRepeat);
            if (User.getUser(db, user.username, password) == null) {
                Thread.sleep(1000);
                errors.add("The old password is wrong");
            }
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                return "auth/password";
            }
            User.changePassword(db, user, newPassword);
            return "redirect:/";
        }
    }

    private static void setAuthSession(HttpServletRequest request, User user) {
        var session = request.getSession();
        session.setAttribute("user", user);
    }

    /**
     * Validate the origin redirect after successful login. Allow only absolute redirects inside this application.
     *
     * @param origin the utl to validate
     * @return true if redirect is allowed, false otherwise.
     */
    private static boolean validateOriginRedirect(String origin) {
        try {
            var uri = new URI(origin);
            System.out.println(uri.getPath());
            System.out.println(uri.getAuthority());
            System.out.println(uri.getScheme());
            return uri.getPath().startsWith("/") && uri.getAuthority() == null && uri.getScheme() == null;
        } catch (URISyntaxException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Validate password.
     *
     * @param password
     * @param repeatPassword
     * @return true if password is valid, false otherwise.
     */
    private static List<String> validatePassword(String password, String repeatPassword) {
        var errors = new ArrayList<String>();
        if (!password.equals(repeatPassword)) {
            errors.add("The two password must be equal.");
        }
        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[0-9].*")) {
            errors.add("Password must contain at least one digit.");
        }
        if (!password.matches(".*\\W.*")) {
            errors.add("Password must contain at least one special character.");
        }
        return errors;
    }

    /**
     * Validate username.
     *
     * @param username
     * @return error messages if username is invalid, null otherwise.
     */
    private static List<String> validateUsername(String username) {
        var errors = new ArrayList<String>();
        if (username.length() < 3) {
            errors.add("Username must be at least 3 characters long.");
        }
        if (username.length() > 255) {
            errors.add("Username must be at most 255 characters long.");
        }
        if (!username.matches("[._a-zA-Z0-9]+")) {
            errors.add("Username must only contain alphanumeric characters.");
        }
        if (username.matches(".*\\s.*")) {
            errors.add("Username must not contain whitespace.");
        }
        return errors;
    }
}
