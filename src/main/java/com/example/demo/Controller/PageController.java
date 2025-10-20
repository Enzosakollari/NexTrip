package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
//endpoint for login and sign up page
    /*if we see closly it returns sstring so to access the htmls that are inside the resources/templates folder
    * we have the thymeleaf template engine that gets these stings and searches the templates folder and loads the html*/
    @GetMapping("/login")
    public String login() {
        return "login";
    }
//signup page
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
//the landing page
    @GetMapping("/index")
    public String home() {
        return "index";
    }
//default page
    @GetMapping("/")
    public String root() {
        return "login";
    }
}
