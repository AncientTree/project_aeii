package com.toyknight.aeii;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.Platform;
import com.toyknight.aeii.utils.SuffixFileFilter;
import com.toyknight.aeii.utils.TileFactory;

import java.util.Scanner;

/**
 * Created by toyknight on 4/2/2015.
 */
public class TextureManager {

    private static final SuffixFileFilter png_filter = new SuffixFileFilter("png");

    private static Texture ms_logo_texture;
    private static Texture ae_logo_texture;
    private static Texture ae_logo_mask_texture;
    private static Texture ae_logo_glow_texture;

    private static Texture[] tile_texture;
    private static Texture[] top_tile_texture;

    private TextureManager() {
    }

    public static void loadTextures(Platform platform) throws AEIIException {
        try {
            ms_logo_texture = new Texture(FileProvider.getAssetsFile("images/ms_logo.png", platform));
            ae_logo_texture = new Texture(FileProvider.getAssetsFile("images/ae_logo.png", platform));
            ae_logo_mask_texture = new Texture(FileProvider.getAssetsFile("images/ae_logo_mask.png", platform));
            ae_logo_glow_texture = new Texture(FileProvider.getAssetsFile("images/ae_glow.png", platform));
            loadTileTextures(platform);
        } catch (GdxRuntimeException ex) {
            throw new AEIIException(ex.getMessage());
        }
    }

    private static void loadTileTextures(Platform platform) throws GdxRuntimeException {
        int tile_count = TileFactory.getTileCount();
        tile_texture = new Texture[tile_count];
        for (int i = 0; i < tile_count; i++) {
            FileHandle tile_image = FileProvider.getAssetsFile("images/tiles/tile_" + i + ".png", platform);
            tile_texture[i] = new Texture(tile_image);
        }
        FileHandle top_tile_config = FileProvider.getAssetsFile("images/tiles/top_tiles/config.dat", platform);
        Scanner din = new Scanner(top_tile_config.read());
        top_tile_texture = new Texture[din.nextInt()];
        System.out.println(top_tile_texture.length);
        for (int i = 0; i < top_tile_texture.length; i++) {
            FileHandle top_tile_image = FileProvider.getAssetsFile("images/tiles/top_tiles/top_tile_" + i + ".png", platform);
            top_tile_texture[i] = new Texture(top_tile_image);
        }
        din.close();
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

    public static Texture getTileTexture(int index) {
        return tile_texture[index];
    }

    public static Texture getTopTileTexture(int index) {
        return top_tile_texture[index];
    }

    public static int getTopTileCount() {
        return top_tile_texture.length;
    }

    public static Animation createAnimation(Texture sheet, int cols, int rows, float frame_duration) {
        TextureRegion[][] tmp = TextureRegion.split(
                sheet,
                sheet.getWidth() / cols,
                sheet.getHeight() / rows);
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        return new Animation(frame_duration, frames);
    }

}
