package me.mole.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apollo/config")
public class ApolloConfigTest {

    @Value("${k11:hello}")
    private String testk1;

    @RequestMapping("/get1")
    public String getVal1() {
        return testk1;
    }
}
