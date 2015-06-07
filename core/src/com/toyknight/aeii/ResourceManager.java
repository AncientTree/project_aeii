package com.toyknight.aeii;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
    private static Texture[] stile_textures;

    private static TextureRegion[] mini_icon_textures;
    private static TextureRegion[][][][] default_unit_textures;
    private static HashMap<String, TextureRegion[][][][]> unit_package_textures;
    private static TextureRegion[] status_textures;

    private static Texture cursor_texture;
    private static Texture attack_cursor_texture;
    private static Texture move_target_cursor_texture;
    private static Texture border_texture;
    private static Texture alpha_texture;

    private static TextureRegion[] big_circle_texture;
    private static TextureRegion[] small_circle_texture;

    private static TextureRegion up_arrow;
    private static TextureRegion down_arrow;
    private static TextureRegion left_arrow;
    private static TextureRegion right_arrow;

    private static TextureRegion[] action_icons;
    private static TextureRegion[] hud_icons_battle;
    private static TextureRegion[] hud_icons_status;
    private static TextureRegion[] arrow_icons;

    private static Texture menu_icon_texture;
    private static TextureRegion[] menu_icon_textures;

    private static TextureRegion[] dust_frames;
    private static TextureRegion[] attack_spark_frames;
    private static TextureRegion[] white_spark_frames;

    private static Texture list_selected_bg;
    private static Texture list_unselected_bg;
    private static Texture aeii_panel_bg;
    private static Texture[] team_bg;
    private static Texture move_path_color;
    private static Texture white_color;
    private static Texture text_background;
    private static Texture border_dark_color;
    private static Texture border_light_color;
    private static Color p_attack_color;
    private static Color m_attack_color;

    private static ShaderProgram grayscale_shader;
    private static ShaderProgram color_filter_shader;

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
            loadStatusTextures();
            cursor_texture = new Texture(FileProvider.getAssetsFile("images/cursor.png"));
            attack_cursor_texture = new Texture(FileProvider.getAssetsFile("images/attack_cursor.png"));
            move_target_cursor_texture = new Texture(FileProvider.getAssetsFile("images/move_target_cursor.png"));
            border_texture = new Texture(FileProvider.getAssetsFile("images/border.png"));
            alpha_texture = new Texture(FileProvider.getAssetsFile("images/alpha.png"));
            loadCircles();
            loadArrows();
            loadIcons();
            menu_icon_texture = new Texture(FileProvider.getAssetsFile("images/menu_icons.png"));
            createMenuIconTextures();
            createAnimationFrames();
            createShaders();
            createColors();
        } catch (GdxRuntimeException ex) {
            throw new AEIIException(ex.getMessage());
        }
    }

    private static void loadTileTextures() throws GdxRuntimeException {
        int tile_count = TileFactory.getTileCount();
        tile_textures = new Texture[tile_count];
        for (int i = 0; i < tile_count; i++) {
            tile_textures[i] = new Texture(FileProvider.getAssetsFile("images/tiles/tile_" + i + ".png"));
        }
        FileHandle top_tile_config = FileProvider.getAssetsFile("images/tiles/top_tiles/config.dat");
        Scanner din = new Scanner(top_tile_config.read());
        top_tile_textures = new Texture[din.nextInt()];
        for (int i = 0; i < top_tile_textures.length; i++) {
            top_tile_textures[i] = new Texture(FileProvider.getAssetsFile("images/tiles/top_tiles/top_tile_" + i + ".png"));
        }
        din.close();
        int stile_count = FileProvider.getAssetsFile("images/stiles").list().length;
        stile_textures = new Texture[stile_count];
        for (int i = 0; i < stile_count; i++) {
            stile_textures[i] = new Texture(FileProvider.getAssetsFile("images/stiles/stiles" + i + ".png"));
        }
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
        Texture mini_icon_sheet = new Texture(FileProvider.getAssetsFile("images/mini_icons.png"));
        mini_icon_textures = createFrames(mini_icon_sheet, 4, 1);
        unit_package_textures = new HashMap();
    }

    private static void loadStatusTextures() {
        Texture status_sheet = new Texture(FileProvider.getAssetsFile("images/status.png"));
        status_textures = createFrames(status_sheet, 2, 1);
    }

    private static void loadCircles() {
        Texture big_circle_sheet = new Texture(FileProvider.getAssetsFile("images/big_circle.png"));
        big_circle_texture = createFrames(big_circle_sheet, 2, 1);
        Texture small_circle_sheet = new Texture(FileProvider.getAssetsFile("images/small_circle.png"));
        small_circle_texture = createFrames(small_circle_sheet, 2, 1);
    }

    private static void loadArrows() {
        TextureRegion[] up_down_arrows = createFrames(new Texture(FileProvider.getAssetsFile("images/arrow_updown.png")), 2, 1);
        up_arrow = up_down_arrows[0];
        down_arrow = up_down_arrows[1];
        TextureRegion[] left_right_arrows = createFrames(new Texture(FileProvider.getAssetsFile("images/arrow_leftright.png")), 1, 2);
        right_arrow = left_right_arrows[0];
        left_arrow = left_right_arrows[1];
    }

    private static void loadIcons() {
        Texture action_icon_sheet = new Texture(FileProvider.getAssetsFile("images/action_icons.png"));
        action_icons = createFrames(action_icon_sheet, 8, 1);
        Texture hud_icon_status_sheet = new Texture(FileProvider.getAssetsFile("images/hud_icons_status.png"));
        hud_icons_status = createFrames(hud_icon_status_sheet, 2, 1);
        Texture hud_icon_battle_sheet = new Texture(FileProvider.getAssetsFile("images/hud_icons_battle.png"));
        hud_icons_battle = createFrames(hud_icon_battle_sheet, 4, 1);
        Texture arrow_icon_sheet = new Texture(FileProvider.getAssetsFile("images/arrow_icons.png"));
        arrow_icons = createFrames(arrow_icon_sheet, 3, 1);
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

    private static void createColors() {
        list_selected_bg = new Texture(createColoredPixmap(Color.GRAY));
        list_unselected_bg = new Texture(createColoredPixmap(Color.DARK_GRAY));
        aeii_panel_bg = new Texture(createColoredPixmap(new Color(36 / 256f, 42 / 256f, 69 / 256f, 1f)));
        team_bg = new Texture[4];
        team_bg[0] = new Texture(createColoredPixmap(new Color(0f, 100 / 256f, 198 / 256f, 1f)));
        team_bg[1] = new Texture(createColoredPixmap(new Color(161 / 256f, 0f, 112 / 256f, 1f)));
        team_bg[2] = new Texture(createColoredPixmap(new Color(0f, 153 / 256f, 55 / 256f, 1f)));
        team_bg[3] = new Texture(createColoredPixmap(new Color(0f, 65 / 256f, 114 / 256f, 1f)));
        move_path_color = new Texture(createColoredPixmap(new Color(225 / 256f, 0f, 82 / 256f, 1f)));
        white_color = new Texture(createColoredPixmap(Color.WHITE));
        text_background = new Texture(createColoredPixmap(new Color(206 / 256f, 206 / 256f, 206 / 256f, 1f)));
        border_dark_color = new Texture(createColoredPixmap(new Color(66 / 256f, 73 / 256f, 99 / 256f, 1f)));
        border_light_color = new Texture(createColoredPixmap(new Color(173 / 256f, 182 / 256f, 173 / 256f, 1f)));
        p_attack_color = new Color(227 / 256f, 0, 117 / 256f, 1f);
        m_attack_color = new Color(0, 0, 255 / 256f, 1f);
    }

    private static void createShaders() {
        grayscale_shader = new ShaderProgram(
                FileProvider.getAssetsFile("shaders/Shader.VERT").readString(),
                FileProvider.getAssetsFile("shaders/Grayscale.FRAG").readString());
        color_filter_shader = new ShaderProgram(
                FileProvider.getAssetsFile("shaders/Shader.VERT").readString(),
                FileProvider.getAssetsFile("shaders/WhiteMask.FRAG").readString());
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

    public static Texture getSTileTexture(int index) {
        return stile_textures[index];
    }

    public static TextureRegion getMiniIcon(int team) {
        return mini_icon_textures[team];
    }

    public static TextureRegion getUnitTexture(String package_name, int team, int index, int level, int frame) {
        if (package_name.equals("default")) {
            return default_unit_textures[team][index][level][frame];
        } else {
            return unit_package_textures.get(package_name)[team][index][level][frame];
        }
    }

    public static TextureRegion getStatusTexture(int index) {
        return status_textures[index];
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

    public static TextureRegion getBigCircleTexture(int index) {
        return big_circle_texture[index];
    }

    public static TextureRegion getSmallCircleTexture(int index) {
        return small_circle_texture[index];
    }

    public static TextureRegion getUpArrow() {
        return up_arrow;
    }

    public static TextureRegion getDownArrow() {
        return down_arrow;
    }

    public static TextureRegion getLeftArrow() {
        return left_arrow;
    }

    public static TextureRegion getRightArrow() {
        return right_arrow;
    }

    public static TextureRegion getActionIcon(int index) {
        return action_icons[index];
    }

    public static TextureRegion getStatusHudIcon(int index) {
        return hud_icons_status[index];
    }

    public static TextureRegion getBattleHudIcon(int index) {
        return hud_icons_battle[index];
    }

    public static TextureRegion getArrowIcon(int index) {
        return arrow_icons[index];
    }

    public static Texture getMenuIconTexture() {
        return menu_icon_texture;
    }

    public static TextureRegion getMenuIcon(int index) {
        return menu_icon_textures[index];
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

    public static Texture getListSelectedBackground() {
        return list_selected_bg;
    }

    public static Texture getListBackground() {
        return list_unselected_bg;
    }

    public static Texture getPanelBackground() {
        return aeii_panel_bg;
    }

    public static Texture getTeamBackground(int team) {
        return team_bg[team];
    }

    public static Texture getMovePathColor() {
        return move_path_color;
    }

    public static Texture getWhiteColor() {
        return white_color;
    }

    public static Texture getTextBackground() {
        return text_background;
    }

    public static Texture getBorderDarkColor() {
        return border_dark_color;
    }

    public static Texture getBorderLightColor() {
        return border_light_color;
    }

    public static Color getPhysicalAttackColor() {
        return p_attack_color;
    }

    public static Color getMagicalAttackColor() {
        return m_attack_color;
    }

    public static ShaderProgram getGrayscaleShader(float scale) {
        grayscale_shader.begin();
        grayscale_shader.setUniformf("grayscale", scale);
        grayscale_shader.end();
        return grayscale_shader;
    }

    public static ShaderProgram getWhiteMaskShader(float scale) {
        color_filter_shader.begin();
        grayscale_shader.setUniformf("grayscale", scale);
        color_filter_shader.end();
        return color_filter_shader;
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

    public static Pixmap createColoredPixmap(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        return pixmap;
    }

}
