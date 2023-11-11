package me.mole.controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import me.mole.entity.GroovyVO;
import me.mole.util.SafetyGroovyShell;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.*;

@RestController
@RequestMapping("/groovy")
public class GroovyTest {

    private static final int CORE_THREAD_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int SCRIPT_EXECUTE_MAX_TIME = 3;

    private static final ThreadPoolExecutor RULE_ENGINE_THREAD_POOL = new
            ThreadPoolExecutor(CORE_THREAD_SIZE, CORE_THREAD_SIZE, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(500),
            new ThreadFactoryBuilder().setNameFormat("rule-engine-thread-%d").build(),
            new ThreadPoolExecutor.AbortPolicy());

    @GetMapping("/ping")
    public String ping() {
        return "192.168.3.118";
    }

    @PostMapping("/test1")
    public String groovytest1(@RequestBody GroovyVO groovyVO) {
        String scriptStr = groovyVO.getScript();
        GroovyShell groovyShell = new GroovyShell();
        Script scriptShell = groovyShell.parse(scriptStr);
        InvokerHelper.createScript(scriptShell.getClass(), getBinding(null, null)).run();
        return "ok";
    }


    @PostMapping("/test2")
    public String groovytest2(@RequestBody GroovyVO groovyVO) {
        String scriptStr = groovyVO.getScript();
        executeScriptForBean(null, null, scriptStr);
        return "ok";
    }

    @PostMapping("/test3")
    public String groovytest3(@RequestBody GroovyVO groovyVO) {
        String scriptStr = groovyVO.getScript();
        executeScriptShell(null, null, scriptStr);
        return "ok";
    }

    private Map<String, Object> executeScriptForBean(Map<String, Object> sourceData,
                                                     Map<String, Object> outputData, String script) {
        Future<Map<String, Object>> submit = RULE_ENGINE_THREAD_POOL.submit(
                () -> executeScriptShell(sourceData, outputData, script));
        try {
            return submit.get(SCRIPT_EXECUTE_MAX_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, Object> executeScriptShell(Map<String, Object> sourceData,
                                                   Map<String, Object> outputData,
                                                   String script) {
        try {
            Script scriptShell = getShellWithCache(script);
            InvokerHelper.createScript(scriptShell.getClass(), getBinding(sourceData, outputData)).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputData;
    }

    private Script getShellWithCache(String script) {
        return SafetyGroovyShell.newInstance().parse(script);
    }

    private Binding getBinding(Map<String, Object> sourceData,
                               Map<String, Object> outputData) {
        Binding binding = new Binding();
        binding.setVariable("input", sourceData);
        binding.setVariable("output", outputData);
        return binding;
    }
}