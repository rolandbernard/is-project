package insecure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import insecure.model.*;
import jakarta.servlet.http.HttpServletRequest;
import insecure.Database;
import insecure.Utils;

@Controller()
@RequestMapping("/product")
public class Products {
    @GetMapping("/create")
    public String getCreate() {
        return "product/create";
    }

    @PostMapping("/create")
    public String postCreate(
            @RequestParam String name, @RequestParam("price") String priceString, Model model,
            HttpServletRequest request)
            throws Exception {
        try (var db = new Database()) {
            try {
                int price = Utils.parseNumber(priceString);
                User user = (User) request.getAttribute("user");
                var product = Product.create(db, name, price, user.id);
                return "redirect:/product/" + product.id;
            } catch (NumberFormatException e) {
                model.addAttribute("error", "Price must be a number");
            }
        }
        return "product/create";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable int id, Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            var product = Product.getProduct(db, id);
            User user = (User) request.getAttribute("user");
            if (product == null) {
                throw new Exception("Product not found");
            }
            model.addAttribute("product", product);
            model.addAttribute("isOwner", product.userId == user.id);
        }
        return "product/info";
    }

    @GetMapping("/my")
    public String getMy(Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            User user = (User) request.getAttribute("user");
            var products = Product.getProducts(db, user.id);
            model.addAttribute("products", products);
        }
        model.addAttribute("title", "My products");
        return "product/list";
    }

    @GetMapping("/all")
    public String getAll(Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            var products = Product.getProducts(db);
            model.addAttribute("products", products);
        }
        model.addAttribute("title", "All products");
        return "product/list";
    }

}
