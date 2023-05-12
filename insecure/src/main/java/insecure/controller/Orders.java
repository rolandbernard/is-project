package insecure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import insecure.model.*;
import jakarta.servlet.http.HttpServletRequest;
import insecure.Database;

@Controller()
@RequestMapping("/order")
public class Orders {
    @PostMapping("/create/{id}")
    public String postCreate(
            @PathVariable("id") int productId,
            Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            User user = (User) request.getAttribute("user");
            Order order = Order.create(db, productId, user.id);
            model.addAttribute("order", order);
            model.addAttribute("user", user);
            return "order/success";
        }
    }

    @GetMapping("/by-me")
    public String getList(
            Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            User user = (User) request.getAttribute("user");
            model.addAttribute("orders", Order.getByUser(db, user.id));
            model.addAttribute("user", user);
            model.addAttribute("title", "My Orders");
            return "order/list";
        }
    }

    @GetMapping("/for-me")
    public String getForMe(
            Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            User user = (User) request.getAttribute("user");
            model.addAttribute("user", user);
            model.addAttribute("orders", Order.getForUser(db, user.id));
            model.addAttribute("title", "Orders for Me");
            return "order/list";
        }
    }
}
