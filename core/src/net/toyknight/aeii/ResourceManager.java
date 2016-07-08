package net.toyknight.aeii;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import net.toyknight.aeii.utils.FileProvider;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.utils.TileFactory;
import net.toyknight.aeii.utils.UnitFactory;

import java.util.HashMap;
import java.util.Scanner;

/**
 * @author toyknight 6/11/2016.
 */
public class ResourceManager {

    private final AssetManager asset_manager = new AssetManager();

    private Texture[] texture_tiles;
    private Texture[] texture_top_tiles;
    private Texture[] texture_small_tiles;
    private Texture texture_tomb;
    private Texture texture_alpha;
    private Texture texture_normal_cursor;
    private Texture texture_attack_cursor;
    private Texture texture_move_target_cursor;
    private TextureRegion[] icons_unit_preview;

    private TextureRegion[][][] texture_units;
    private TextureRegion[] texture_heads;
    private TextureRegion[] texture_status;
    private TextureRegion[] texture_level;

    private HashMap<String, Texture> texture_map_editor_icons;

    private Texture texture_border;

    private TextureRegion[] texture_big_circle;
    private TextureRegion[] texture_small_circle;

    private TextureRegion[] icons_arrow;
    private TextureRegion[] icons_action;
    private TextureRegion[] icons_hud_status;
    private TextureRegion[] icons_hud_battle;
    private TextureRegion[] icons_main_menu;

    private Texture texture_dust;
    private Texture texture_smoke;
    private Texture texture_spark_attack;
    private Texture texture_spark_white;
    private TextureRegion[] texture_portraits;

    private Texture texture_main_menu_background;

    private Texture bg_list_selected;
    private Texture bg_list_unselected;
    private Texture bg_panel;
    private Texture[] bg_team;
    private Texture bg_text;
    private Texture color_move_path;
    private Texture color_white;
    private Texture color_border_dark;
    private Texture color_border_light;

    private Color color_physical_attack;
    private Color color_magic_attack;

    private ShaderProgram grayscale_shader;
    private ShaderProgram color_filter_shader;

    private BitmapFont font_title;
    private BitmapFont font_text;
    private Texture texture_chars_large;
    private Texture texture_chars_small;

