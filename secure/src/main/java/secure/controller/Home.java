package secure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import secure.Database;
import secure.model.*;

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
            var messages = Message.getLastInvolving(db, user);
            model.addAttribute("messages", messages.subList(0, Integer.min(10, messages.size())));
            var orders = user.isVendor ? Order.getForUser(db, user) : Order.getByUser(db, user);
            model.addAttribute("orders", orders.subList(0, Integer.min(10, orders.size())));
            model.addAttribute("title", user.isVendor ? "Orders for me" : "My Orders");
        }
        return "home";
    }
}
