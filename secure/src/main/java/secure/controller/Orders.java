package secure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import secure.Database;
import secure.model.*;

@Controller()
@RequestMapping("/order")
public class Orders {
    @PostMapping("/create/{id}")
    public String postCreate(@PathVariable("id") String productId, Model model, HttpServletRequest request)
            throws Exception {
        User user = (User) request.getAttribute("user");
        if (user.isVendor) {
            return "redirect:/product/" + productId;
        }
        try (var db = new Database()) {
            Order order = Order.create(db, productId, user);
            return "redirect:/order/" + order.id;
        }
    }

    @GetMapping("/{id}")
    public String getOrder(@PathVariable("id") String productId, Model model, HttpServletRequest request)
            throws Exception {
        User user = (User) request.getAttribute("user");
        try (var db = new Database()) {
            var order = Order.getOrder(db, user, productId);
            if (order == null) {
                throw new Exception("Order not found");
            }
            model.addAttribute("order", order);
            return "order/success";
        }
    }

    @GetMapping()
    public String getList(Model model, HttpServletRequest request) throws Exception {
        User user = (User) request.getAttribute("user");
        try (var db = new Database()) {
            model.addAttribute("orders", user.isVendor ? Order.getForUser(db, user) : Order.getByUser(db, user));
            model.addAttribute("title", user.isVendor ? "Orders for me" : "My Orders");
            return "order/list";
        }
    }
}
