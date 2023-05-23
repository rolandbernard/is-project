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
        try (var db = new Database()) {
            User user = (User) request.getAttribute("user");
            Order order = Order.create(db, productId, user);
            model.addAttribute("order", order);
            return "order/success";
        }
    }

    @GetMapping()
    public String getList(Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            User user = (User) request.getAttribute("user");
            model.addAttribute("orders", user.isVendor ? Order.getForUser(db, user.id) : Order.getByUser(db, user.id));
            model.addAttribute("title", user.isVendor ? "Orders for me" : "My Orders");
            return "order/list";
        }
    }
}
