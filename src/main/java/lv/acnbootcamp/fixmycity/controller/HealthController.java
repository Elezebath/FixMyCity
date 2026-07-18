package lv.acnbootcamp.fixmycity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health-check controller used to verify that the application
 * is up and reachable (e.g. after a Docker/CI deployment).
 */

@RestController
public class HealthController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}