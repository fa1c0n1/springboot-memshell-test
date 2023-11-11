package me.mole.util;

import com.google.common.collect.ImmutableMap;
import groovy.lang.GroovyShell;
import groovy.transform.ThreadInterrupt;
import groovy.transform.TimedInterrupt;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SafetyGroovyShell {

    private static final Map<String, Object> TIME_OUT_ARGS = ImmutableMap.of("value", 1);
    private static final GroovyShell GROOVY_SHELL = new GroovyShell(getCompileConfigurations());

    private static final String JAVA_LANG_RUNTIME = "java.lang.Runtime";
    private static final String JAVA_LANG_SYSTEM = "java.lang.System";

    private SafetyGroovyShell() {
    }

    public static GroovyShell newInstance() {
        return GROOVY_SHELL;
    }

    private static CompilerConfiguration getCompileConfigurations() {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        final SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setClosuresAllowed(true);
        List<Integer> tokensBlacklist = new ArrayList<>();
        tokensBlacklist.add(Types.KEYWORD_WHILE);
        tokensBlacklist.add(Types.KEYWORD_GOTO);
        secure.setTokensBlacklist(tokensBlacklist);
        secure.setIndirectImportCheckEnabled(true);
        List<Class<? extends Statement>> statementBlacklist = new ArrayList<>();
        statementBlacklist.add(WhileStatement.class);
        secure.setStatementsBlacklist(statementBlacklist);
        List<String> receiversBlackList = new ArrayList<>();
        receiversBlackList.add(JAVA_LANG_SYSTEM);
        receiversBlackList.add(JAVA_LANG_RUNTIME);
        secure.setReceiversBlackList(receiversBlackList);
        compilerConfiguration.addCompilationCustomizers(secure);
        compilerConfiguration.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt.class));
        compilerConfiguration.addCompilationCustomizers(new ASTTransformationCustomizer(TIME_OUT_ARGS, TimedInterrupt.class));
        return compilerConfiguration;
    }
}
