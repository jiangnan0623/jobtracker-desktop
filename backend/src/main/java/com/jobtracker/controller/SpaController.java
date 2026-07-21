package com.jobtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping(value = {
            "/",
            "/dashboard",
            "/applications",
            "/application/{id}",
            "/resume",
            "/notes",
            "/reminder",
            "/settings"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
