package net.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.GameContext;

/**
 * @author toyknight 9/7/2015.
 */
public class MessageBoard extends AEIITable {

    private final Array<Message> messages;

    private boolean fading;

    private float alpha = 3f;

    public MessageBoard(GameContext context) {
        super(context);
        this.fading = true;
        this.messages = new Array<Message>();
    }

    public void setFading(boolean fading) {
        this.fading = fading;
    }

    public void display() {
        alpha = 3f;
    }

    public float getAlpha() {
        if (alpha > 1f) {
            return 1f;
        }
        if (alpha < 0f) {
            return 0f;
        }
        return alpha;
    }

    public void appendMessage(String username, String message) {
        Message msg = new Message(username, message);
        messages.add(msg);
        display();
    }


    public void clearMessages() {
        messages.clear();
    }

    public void update(float delta) {
        if (alpha > 0 && fading) {
            if (alpha > 1) {
                alpha -= delta;
            } else {
                alpha -= delta * 3;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        getContext().getFontRenderer().setTextAlpha(getAlpha());
        for (int i = 0; i < messages.size; i++) {
            Message message = messages.get(messages.size - i - 1);
            float font_height = getResources().getTextFont().getCapHeight();
            float draw_y = (i * font_height * 2) + font_height;
            float cap_height = fading ? font_height * 8 : getHeight() - font_height * 2;
            if (draw_y <= cap_height) {
                String username = message.getUsername();
                String content = message.getMessage();
                if (username == null) {
                    getContext().getFontRenderer().drawText(
                            batch, ">" + content, getX() + ts / 2, getY() + draw_y + font_height);
                } else {
                    getContext().getFontRenderer().drawText(
                            batch, ">" + username + ": " + content, getX() + ts / 2, getY() + draw_y + font_height);
                }
            } else {
                break;
            }
        }
        getContext().getFontRenderer().setTextAlpha(1.0f);
        super.draw(batch, parentAlpha);
    }

    private class Message {

        private final String username;
        private final String message;

        public Message(String username, String message) {
            this.username = username;
            this.message = message;
        }

        public String getUsername() {
            return username;
        }

        public String getMessage() {
            return message;
        }

    }

}
