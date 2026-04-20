package pk.wc.pasir_wiktor_czerniak;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "Hello, World!";
    }
    @GetMapping("/info")
    public java.util.Map<String, String> getInfo() {
        java.util.Map<String, String> info = new java.util.HashMap<>();
        info.put("appName", "Aplikacja Budżetowa");
        info.put("version", "1.0");
        info.put("message", "Witaj w aplikacji budżetowej stworzonej ze Spring Boot!");
        return info;
    }
}