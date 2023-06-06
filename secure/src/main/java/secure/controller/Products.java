package secure.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.*;
import secure.Database;
import secure.Utils;
import secure.model.*;

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
        if (!user.isVendor) {
            throw new Exception("Only vendors can create products");
        }
        try (var db = new Database()) {
            var errors = new ArrayList<String>();
            if (name.length() > 255) {
                errors.add("Product name must be at most 255 characters long");
            }
            int price = 0;
            try {
                price = Utils.parseNumber(priceString);
            } catch (NumberFormatException e) {
                errors.add("Price must be a number");
            }
            if (errors.isEmpty()) {
                var product = Product.create(db, name, price, user.id);
                return "redirect:/product/" + product.id;
            }
            model.addAttribute("errors", errors);
        }
        return "product/create";
    }

    @GetMapping("/search")
    public String getSearch(@RequestParam String keyword, Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            var products = Product.search(db, keyword);
            model.addAttribute("products", products);
            model.addAttribute("keyword", keyword);
            model.addAttribute("title", "Search results for \"" + keyword + "\"");
            return "product/list";
        }
    }

    @GetMapping("/{id}")
    public String get(@PathVariable String id, Model model, HttpSession session, HttpServletRequest request)
            throws Exception {
        var user = (User) request.getAttribute("user");
        try (var db = new Database()) {
            var productVendor = Product.getProduct(db, id);
            if (productVendor == null) {
                throw new Exception("Product not found");
            }
            var product = productVendor.product();
            var reviews = Response.getForProduct(db, product.id);
            var hasBoughtProduct = Order.userBoughtProduct(db, user.id, id);
            model.addAttribute("product", productVendor);
            model.addAttribute("isOwner", product.userId.equals(user.id));
            model.addAttribute("hasReviewed",
                    reviews.stream().anyMatch(review -> review.review().userId.equals(user.id)));
            model.addAttribute("hasBought", hasBoughtProduct);
            model.addAttribute("reviews", reviews);
            return "product/info";
        }
    }

    @GetMapping("/{id}/order")
    public String getOrder(@PathVariable String id, Model model) throws Exception {
        try (var db = new Database()) {
            var productVendor = Product.getProduct(db, id);
            var product = productVendor.product();
            model.addAttribute("product", product);
            return "product/order";
        }
    }

    @PostMapping("/{productId}/review/{reviewId}/response")
    public String postResponse(@PathVariable String productId, @PathVariable String reviewId,
            @RequestParam String comment, HttpServletRequest request, RedirectAttributes ra) throws Exception {
        User user = (User) request.getAttribute("user");
        try (var db = new Database()) {
            Response.create(db, reviewId, user.id, comment);
            return "redirect:/product/" + productId;
        }
    }

    @PostMapping("/{id}/review")
    public String postReview(@PathVariable String id, @RequestParam String comment, @RequestParam() int rating,
            Model model, HttpServletRequest request, RedirectAttributes ra) throws Exception {
        User user = (User) request.getAttribute("user");
        try (var db = new Database()) {
            var product = Product.getProduct(db, id).product();
            if (product == null) {
                throw new Exception("Product not found");
            }
            var hasBoughtProduct = Order.userBoughtProduct(db, user.id, id);
            if (!hasBoughtProduct) {
                throw new Exception("You must buy the product to review it");
            }
            var reviews = Response.getForProduct(db, product.id);
            var hasReview = reviews.stream().anyMatch(review -> review.review().userId.equals(user.id));
            if (user.isVendor || (!user.isVendor && hasReview)) {
                throw new Exception("Only customers can review once");
            }
            Review.create(db, user.id, product.id, rating, comment);
            return "redirect:/product/" + product.id;
        }
    }

    @GetMapping("/my")
    public String getMy(Model model, HttpServletRequest request, RedirectAttributes ra) throws Exception {
        User user = (User) request.getAttribute("user");
        if (!user.isVendor) {
            return "redirect:/product/all";
        }
        try (var db = new Database()) {
            var products = Product.getProducts(db, user.id);
            model.addAttribute("products", products);
            model.addAttribute("title", "My products");
            return "product/list";
        }
    }

    @GetMapping("/all")
    public String getAll(Model model, HttpServletRequest request) throws Exception {
        try (var db = new Database()) {
            var products = Product.getProducts(db);
            model.addAttribute("products", products);
            model.addAttribute("title", "All products");
            model.addAttribute("keyword", "");
            return "product/list";
        }
    }
}
