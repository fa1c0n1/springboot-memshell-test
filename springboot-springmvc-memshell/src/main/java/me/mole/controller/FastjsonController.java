package me.mole.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fj1247")
public class FastjsonController {

    @RequestMapping("/hello")
    public String hello() {
        return "Hello, SpringMVC Memshell!";
    }

    /**
     *
     *
     *
     * @param jsonStr
     * @return
     */
    @PostMapping(value = "/hackfastjson")
    public String hackfastjson(@RequestBody String jsonStr) {
        System.out.println("s===" + jsonStr);
        Object jsonObject = JSON.parse(jsonStr);
        return "ok";
    }
}