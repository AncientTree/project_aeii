package com.toyknight.aeii;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.SuffixFileFilter;
import com.toyknight.aeii.utils.TileFactory;
import com.toyknight.aeii.utils.UnitFactory;

import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by toyknight on 4/2/2015.
 */
public class ResourceManager {

    private static final SuffixFileFilter png_filter = new SuffixFileFilter("png");

    private static Texture ms_logo_texture;
    private static Texture ae_logo_texture;
    private static Texture ae_logo_mask_texture;
    private static Texture ae_logo_glow_texture;

    private static Texture tomb_texture;
    private static Texture[] tile_textures;
    private static Texture[] top_tile_textures;

    private static TextureRegion[][][][] default_unit_textures;
    private static HashMap<String, TextureRegion[][][][]> unit_package_textures;

    private static Texture cursor_texture;
    private static Texture attack_cursor_texture;
    private static Texture move_target_cursor_texture;
    private static Texture border_texture;
    private static Texture alpha_texture;

    private static Texture[] action_button_textures;

    private static Texture menu_icon_texture;
    private static TextureRegion[] menu_icon_textures;

    private static TextureRegion[] dust_frames;
    private static TextureRegion[] attack_spark_frames;
    private static TextureRegion[] white_spark_frames;

    private static Texture aeii_panel_bg;
    private static Texture[] team_bg;
    private static Color move_path_color;

    private static final String GS_VERT =
            "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                    "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
                    "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

                    "uniform mat4 u_projTrans;\n" +
                    " \n" +
                    "varying vec4 vColor;\n" +
                    "varying vec2 vTexCoord;\n" +

