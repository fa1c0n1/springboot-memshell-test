package me.mole.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class ControllerTest {

    //映射访问路径
    @RequestMapping("/index")
    public String index(){
        String tempFolder = System.getProperty("java.io.tmpdir");
        System.out.println("tempFolde=" + tempFolder);
        return "test";
    }
}
