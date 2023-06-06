package insecure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String postCreate(@RequestParam String name, @RequestParam("price") String priceString, Model model,
            HttpServletRequest request, RedirectAttributes ra) throws Exception {
        User user = (User) request.getAttribute("user");
        try (var db = new Database()) {
            try {
                int price = Utils.parseNumber(priceString);
                var product = Product.create(db, name, price, user.id);
                return "redirect:/product/" + product.id;
            } catch (NumberFormatException e) {
                model.addAttribute("error", "Price must be a number");
            }
        }
        model.addAttribute("user", user);
        return "product/create";
    }

    @GetMapping("/search")
    public String getSearch(@RequestParam String keyword, Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            var user = (User) request.getAttribute("user");
            var products = Product.search(db, keyword);
            model.addAttribute("products", products);
            model.addAttribute("user", user);
            model.addAttribute("keyword", keyword);
            model.addAttribute("title", "Search results for \"" + keyword + "\"");
            return "product/list";
        }
    }

    @GetMapping("/{id}")
    public String get(@PathVariable int id, Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            var productVendor = Product.getProduct(db, id);
            var user = (User) request.getAttribute("user");
            if (productVendor == null) {
                throw new Exception("Product not found");
            }
            var product = productVendor.product();
            var reviews = Response.getForProduct(db, product.id);
            model.addAttribute("product", productVendor);
            model.addAttribute("isOwner", product.userId == user.id);
            model.addAttribute("hasReviewed", reviews.stream().anyMatch(review -> review.review().userId == user.id));
            model.addAttribute("reviews", reviews);
            model.addAttribute("user", user);
            return "product/info";
        }
    }

    @PostMapping("/{productId}/review/{reviewId}/response")
    public String postResponse(@PathVariable int productId, @PathVariable int reviewId, @RequestParam String comment,
            HttpServletRequest request, RedirectAttributes ra) throws Exception {
        try (var db = new Database()) {
            User user = (User) request.getAttribute("user");
            Response.create(db, reviewId, user.id, comment);
            return "redirect:/product/" + productId;
        }
    }

    @PostMapping("/{id}/review")
    public String postReview(@PathVariable int id, @RequestParam String comment, @RequestParam() int rating,
            Model model, HttpServletRequest request, RedirectAttributes ra) throws Exception {
        try (var db = new Database()) {
            User user = (User) request.getAttribute("user");
            var product = Product.getProduct(db, id).product();
            if (product == null) {
                throw new Exception("Product not found");
            }
            Review.create(db, user.id, product.id, rating, comment);
            return "redirect:/product/" + product.id;
        }
    }

    @GetMapping("/my")
    public String getMy(Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            User user = (User) request.getAttribute("user");
            var products = Product.getProducts(db, user.id);
            model.addAttribute("products", products);
            model.addAttribute("user", user);
            model.addAttribute("title", "My products");
            return "product/list";
        }
    }

    @GetMapping("/all")
    public String getAll(Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            var user = (User) request.getAttribute("user");
            var products = Product.getProducts(db);
            model.addAttribute("products", products);
            model.addAttribute("user", user);
            model.addAttribute("title", "All products");
            model.addAttribute("keyword", "");
            return "product/list";
        }
    }

}
