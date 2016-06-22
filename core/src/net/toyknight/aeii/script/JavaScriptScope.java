package net.toyknight.aeii.script;

import org.mozilla.javascript.*;

/**
 * @author toyknight 10/19/2015.
 */
public class JavaScriptScope {

    private final Context context;
    private final Scriptable scope;

    public JavaScriptScope(Context context, String script, String name) {
        this.context = context;
        this.scope = context.initStandardObjects();
        this.context.evaluateString(scope, script, name, 1, null);
    }

    public void bind(String name, Object obj) {
        Object wrappedObject = Context.javaToJS(obj, scope);
        ScriptableObject.putProperty(scope, name, wrappedObject);
    }

    public Object invokeFunction(String name, Object... args) throws ScriptException {
        Object function_object = scope.get(name, scope);
        if (function_object instanceof Function) {
            Function function = (Function) function_object;
            try {
                return function.call(context, scope, scope, args);
            } catch (EvaluatorException ex) {
                throw new ScriptException(ex.getMessage(), ex);
            }
        } else {
            throw new ScriptException("function not found: " + name);
        }
    }

}
