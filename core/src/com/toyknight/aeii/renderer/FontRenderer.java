package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 4/2/2015.
 */
public class FontRenderer {

    private static final int[] SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    private static GlyphLayout font_layout = new GlyphLayout();

    private static BitmapFont ui_font_title;
    private static BitmapFont ui_font_text;

    private static TextureRegion[] small_chars;
    private static TextureRegion[] large_chars;
    private static int schar_width;
    private static int schar_height;
    private static int lchar_width;
    private static int lchar_height;

    private FontRenderer() {
    }

    public static void loadFonts(int ts) {
        String charset = Language.createCharset(FreeTypeFontGenerator.DEFAULT_CHARS);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(FileProvider.getUIDefaultFont());

        FreeTypeFontParameter title_parameter = new FreeTypeFontParameter();
        title_parameter.size = ts / 2;
        title_parameter.color = Color.WHITE;
        title_parameter.shadowColor = Color.DARK_GRAY;
        title_parameter.shadowOffsetX = ts / 24;
        title_parameter.shadowOffsetY = ts / 24;
        title_parameter.characters = charset;
        ui_font_title = generator.generateFont(title_parameter);

        FreeTypeFontParameter text_parameter = new FreeTypeFontParameter();
        text_parameter.size = ts / 3;
        text_parameter.color = Color.WHITE;
        text_parameter.borderColor = Color.BLACK;
        text_parameter.borderWidth = ts / 24;
        text_parameter.characters = charset;
        ui_font_text = generator.generateFont(text_parameter);
        generator.dispose();

        Texture small_char_sheet = new Texture(FileProvider.getAssetsFile("images/small_chars.png"));
        small_chars = ResourceManager.createFrames(small_char_sheet, 12, 1);
        Texture large_char_sheet = new Texture(FileProvider.getAssetsFile("images/large_chars.png"));
        large_chars = ResourceManager.createFrames(large_char_sheet, 13, 1);
        schar_width = ts * 6 / 24;
        schar_height = ts * 7 / 24;
        lchar_width = ts * 8 / 24;
        lchar_height = ts * 11 / 24;
    }

    public static BitmapFont getTitleFont() {
        return ui_font_title;
    }

    public static void setTitleAlpha(float alpha) {
        Color color = ui_font_title.getColor();
        ui_font_title.setColor(color.r, color.g, color.b, alpha);
    }

    public static void setTitleColor(Color color) {
        float alpha = ui_font_title.getColor().a;
        ui_font_title.setColor(color.r, color.g, color.b, alpha);
    }

    public static GlyphLayout getTitleLayout(String str) {
        font_layout.setText(ui_font_title, str);
        return font_layout;
    }

    public static void drawTitle(Batch batch, String str, float x, float y) {
        ui_font_title.draw(batch, str, x, y);
    }

    public static void drawTitleCenter(Batch batch, String str, float target_x, float target_y, float target_width, float target_height) {
        font_layout.setText(ui_font_title, str);
        float x = target_x + (target_width - font_layout.width) / 2;
        float y = target_y + (target_height - font_layout.height) / 2 + font_layout.height;
        drawTitle(batch, str, x, y);
    }

    public static BitmapFont getTextFont() {
        return ui_font_text;
    }

    public static void setTextAlpha(float alpha) {
        Color color = ui_font_text.getColor();
        ui_font_text.setColor(color.r, color.g, color.b, alpha);
    }

    public static void setTextColor(Color color) {
        float alpha = ui_font_text.getColor().a;
        ui_font_text.setColor(color.r, color.g, color.b, alpha);
    }

    public static GlyphLayout getTextLayout(String str) {
        font_layout.setText(ui_font_text, str);
        return font_layout;
    }

    public static void drawText(Batch batch, String str, float x, float y) {
        ui_font_text.draw(batch, str, x, y);
    }

    public static void drawTextCenter(Batch batch, String str, float target_x, float target_y, float target_width, float target_height) {
        font_layout.setText(ui_font_text, str);
        float x = target_x + (target_width - font_layout.width) / 2;
        float y = target_y + (target_height - font_layout.height) / 2 + font_layout.height;
        drawText(batch, str, x, y);
    }

    public static void drawNegativeSNumber(Batch batch, int number, int x, int y) {
        batch.draw(getSMinus(), x, y, schar_width, schar_height);
        drawSNumber(batch, number, x + schar_width, y);
    }

    public static void drawSNumber(Batch batch, int number, float x, float y) {
        int[] array = getIntArray(number);
        for (int i = 0; i < array.length; i++) {
            int n = array[i];
            batch.draw(small_chars[n], x + schar_width * i, y, schar_width, schar_height);
        }
        batch.flush();
    }

    public static void drawTileDefenceBonus(Batch batch, int defence_bonus, int x, int y) {
        batch.draw(small_chars[11], x, y, schar_width, schar_height);
        drawSNumber(batch, defence_bonus, x + schar_width, y);
    }

    public static void drawLFraction(Batch batch, int molecule, int denominator, int x, int y) {
        drawLNumber(batch, molecule, x, y);
        batch.draw(large_chars[10], x + getLNumberWidth(molecule, false), y, lchar_width, lchar_height);
        drawLNumber(batch, denominator, x + getLNumberWidth(molecule, false) + lchar_width, y);
    }

    public static void drawPositiveLNumber(Batch batch, int number, int x, int y) {
        batch.draw(getLPlus(), x, y, lchar_width, lchar_height);
        drawLNumber(batch, number, x + lchar_width, y);
    }

    public static void drawNegativeLNumber(Batch batch, int number, int x, int y) {
        batch.draw(getLMinus(), x, y, lchar_width, lchar_height);
        drawLNumber(batch, number, x + lchar_width, y);
    }

    public static void drawLNumber(Batch batch, int number, float x, float y) {
        int[] array = getIntArray(number);
        for (int i = 0; i < array.length; i++) {
            int n = array[i];
            batch.draw(large_chars[n], x + lchar_width * i, y, lchar_width, lchar_height);
        }
        batch.flush();
    }

    public static int getSNumberWidth(int number, boolean signed) {
        if (signed) {
            return schar_width * getDigitsOfInt(number) + schar_width;
        } else {
            return schar_width * getDigitsOfInt(number);
        }
    }

    public static int getSCharHeight() {
        return schar_height;
    }

    public static int getLFractionWidth(int molecule, int denominator) {
        return getLNumberWidth(molecule, false) + getLNumberWidth(denominator, false) + lchar_width;
    }

    public static int getLNumberWidth(int number, boolean signed) {
        if (signed) {
            return lchar_width * getDigitsOfInt(number) + lchar_width;
        } else {
            return lchar_width * getDigitsOfInt(number);
        }
    }

    public static int getLCharHeight() {
        return lchar_height;
    }

    public static TextureRegion getSMinus() {
        return small_chars[10];
    }

    public static TextureRegion getLMinus() {
        return large_chars[11];
    }

    public static TextureRegion getLPlus() {
        return large_chars[12];
    }

    private static int getDigitsOfInt(int n) {
        for (int i = 0; ; i++) {
            if (n <= SIZE_TABLE[i]) {
                return i + 1;
            }
        }
    }

    private static int[] getIntArray(int n) {
        int length = getDigitsOfInt(n);
        int[] array = new int[length];
        int index = length - 1;
        if (n == 0) {
            array[0] = 0;
        } else {
            while (n > 0) {
                array[index] = n % 10;
                n = n / 10;
                index--;
            }
        }
        return array;
    }

}