    public void prepare(int ts) {
        //map related textures
        texture_tiles = new Texture[TileFactory.getTileCount()];
        for (int i = 0; i < texture_tiles.length; i++) {
            asset_manager.load("images/tiles/tile_" + i + ".png", Texture.class);
        }
        texture_top_tiles = new Texture[getTopTileCount()];
        for (int i = 0; i < texture_top_tiles.length; i++) {
            asset_manager.load("images/tiles/top_tiles/top_tile_" + i + ".png", Texture.class);
        }
        texture_small_tiles = new Texture[getSmallTileCount()];
        for (int i = 0; i < texture_small_tiles.length; i++) {
            asset_manager.load("images/stiles/stiles" + i + ".png", Texture.class);
        }
        asset_manager.load("images/tombstone.png", Texture.class);
        asset_manager.load("images/alpha.png", Texture.class);
        asset_manager.load("images/cursor_normal.png", Texture.class);
        asset_manager.load("images/cursor_attack.png", Texture.class);
        asset_manager.load("images/cursor_move_target.png", Texture.class);
        asset_manager.load("images/icons_unit_preview.png", Texture.class);
        //unit textures
        texture_units = new TextureRegion[4][UnitFactory.getUnitCount()][2];
        for (int team = 0; team < 4; team++) {
            asset_manager.load("images/units/unit_sheet_" + team + ".png", Texture.class);
        }
        texture_heads = new TextureRegion[4];
        asset_manager.load("images/units/heads.png", Texture.class);
        asset_manager.load("images/status.png", Texture.class);
        asset_manager.load("images/level.png", Texture.class);
        //map editor icons
        asset_manager.load("images/editor/icon_brush.png", Texture.class);
        asset_manager.load("images/editor/icon_hand.png", Texture.class);
        asset_manager.load("images/editor/icon_eraser.png", Texture.class);
        asset_manager.load("images/editor/icon_load.png", Texture.class);
        asset_manager.load("images/editor/icon_save.png", Texture.class);
        asset_manager.load("images/editor/icon_resize.png", Texture.class);
        asset_manager.load("images/editor/icon_exit.png", Texture.class);
        //misc. textures
        asset_manager.load("images/border.png", Texture.class);
        asset_manager.load("images/circle_big.png", Texture.class);
        asset_manager.load("images/circle_small.png", Texture.class);
        asset_manager.load("images/icons_arrow.png", Texture.class);
        asset_manager.load("images/icons_action.png", Texture.class);
        asset_manager.load("images/icons_hud_status.png", Texture.class);
        asset_manager.load("images/icons_hud_battle.png", Texture.class);
        asset_manager.load("images/icons_main_menu.png", Texture.class);
        asset_manager.load("images/dust.png", Texture.class);
        asset_manager.load("images/smoke.png", Texture.class);
        asset_manager.load("images/spark_attack.png", Texture.class);
        asset_manager.load("images/spark_white.png", Texture.class);
        asset_manager.load("images/main_menu_background.png", Texture.class);
        asset_manager.load("images/portraits.png", Texture.class);

        FileHandleResolver file_resolver = new InternalFileHandleResolver();

        //fonts
        asset_manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(file_resolver));
        asset_manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(file_resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter text_param = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        text_param.fontFileName = FileProvider.getUIDefaultFont().path();
        text_param.fontParameters.size = ts / 3;
        text_param.fontParameters.color = Color.WHITE;
        text_param.fontParameters.borderColor = Color.BLACK;
        text_param.fontParameters.borderWidth = ts / 24;
        text_param.fontParameters.characters = Language.createTextCharset(FreeTypeFontGenerator.DEFAULT_CHARS);
        asset_manager.load("text.ttf", BitmapFont.class, text_param);

        FreetypeFontLoader.FreeTypeFontLoaderParameter title_param = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        title_param.fontFileName = FileProvider.getUIDefaultFont().path();
        title_param.fontParameters.size = ts / 2;
        title_param.fontParameters.color = Color.WHITE;
        title_param.fontParameters.shadowColor = Color.BLACK;
        title_param.fontParameters.shadowOffsetX = ts / 12;
        title_param.fontParameters.shadowOffsetY = ts / 12;
        title_param.fontParameters.characters = Language.createTitleCharset(FreeTypeFontGenerator.DEFAULT_CHARS);
        asset_manager.load("title.ttf", BitmapFont.class, title_param);

        asset_manager.load("images/chars_large.png", Texture.class);
        asset_manager.load("images/chars_small.png", Texture.class);
        //skin
        SkinLoader.SkinParameter skin_parameter = new SkinLoader.SkinParameter("skin/aeii_skin.atlas");
        asset_manager.load("skin/aeii_skin.atlas", TextureAtlas.class);
        asset_manager.load("skin/aeii_skin.json", Skin.class, skin_parameter);
        //shader
        grayscale_shader = new ShaderProgram(
                FileProvider.getAssetsFile("shaders/Shader.VERT").readString(),
                FileProvider.getAssetsFile("shaders/Grayscale.FRAG").readString());
        color_filter_shader = new ShaderProgram(
                FileProvider.getAssetsFile("shaders/Shader.VERT").readString(),
                FileProvider.getAssetsFile("shaders/WhiteMask.FRAG").readString());
    }

