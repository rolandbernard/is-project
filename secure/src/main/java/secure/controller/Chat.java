package secure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import secure.Database;
import secure.model.*;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/chat")
class Chat {
    @GetMapping
    public String chats(HttpServletRequest request, Model model) throws Exception {
        try (Database db = new Database()) {
            User user = (User) request.getAttribute("user");
            var messages = Message.getLastInvolving(db, user.id);
            model.addAttribute("messages", messages);
            model.addAttribute("user", user);
            return "chat/list";
        }
    }

    @GetMapping("/{otherId}")
    public String chat(@PathVariable int otherId, HttpServletRequest request, Model model) throws Exception {
        try (Database db = new Database()) {
            User user = (User) request.getAttribute("user");
            var messages = Message.getBetween(db, user.id, otherId);
            model.addAttribute("messages", messages);
            model.addAttribute("user", user);
            model.addAttribute("otherUser", User.getUser(db, otherId));
            return "chat/messages";
        }
    }

    @PostMapping("/{otherId}")
    public String chat(@PathVariable int otherId, @RequestParam String message, HttpServletRequest request, Model model)
            throws Exception {
        try (Database db = new Database()) {
            User user = (User) request.getAttribute("user");
            Message.create(db, user.id, otherId, message);
            return "redirect:/chat/" + otherId;
        }
    }
}
