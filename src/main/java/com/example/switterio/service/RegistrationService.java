package com.example.switterio.service;

import com.example.switterio.Controller.ControllerUtil;
import com.example.switterio.domain.User;
import com.example.switterio.domain.dto.CaptchaResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@Service
public class RegistrationService {
    private final static String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    @Value("${recaptcha.secret}")
    private String secret;
    private final UserService userService;
    private final RestTemplate restTemplate;

    public RegistrationService(UserService userService, RestTemplate restTemplate) {
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    public boolean addUser(@RequestParam("g-recaptcha-response") String captchaResponse, @Valid User user, BindingResult bindingResult, Model model) {
        String url = String.format(CAPTCHA_URL, secret, captchaResponse);
        CaptchaResponseDTO response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDTO.class);
        if (!response.isSuccess()) {
            model.addAttribute("captchaError", "Fill captcha");
        }

        if (bindingResult.hasErrors() || !response.isSuccess()) {
            Map<String, String> errors = ControllerUtil.getErrors(bindingResult);
            model.mergeAttributes(errors);
            return false;
        }

        if (user.getPassword() != null && !user.getPassword().equals(user.getPassword2())) {
            model.addAttribute("passwordError", "Passwords are different!");
            return false;
        }

        if (!userService.addUser(user)) {
            model.addAttribute("usernameError", "User exists!");
            return false;
        }

        return true;
    }

    public boolean activateUser(Model model, String code) {
        boolean isActivated = userService.activateUser(code);

        if (isActivated) {
            model.addAttribute("messageType", "success");
            model.addAttribute("message", "User successfully activated");
        } else {
            model.addAttribute("messageType", "danger");
            model.addAttribute("message", "Activation code is not found!");
        }

        return isActivated;
    }
}