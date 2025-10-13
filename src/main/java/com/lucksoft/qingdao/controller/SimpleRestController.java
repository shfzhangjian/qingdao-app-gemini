
package com.lucksoft.qingdao.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api")
public class SimpleRestController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World! The time is " + new Date();
    }
}
