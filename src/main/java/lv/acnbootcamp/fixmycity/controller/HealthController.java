package lv.acnbootcamp.fixmycity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// test controller for health check
@RestController
public class HealthController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}