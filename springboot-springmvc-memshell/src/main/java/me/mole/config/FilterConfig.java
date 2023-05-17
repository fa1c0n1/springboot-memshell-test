package me.mole.config;

import me.mole.filter.CustomFilter1;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<CustomFilter1> customFilterRegistrationBean() {
        FilterRegistrationBean<CustomFilter1> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CustomFilter1());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // 设置过滤器的优先级
        return registrationBean;
    }
}
