package insecure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import insecure.model.User;
import jakarta.servlet.http.HttpServletRequest;

@Controller
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
