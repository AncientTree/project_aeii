package com.toyknight.aeii;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by toyknight on 4/2/2015.
 */
public class TextureManager {

    private static Texture ms_logo_texture;
    private static Texture ae_logo_texture;
    private static Texture ae_logo_mask_texture;
    private static Texture ae_logo_glow_texture;

    private TextureManager() {
    }

    public static void loadTextures() {
        ms_logo_texture = new Texture(Gdx.files.internal("assets/images/ms_logo.png"));
        ae_logo_texture = new Texture(Gdx.files.internal("assets/images/ae_logo.png"));
        ae_logo_mask_texture = new Texture(Gdx.files.internal("assets/images/ae_logo_mask.png"));
        ae_logo_glow_texture = new Texture(Gdx.files.internal("assets/images/ae_glow.png"));
    }

    public static Texture getMSLogoTexture() {
        return ms_logo_texture;
    }
    public static Texture getAELogoTexture() {
        return ae_logo_texture;
    }
    public static Texture getAELogoMaskTexture() {
        return ae_logo_mask_texture;
    }
    public static Texture getAELogoGlowTexture() {
        return ae_logo_glow_texture;
    }

    public static Animation createAnimation(Texture sheet, int cols, int rows, float frame_duration) {
        TextureRegion[][] tmp = TextureRegion.split(
                sheet,
                sheet.getWidth() / cols,
                sheet.getHeight() / rows);
        TextureRegion[]frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        return new Animation(frame_duration, frames);
    }

}
