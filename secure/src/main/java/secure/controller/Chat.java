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
        User user = (User) request.getAttribute("user");
        try (Database db = new Database()) {
            var messages = Message.getLastInvolving(db, user);
            model.addAttribute("messages", messages);
            return "chat/list";
        }
    }

    @GetMapping("/{otherId}")
    public String chat(@PathVariable String otherId, HttpServletRequest request, Model model) throws Exception {
        User user = (User) request.getAttribute("user");
        if (user.id.equals(otherId)) {
            return "redirect:/chat";
        }
        try (Database db = new Database()) {
            User otherUser = User.getUser(db, otherId);
            if (user.isVendor == otherUser.isVendor) {
                return "redirect:/chat";
            }
            var messages = Message.getBetween(db, user, otherId);
            model.addAttribute("messages", messages);
            model.addAttribute("otherUser", User.getUser(db, otherId));
            return "chat/messages";
        }
    }

    @PostMapping("/{otherId}")
    public String chat(@PathVariable String otherId, @RequestParam String message, HttpServletRequest request,
            Model model) throws Exception {
        User user = (User) request.getAttribute("user");
        if (user.id.equals(otherId)) {
            return "redirect:/chat";
        }
        try (Database db = new Database()) {
            User otherUser = User.getUser(db, otherId);
            if (user.isVendor == otherUser.isVendor) {
                return "redirect:/chat";
            }
            Message.create(db, user, otherId, message);
            return "redirect:/chat/" + otherId;
        }
    }
}