    public void initialize() {
        for (int i = 0; i < texture_tiles.length; i++) {
            texture_tiles[i] = asset_manager.get("images/tiles/tile_" + i + ".png", Texture.class);
        }
        for (int i = 0; i < texture_top_tiles.length; i++) {
            texture_top_tiles[i] = asset_manager.get("images/tiles/top_tiles/top_tile_" + i + ".png", Texture.class);
        }
        for (int i = 0; i < texture_small_tiles.length; i++) {
            texture_small_tiles[i] = asset_manager.get("images/stiles/stiles" + i + ".png", Texture.class);
        }
        texture_tomb = asset_manager.get("images/tombstone.png", Texture.class);
        texture_alpha = asset_manager.get("images/alpha.png", Texture.class);
        texture_normal_cursor = asset_manager.get("images/cursor_normal.png", Texture.class);
        texture_attack_cursor = asset_manager.get("images/cursor_attack.png", Texture.class);
        texture_move_target_cursor = asset_manager.get("images/cursor_move_target.png", Texture.class);
        Texture sheet_unit_preview = asset_manager.get("images/icons_unit_preview.png", Texture.class);
        icons_unit_preview = createFrames(sheet_unit_preview, 4, 1);

        for (int team = 0; team < 4; team++) {
            Texture sheet_units = asset_manager.get("images/units/unit_sheet_" + team + ".png", Texture.class);
            int texture_size = sheet_units.getWidth() / UnitFactory.getUnitCount();
            for (int index = 0; index < UnitFactory.getUnitCount(); index++) {
                texture_units[team][index][0] = new TextureRegion(sheet_units,
                        index * texture_size, 0, texture_size, texture_size);
                texture_units[team][index][1] = new TextureRegion(sheet_units,
                        index * texture_size, texture_size, texture_size, texture_size);
            }
        }
        Texture sheet_heads = asset_manager.get("images/units/heads.png", Texture.class);
        texture_heads = createFrames(sheet_heads, 4, 1);
        Texture sheet_status = asset_manager.get("images/status.png", Texture.class);
        texture_status = createFrames(sheet_status, 4, 1);
        Texture sheet_level = asset_manager.get("images/level.png", Texture.class);
        texture_level = createFrames(sheet_level, 3, 1);

        texture_map_editor_icons = new HashMap<String, Texture>();
        texture_map_editor_icons.put("icon_brush", asset_manager.get("images/editor/icon_brush.png", Texture.class));
        texture_map_editor_icons.put("icon_hand", asset_manager.get("images/editor/icon_hand.png", Texture.class));
        texture_map_editor_icons.put("icon_eraser", asset_manager.get("images/editor/icon_eraser.png", Texture.class));
        texture_map_editor_icons.put("icon_load", asset_manager.get("images/editor/icon_load.png", Texture.class));
        texture_map_editor_icons.put("icon_save", asset_manager.get("images/editor/icon_save.png", Texture.class));
        texture_map_editor_icons.put("icon_resize", asset_manager.get("images/editor/icon_resize.png", Texture.class));
        texture_map_editor_icons.put("icon_exit", asset_manager.get("images/editor/icon_exit.png", Texture.class));

        texture_border = asset_manager.get("images/border.png", Texture.class);
        Texture sheet_big_circle = asset_manager.get("images/circle_big.png", Texture.class);
        texture_big_circle = createFrames(sheet_big_circle, 2, 1);
        Texture sheet_small_circle = asset_manager.get("images/circle_small.png", Texture.class);
        texture_small_circle = createFrames(sheet_small_circle, 2, 1);
        Texture sheet_icons_arrow = asset_manager.get("images/icons_arrow.png", Texture.class);
        icons_arrow = createFrames(sheet_icons_arrow, 3, 1);
        Texture sheet_icons_action = asset_manager.get("images/icons_action.png", Texture.class);
        icons_action = createFrames(sheet_icons_action, 8, 1);
        Texture sheet_icons_hud_status = asset_manager.get("images/icons_hud_status.png", Texture.class);
        icons_hud_status = createFrames(sheet_icons_hud_status, 3, 1);
        Texture sheet_icons_hud_battle = asset_manager.get("images/icons_hud_battle.png", Texture.class);
        icons_hud_battle = createFrames(sheet_icons_hud_battle, 4, 1);
        Texture sheet_icons_main_menu = asset_manager.get("images/icons_main_menu.png", Texture.class);
        icons_main_menu = createFrames(sheet_icons_main_menu, 10, 1);
        texture_dust = asset_manager.get("images/dust.png", Texture.class);
        texture_smoke = asset_manager.get("images/smoke.png", Texture.class);
        texture_spark_attack = asset_manager.get("images/spark_attack.png", Texture.class);
        texture_spark_white = asset_manager.get("images/spark_white.png", Texture.class);
        Texture sheet_portraits = asset_manager.get("images/portraits.png", Texture.class);
        texture_portraits = createFrames(sheet_portraits, 6, 1);
        texture_main_menu_background = asset_manager.get("images/main_menu_background.png", Texture.class);

        bg_list_selected = new Texture(createColoredPixmap(Color.GRAY));
        bg_list_unselected = new Texture(createColoredPixmap(Color.DARK_GRAY));
        bg_panel = new Texture(createColoredPixmap(new Color(36 / 256f, 42 / 256f, 69 / 256f, 1f)));
        bg_team = new Texture[4];
        bg_team[0] = new Texture(createColoredPixmap(new Color(0f, 100 / 256f, 198 / 256f, 1f)));
        bg_team[1] = new Texture(createColoredPixmap(new Color(161 / 256f, 0f, 112 / 256f, 1f)));
        bg_team[2] = new Texture(createColoredPixmap(new Color(0f, 153 / 256f, 55 / 256f, 1f)));
        bg_team[3] = new Texture(createColoredPixmap(new Color(0f, 65 / 256f, 114 / 256f, 1f)));
        bg_text = new Texture(createColoredPixmap(new Color(192 / 256f, 192 / 256f, 192 / 256f, 0.8f)));
        color_border_dark = new Texture(createColoredPixmap(new Color(66 / 256f, 73 / 256f, 99 / 256f, 1f)));
        color_border_light = new Texture(createColoredPixmap(new Color(173 / 256f, 182 / 256f, 173 / 256f, 1f)));
        color_move_path = new Texture(createColoredPixmap(new Color(225 / 256f, 0f, 82 / 256f, 1f)));
        color_white = new Texture(createColoredPixmap(Color.WHITE));

        color_physical_attack = new Color(227 / 256f, 0, 117 / 256f, 1f);
        color_magic_attack = new Color(0, 0, 255 / 256f, 1f);

        font_title = asset_manager.get("title.ttf", BitmapFont.class);
        font_text = asset_manager.get("text.ttf", BitmapFont.class);
        texture_chars_large = asset_manager.get("images/chars_large.png", Texture.class);
        texture_chars_small = asset_manager.get("images/chars_small.png", Texture.class);
    }

