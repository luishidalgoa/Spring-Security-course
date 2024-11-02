package dev.luishidalgoa.spring_section4.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticeController {

    @GetMapping("/notices")
    public String getNotices() {
        return "Notice Details";
    }
}
