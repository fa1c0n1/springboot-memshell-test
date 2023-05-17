package me.mole.filter;

import javax.servlet.*;
import java.io.IOException;

public class CustomFilter1 implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
        try {
            System.out.println("CustomFilter1 in...");
            filterChain.doFilter(request, response);
            System.out.println("CustomFilter1 out...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
