package secure;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HelloWorld {
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);
        return "hello";
    }
}
