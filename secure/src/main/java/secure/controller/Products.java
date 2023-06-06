package secure.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.*;
import secure.Database;
import secure.Utils;
import secure.model.*;

@Controller()
@RequestMapping("/product")
public class Products {
    @GetMapping("/create")
    public String getCreate(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (!user.isVendor) {
            return "redirect:/product/all";
        }
        return "product/create";
    }

    @PostMapping("/create")
    public String postCreate(@RequestParam String name, @RequestParam("price") String priceString,
            @RequestParam String description, @RequestParam(required = false) MultipartFile image, Model model,
            HttpServletRequest request) throws Exception {
        User user = (User) request.getAttribute("user");
        if (!user.isVendor) {
            return "redirect:/product/all";
        }
        try (var db = new Database()) {
            var errors = new ArrayList<String>();
            if (name.length() < 3) {
                errors.add("Product name must be at least 3 characters long");
            } else if (name.length() > 255) {
                errors.add("Product name must be at most 255 characters long");
            }
            if (image != null && image.getSize() > 128 * 1024) {
                errors.add("Images must be no larger than 128 KiB");
            }
            long price = 0;
            try {
                price = Utils.parseNumber(priceString);
            } catch (NumberFormatException e) {
                errors.add("Price must be a number");
            }
            if (errors.isEmpty()) {
                var product = Product.create(db, name, price, description,
                        image != null && !image.isEmpty() ? image.getBytes() : null,
                        user.id);
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
                return "redirect:/product/all";
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

    @GetMapping("/{id}/image")
    public @ResponseBody byte[] getImage(@PathVariable String id) throws Exception {
        try (var db = new Database()) {
            var productVendor = Product.getProduct(db, id);
            if (productVendor == null) {
                return null;
            }
            var product = productVendor.product();
            return product.image;
        }
    }

    @PostMapping("/{productId}/review/{reviewId}/response")
    public String postResponse(@PathVariable String productId, @PathVariable String reviewId,
            @RequestParam String comment, HttpServletRequest request) throws Exception {
        User user = (User) request.getAttribute("user");
        try (var db = new Database()) {
            if (!comment.isBlank()) {
                var review = Review.getReview(db, reviewId);
                if (review != null && review.productId.equals(productId)) {
                    var product = Product.getProduct(db, productId);
                    if (product != null && (review.userId.equals(user.id) || product.vendor().id.equals(user.id))) {
                        Response.create(db, reviewId, user.id, comment);
                    }
                }
            }
            return "redirect:/product/" + productId;
        }
    }

    @PostMapping("/{id}/review")
    public String postReview(@PathVariable String id, @RequestParam String comment, @RequestParam int rating,
            Model model, HttpServletRequest request) throws Exception {
        User user = (User) request.getAttribute("user");
        if (user.isVendor) {
            return "redirect:/product/" + id;
        }
        try (var db = new Database()) {
            var product = Product.getProduct(db, id).product();
            if (product == null) {
                return "redirect:/product/" + id;
            }
            var hasBoughtProduct = Order.userBoughtProduct(db, user.id, id);
            if (!hasBoughtProduct) {
                return "redirect:/product/" + id;
            }
            var reviews = Response.getForProduct(db, product.id);
            if (reviews.stream().anyMatch(review -> review.review().userId.equals(user.id))) {
                return "redirect:/product/" + id;
            }
            Review.create(db, user.id, product.id, rating, comment);
            return "redirect:/product/" + product.id;
        }
    }

    @GetMapping("/my")
    public String getMy(Model model, HttpServletRequest request) throws Exception {
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
