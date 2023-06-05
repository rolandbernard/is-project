package insecure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import insecure.Database;
import insecure.model.*;

@Controller
@RequestMapping("/")
public class Home {
    @GetMapping
    public String getHome(Model model, HttpServletRequest request) throws Exception {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return "index";
        }
        try (Database db = new Database()) {
            var messages = Message.getLastInvolving(db, user.id);
            model.addAttribute("messages", messages);
            model.addAttribute("orders", user.isVendor ? Order.getForUser(db, user.id) : Order.getByUser(db, user.id));
            model.addAttribute("title", user.isVendor ? "Orders for me" : "My Orders");
        }
        return "home";
    }
}