                    "void main() {\n" +
                    "       vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
                    "       vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
                    "       gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                    "}";

    private static final String GS_FRAG =
            //GL ES specific stuff
            "#ifdef GL_ES\n" //
                    + "#define LOWP lowp\n" //
                    + "precision mediump float;\n" //
                    + "#else\n" //
                    + "#define LOWP \n" //
                    + "#endif\n" + //
                    "varying LOWP vec4 vColor;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "uniform float grayscale;\n" +
                    "void main() {\n" +
                    "       vec4 texColor = texture2D(u_texture, vTexCoord);\n" +
                    "       \n" +
                    "       float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));\n" +
                    "       texColor.rgb = mix(vec3(gray), texColor.rgb, grayscale);\n" +
                    "       \n" +
                    "       gl_FragColor = texColor * vColor;\n" +
                    "}";

    private static ShaderProgram grayscale_shader;

    private ResourceManager() {
    }

    public static void loadResources() throws AEIIException {
        try {
            ms_logo_texture = new Texture(FileProvider.getAssetsFile("images/ms_logo.png"));
            ae_logo_texture = new Texture(FileProvider.getAssetsFile("images/ae_logo.png"));
            ae_logo_mask_texture = new Texture(FileProvider.getAssetsFile("images/ae_logo_mask.png"));
            ae_logo_glow_texture = new Texture(FileProvider.getAssetsFile("images/ae_glow.png"));
            tomb_texture = new Texture(FileProvider.getAssetsFile("images/tombstone.png"));
            loadTileTextures();
            loadUnitTextures();
            cursor_texture = new Texture(FileProvider.getAssetsFile("images/cursor.png"));
            attack_cursor_texture = new Texture(FileProvider.getAssetsFile("images/attack_cursor.png"));
            move_target_cursor_texture = new Texture(FileProvider.getAssetsFile("images/move_target_cursor.png"));
            border_texture = new Texture(FileProvider.getAssetsFile("images/border.png"));
            alpha_texture = new Texture(FileProvider.getAssetsFile("images/alpha.png"));
            loadActionButtonTextures();
            menu_icon_texture = new Texture(FileProvider.getAssetsFile("images/menu_icons.png"));
            createMenuIconTextures();
            createAnimationFrames();
            createGrayscaleShader();

            aeii_panel_bg = new Texture(FileProvider.getAssetsFile("images/panel_bg.png"));
            team_bg = new Texture[4];
            for (int team = 0; team < 4; team++) {
                team_bg[team] = new Texture(FileProvider.getAssetsFile("images/team_bg_" + team + ".png"));
            }
            move_path_color = new Color(225 / 256f, 0f, 82 / 256f, 1.0f);
        } catch (GdxRuntimeException ex) {
            throw new AEIIException(ex.getMessage());
        }
    }

    private static void loadTileTextures() throws GdxRuntimeException {
        int tile_count = TileFactory.getTileCount();
        tile_textures = new Texture[tile_count];
        for (int i = 0; i < tile_count; i++) {
            FileHandle tile_image = FileProvider.getAssetsFile("images/tiles/tile_" + i + ".png");
            tile_textures[i] = new Texture(tile_image);
        }
        FileHandle top_tile_config = FileProvider.getAssetsFile("images/tiles/top_tiles/config.dat");
        Scanner din = new Scanner(top_tile_config.read());
        top_tile_textures = new Texture[din.nextInt()];
        for (int i = 0; i < top_tile_textures.length; i++) {
            FileHandle top_tile_image = FileProvider.getAssetsFile("images/tiles/top_tiles/top_tile_" + i + ".png");
            top_tile_textures[i] = new Texture(top_tile_image);
        }
        din.close();
    }

    private static void loadUnitTextures() {
        //load default units
        Texture[] unit_texture_sheets = new Texture[4];
        for (int team = 0; team < 4; team++) {
            FileHandle sheet = FileProvider.getAssetsFile("images/units/unit_sheet_" + team + ".png");
            unit_texture_sheets[team] = new Texture(sheet);
        }
        int unit_count = UnitFactory.getUnitCount("default");
        default_unit_textures = new TextureRegion[4][unit_count][4][2];
        for (int team = 0; team < 4; team++) {
            Texture unit_texture_sheet = unit_texture_sheets[team];
            int texture_size = unit_texture_sheet.getWidth() / unit_count;
            for (int index = 0; index < unit_count; index++) {
                for (int level = 0; level < 4; level++) {
                    default_unit_textures[team][index][level][0] = new TextureRegion(unit_texture_sheet, index * texture_size, level * texture_size * 2, texture_size, texture_size);
                    default_unit_textures[team][index][level][1] = new TextureRegion(unit_texture_sheet, index * texture_size, level * texture_size * 2 + texture_size, texture_size, texture_size);
                }
            }
        }

        unit_package_textures = new HashMap();
    }

    private static void loadActionButtonTextures() {
        action_button_textures = new Texture[9];
        for (int i = 0; i < 9; i++) {
            action_button_textures[i] = new Texture(FileProvider.getAssetsFile("images/buttons/action_button_" + i + ".png"));
        }
    }

    private static void createMenuIconTextures() {
        int size = menu_icon_texture.getHeight();
        menu_icon_textures = new TextureRegion[menu_icon_texture.getWidth() / size];
        for (int i = 0; i < menu_icon_textures.length; i++) {
            menu_icon_textures[i] = new TextureRegion(menu_icon_texture, i * size, 0, size, size);
        }
    }

    private static void createAnimationFrames() {
        Texture dust_texture = new Texture(FileProvider.getAssetsFile("images/dust.png"));
        dust_frames = createFrames(dust_texture, 4, 1);
        Texture attack_spark_sheet = new Texture(FileProvider.getAssetsFile("images/attack_spark.png"));
        attack_spark_frames = createFrames(attack_spark_sheet, 6, 1);
        Texture white_spark_sheet = new Texture(FileProvider.getAssetsFile("images/white_spark.png"));
        white_spark_frames = createFrames(white_spark_sheet, 6, 1);
    }

    private static void createGrayscaleShader() {
        grayscale_shader = new ShaderProgram(GS_VERT, GS_FRAG);
        grayscale_shader.begin();
        grayscale_shader.setUniformf("grayscale", 0f);
        grayscale_shader.end();
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

    public static Texture getTombTexture() {
        return tomb_texture;
    }

    public static Texture getAELogoGlowTexture() {
        return ae_logo_glow_texture;
    }

    public static Texture getTileTexture(int index) {
        return tile_textures[index];
    }

    public static Texture getTopTileTexture(int index) {
        return top_tile_textures[index];
    }

    public static TextureRegion getUnitTexture(String package_name, int team, int index, int level, int frame) {
        if (package_name.equals("default")) {
            return default_unit_textures[team][index][level][frame];
        } else {
            return unit_package_textures.get(package_name)[team][index][level][frame];
        }
    }

    public static Texture getCursorTexture() {
        return cursor_texture;
    }

    public static Texture getAttackCursorTexture() {
        return attack_cursor_texture;
    }

    public static Texture getMoveTargetCursorTexture() {
        return move_target_cursor_texture;
    }

    public static Texture getBorderTexture() {
        return border_texture;
    }

    public static Texture getAlphaTexture() {
        return alpha_texture;
    }

    public static Texture getActionButtonTexture(int index) {
        return action_button_textures[index];
    }

    public static Texture getMenuIconTexture() {
        return menu_icon_texture;
    }

    public static TextureRegion getMenuIcon(int index) {
        return menu_icon_textures[index];
    }

    public static int getMenuIconSize(int scaling) {
        return menu_icon_texture.getHeight() * scaling;
    }

    public static TextureRegion[] getAttackSparkFrames() {
        return attack_spark_frames;
    }

    public static TextureRegion[] getWhiteSparkFrames() {
        return white_spark_frames;
    }

    public static TextureRegion[] getDustFrames() {
        return dust_frames;
    }

    public static Texture getPanelBackground() {
        return aeii_panel_bg;
    }

    public static Texture getTeamBackground(int team) {
        return team_bg[team];
    }

    public static Color getMovePathColor() {
        return move_path_color;
    }

    public static ShaderProgram getGrayscaleShader() {
        return grayscale_shader;
    }

    public static int getTopTileCount() {
        return top_tile_textures.length;
    }

    public static TextureRegion[] createFrames(Texture sheet, int cols, int rows) {
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
        return frames;
    }

    public static Animation createAnimation(Texture sheet, int cols, int rows, float frame_duration) {
        TextureRegion[] frames = createFrames(sheet, cols, rows);
        return new Animation(frame_duration, frames);
    }

    public static TextureRegionDrawable createDrawable(TextureRegion texture, int preferred_width, int preferred_height) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
        drawable.setMinWidth(preferred_width);
        drawable.setMinHeight(preferred_height);
        return drawable;
    }

    public static void setBatchAlpha(Batch batch, float alpha) {
        Color color = batch.getColor();
        batch.setColor(color.r, color.g, color.b, alpha);
    }

}
