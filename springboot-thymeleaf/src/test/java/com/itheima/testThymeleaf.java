package com.itheima;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.Model;

@SpringBootTest
@RunWith(SpringRunner.class)
public class testThymeleaf {

    @Test
    public String hello(Model model) {
        model.addAttribute("message", "hello thymeleaf");
        return null;
    }
}
