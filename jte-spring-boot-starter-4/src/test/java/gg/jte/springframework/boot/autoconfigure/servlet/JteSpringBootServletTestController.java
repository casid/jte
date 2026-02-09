package gg.jte.springframework.boot.autoconfigure.servlet;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class JteSpringBootServletTestController {

    @GetMapping("/greet")
    public String greeting(@RequestParam("subject") String subject, Model model) {
        model.addAttribute("subject", subject);
        return "greeting";
    }
}
