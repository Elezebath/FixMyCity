package lv.acnbootcamp.fixmycity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles SPA routing by forwarding frontend routes to index.html,
 * allowing React Router to handle client-side navigation.
 */

@Controller
public class SpaController {

    @RequestMapping(value = {
            "/",
            "/app",
            "/app/**",
            "/forgot-password",
            "/reset-password"
    })
    public String forward() {
        return "forward:/index.html";
    }
}