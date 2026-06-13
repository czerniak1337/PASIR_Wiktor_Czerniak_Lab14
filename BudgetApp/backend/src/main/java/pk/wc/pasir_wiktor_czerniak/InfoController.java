package pk.wc.pasir_wiktor_czerniak;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InfoController {

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("appName", "Aplikacja Budzetowa");
        info.put("version", "1.0");
        info.put("message", "Witaj w aplikacji budzetowej stworzonej ze Spring Boot!");
        return info;
    }
}