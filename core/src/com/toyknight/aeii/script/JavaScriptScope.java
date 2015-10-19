package com.toyknight.aeii.script;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

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

    public Object invokeFunction(String name, Object... args) throws NoSuchMethodException {
        Object function_object = scope.get(name, scope);
        if (function_object instanceof Function) {
            Function function = (Function) function_object;
            return function.call(context, scope, scope, args);
        } else {
            throw new NoSuchMethodException("function not found: " + name);
        }
    }

}
