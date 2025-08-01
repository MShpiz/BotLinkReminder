package backend.academy.bot.controllers;

import backend.academy.bot.RateLimit.WithRateLimitProtection;
import backend.academy.bot.models.UpdateInfo;
import backend.academy.bot.services.UpdateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateController {

    private final UpdateService service;

    public UpdateController(@Autowired UpdateService service) {
        this.service = service;
    }

    @PostMapping("/updates")
    @WithRateLimitProtection
    public void st(@Valid @RequestBody UpdateInfo info) {
        service.update(info);
    }
}
