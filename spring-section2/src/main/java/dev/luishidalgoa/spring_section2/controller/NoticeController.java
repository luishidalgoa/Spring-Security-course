package dev.luishidalgoa.spring_section2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticeController {

    @GetMapping("/notice")
    public String getNotices() {
        return "Notice Details";
    }
}
