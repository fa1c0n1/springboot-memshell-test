package me.mole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringMVCMemshellApplication {

    public static void main(String[] args) {
        String tempFolder = System.getProperty("java.io.tmpdir");
        System.out.println("java.io.tmpdir => " + tempFolder);
        SpringApplication.run(SpringMVCMemshellApplication.class, args);
    }

}