    public boolean update() {
        return asset_manager.update();
    }

    public void dispose() {
        texture_tiles = null;
        texture_top_tiles = null;
        texture_small_tiles = null;
        texture_tomb = null;
        texture_alpha = null;
        texture_normal_cursor = null;
        texture_attack_cursor = null;
        texture_move_target_cursor = null;
        icons_unit_preview = null;

        texture_units = null;
        texture_heads = null;
        texture_status = null;
        texture_level = null;

        texture_map_editor_icons.clear();

        texture_border = null;

        texture_big_circle = null;
        texture_small_circle = null;

        icons_arrow = null;
        icons_action = null;
        icons_hud_status = null;
        icons_hud_battle = null;
        icons_main_menu = null;

        texture_dust = null;
        texture_smoke = null;
        texture_spark_attack = null;
        texture_spark_white = null;
        texture_portraits = null;

        texture_main_menu_background = null;

        bg_list_selected = null;
        bg_list_unselected = null;
        bg_panel = null;
        bg_team = null;
        bg_text = null;
        color_move_path = null;
        color_white = null;
        color_border_dark = null;
        color_border_light = null;

        color_physical_attack = null;
        color_magic_attack = null;

        grayscale_shader = null;
        color_filter_shader = null;

        font_title = null;
        font_text = null;
        texture_chars_large = null;
        texture_chars_small = null;
        asset_manager.dispose();
    }

    public Skin getSkin() {
        return asset_manager.get("skin/aeii_skin.json", Skin.class);
    }

    public Texture getTileTexture(int index) {
        return texture_tiles[index];
    }

    public Texture getTopTileTexture(int index) {
        return texture_top_tiles[index];
    }

    public Texture getSmallTileTexture(int index) {
        return texture_small_tiles[index];
    }

    public Texture getTombTexture() {
        return texture_tomb;
    }

    public Texture getAlphaTexture() {
        return texture_alpha;
    }

    public Texture getNormalCursorTexture() {
        return texture_normal_cursor;
    }

    public Texture getAttackCursorTexture() {
        return texture_attack_cursor;
    }

    public Texture getMoveTargetCursorTexture() {
        return texture_move_target_cursor;
    }

    public TextureRegion getUnitPreviewTexture(int team) {
        return icons_unit_preview[team];
    }

