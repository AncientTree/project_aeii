package net.toyknight.aeii.utils;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;

/**
 * @author toyknight 8/29/2016.
 */
public class InputFilter implements TextField.TextFieldFilter {

    @Override
    public boolean acceptChar(TextField textField, char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == '_' || c == ' ';
    }

}
