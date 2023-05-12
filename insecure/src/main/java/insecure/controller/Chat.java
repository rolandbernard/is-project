package insecure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import insecure.Database;
import insecure.model.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/chat")
class Chat {
    @GetMapping
    public String chats(
            HttpServletRequest request,
            Model model) throws Exception {
        try (Database db = new Database()) {
            User user = (User) request.getAttribute("user");
            var messages = Message.getLastInvolving(db, user.id);
            model.addAttribute("message", messages);
            model.addAttribute("user", user);
            return "chat/list";
        }
    }
}