    public TextureRegion getUnitTexture(int team, int index, int frame) {
        return texture_units[team][index][frame];
    }

    public TextureRegion getHeadTexture(int index) {
        return texture_heads[index];
    }

    public TextureRegion getStatusTexture(int index) {
        return texture_status[index];
    }

    public TextureRegion getLevelTexture(int index) {
        return texture_level[index];
    }

    public Texture getBorderTexture() {
        return texture_border;
    }

    public TextureRegion getBigCircleTexture(int index) {
        return texture_big_circle[index];
    }

    public TextureRegion getSmallCircleTexture(int index) {
        return texture_small_circle[index];
    }

    public TextureRegion getArrowIcon(int index) {
        return icons_arrow[index];
    }

    public TextureRegion getActionIcon(int index) {
        return icons_action[index];
    }

    public TextureRegion getStatusHudIcon(int index) {
        return icons_hud_status[index];
    }

    public TextureRegion getBattleHudIcon(int index) {
        return icons_hud_battle[index];
    }

    public TextureRegion getMenuIcon(int index) {
        return icons_main_menu[index];
    }

    public Texture getDustTexture() {
        return texture_dust;
    }

    public Texture getSmokeTexture() {
        return texture_smoke;
    }

    public Texture getAttackSparkTexture() {
        return texture_spark_attack;
    }

    public Texture getWhiteSparkTexture() {
        return texture_spark_white;
    }

    public TextureRegion getPortraitTexture(int index) {
        return texture_portraits[index];
    }

    public Texture getMainMenuBackgroundTexture() {
        return texture_main_menu_background;
    }

    public Texture getEditorTexture(String key) {
        return texture_map_editor_icons.get(key);
    }

    public Texture getListSelectedBackground() {
        return bg_list_selected;
    }

    public Texture getListBackground() {
        return bg_list_unselected;
    }

    public Texture getPanelBackground() {
        return bg_panel;
    }

    public Texture getTeamBackground(int team) {
        return bg_team[team];
    }

    public Texture getMovePathColor() {
        return color_move_path;
    }

    public Texture getWhiteColor() {
        return color_white;
    }

    public Texture getTextBackground() {
        return bg_text;
    }

    public Texture getBorderDarkColor() {
        return color_border_dark;
    }

    public Texture getBorderLightColor() {
        return color_border_light;
    }

    public Color getPhysicalAttackColor() {
        return color_physical_attack;
    }

    public Color getMagicalAttackColor() {
        return color_magic_attack;
    }

    public ShaderProgram getGrayscaleShader(float scale) {
        grayscale_shader.begin();
        grayscale_shader.setUniformf("grayscale", scale);
        grayscale_shader.end();
        return grayscale_shader;
    }

    public ShaderProgram getWhiteMaskShader(float scale) {
        color_filter_shader.begin();
        grayscale_shader.setUniformf("grayscale", scale);
        color_filter_shader.end();
        return color_filter_shader;
    }

    public BitmapFont getTextFont() {
        return font_text;
    }

    public BitmapFont getTitleFont() {
        return font_title;
    }

    public Texture getLargeCharacterTexture() {
        return texture_chars_large;
    }

    public Texture getSmallCharacterTexture() {
        return texture_chars_small;
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

    public static TextureRegionDrawable createDrawable(Texture texture) {
        return createDrawable(texture, texture.getWidth(), texture.getHeight());
    }

    public static TextureRegionDrawable createDrawable(Texture texture, int width, int height) {
        return createDrawable(new TextureRegion(texture), width, height);
    }

    public static TextureRegionDrawable createDrawable(TextureRegion texture, int width, int height) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
        drawable.setMinWidth(width);
        drawable.setMinHeight(height);
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

    private int getTopTileCount() {
        FileHandle top_tile_config = FileProvider.getAssetsFile("images/tiles/top_tiles/config.dat");
        Scanner din = new Scanner(top_tile_config.read());
        int count = din.nextInt();
        din.close();
        return count;
    }

    private int getSmallTileCount() {
        FileHandle top_tile_config = FileProvider.getAssetsFile("images/stiles/config.dat");
        Scanner din = new Scanner(top_tile_config.read());
        int count = din.nextInt();
        din.close();
        return count;
    }

}
