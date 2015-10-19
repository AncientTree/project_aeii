package com.toyknight.aeii.script;

import com.badlogic.gdx.files.FileHandle;
import org.mozilla.javascript.Context;

/**
 * @author toyknight 10/19/2015.
 */
public class JavaScriptEngine {

    private final Context context;

    public JavaScriptEngine() {
        context = Context.enter();
    }

    public JavaScriptScope evaluate(FileHandle script_file) {
        return evaluate(script_file, script_file.name());
    }

    public JavaScriptScope evaluate(FileHandle script_file, String source_name) {
        String script = script_file.readString();
        return new JavaScriptScope(context, script, source_name);
    }

}
