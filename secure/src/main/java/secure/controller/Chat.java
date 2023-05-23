package secure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.*;
import secure.Database;
import secure.model.*;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/chat")
class Chat {
    @GetMapping
    public String chats(HttpServletRequest request, HttpSession session, Model model) throws Exception {
        try (Database db = new Database()) {
            User user = (User) request.getAttribute("user");
            var messages = Message.getLastInvolving(db, user.id);
            model.addAttribute("messages", messages);
            return "chat/list";
        }
    }

    @GetMapping("/{otherId}")
    public String chat(@PathVariable String otherId, HttpServletRequest request, Model model) throws Exception {
        try (Database db = new Database()) {
            User user = (User) request.getAttribute("user");
            User otherUser = User.getUser(db, otherId);
            if (user.isVendor == otherUser.isVendor) {
                throw new Exception("Only Customer and Vendor can have a chat");
            }
            if (user.id == otherId) {
                throw new Exception("You cannot send message to yourself");
            }
            var messages = Message.getBetween(db, user.id, otherId);
            model.addAttribute("messages", messages);
            model.addAttribute("otherUser", User.getUser(db, otherId));
            return "chat/messages";
        }
    }

    @PostMapping("/{otherId}")
    public String chat(@PathVariable String otherId, @RequestParam String message, HttpServletRequest request,
            Model model) throws Exception {
        try (Database db = new Database()) {
            User user = (User) request.getAttribute("user");
            User otherUser = User.getUser(db, otherId);
            if (user.isVendor == otherUser.isVendor) {
                throw new Exception("Only Customer and Vendor can send messages");
            }
            if (user.id == otherId) {
                throw new Exception("You cannot send message to yourself");
            }
            Message.create(db, user.id, otherId, message);
            return "redirect:/chat/" + otherId;
        }
    }
}
