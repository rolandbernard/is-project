package secure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import secure.model.User;

@Controller
@RequestMapping("/")
public class Home {
    @GetMapping
    public String getHome(Model model, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return "index";
        }
        return "home";
    }
}