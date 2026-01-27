package com.easyconfig.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easyconfig.server.entity.Environment;
import com.easyconfig.server.service.EnvironmentService;

@RestController
@RequestMapping("/env")
public class EnvironmentController {

    @Autowired
    EnvironmentService environmentService;

    @GetMapping("/test")
    public Environment test(@RequestParam String name) {
        return environmentService.getEnvironmentByName(name);
    }
}
