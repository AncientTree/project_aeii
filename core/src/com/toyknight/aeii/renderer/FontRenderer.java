package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.utils.FileProvider;

/**
 * Created by toyknight on 4/2/2015.
 */
public class FontRenderer {

    private static final int[] SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    private static BitmapFont font_yahei;

    private static BitmapFont current_font;

    private static TextureRegion[] small_chars;
    private static TextureRegion[] large_chars;
    private static int schar_width;
    private static int schar_height;
    private static int lchar_width;
    private static int lchar_height;

    private FontRenderer() {
    }

    public static void loadFonts(int ts) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(FileProvider.getAssetsFile("fonts/ui_default.ttf"));
        font_yahei = new BitmapFont(
                FileProvider.getAssetsFile("skin/ms_yahei_shadow.fnt"),
                FileProvider.getAssetsFile("skin/ms_yahei_shadow.png"), false);

        current_font = font_yahei;
        Texture small_char_sheet = new Texture(FileProvider.getAssetsFile("images/small_chars.png"));
        small_chars = ResourceManager.createFrames(small_char_sheet, 12, 1);
        Texture large_char_sheet = new Texture(FileProvider.getAssetsFile("images/large_chars.png"));
        large_chars = ResourceManager.createFrames(large_char_sheet, 13, 1);
        schar_width = ts / 24 * 6;
        schar_height = ts / 24 * 7;
        lchar_width = ts / 24 * 8;
        lchar_height = ts / 24 * 11;
    }

    public static void setFont(String font) {
        if (font.equals("YaHei")) {
            current_font = font_yahei;
        }
    }

    public static void setColor(float r, float g, float b, float a) {
        current_font.setColor(r, g, b, a);
    }

    public static void drawString(Batch batch, String str, float x, float y) {
        current_font.draw(batch, str, x, y);
    }

    public static void drawNegativeSNumber(Batch batch, int number, int x, int y) {
        batch.begin();
        batch.draw(getSMinus(), x, y, schar_width, schar_height);
        batch.end();
        drawSNumber(batch, number, x + schar_width, y);
    }

    public static void drawSNumber(Batch batch, int number, int x, int y) {
        batch.begin();
        int[] array = getIntArray(number);
        for (int i = 0; i < array.length; i++) {
            int n = array[i];
            batch.draw(small_chars[n], x + schar_width * i, y, schar_width, schar_height);
        }
        batch.end();
    }

    public static void drawPositiveLNumber(Batch batch, int number, int x, int y) {
        batch.begin();
        batch.draw(getLPlus(), x, y, lchar_width, lchar_height);
        batch.end();
        drawLNumber(batch, number, x + lchar_width, y);
    }

    public static void drawNegativeLNumber(Batch batch, int number, int x, int y) {
        batch.begin();
        batch.draw(getLMinus(), x, y, lchar_width, lchar_height);
        batch.end();
        drawLNumber(batch, number, x + lchar_width, y);
    }

    public static void drawLNumber(Batch batch, int number, int x, int y) {
        batch.begin();
        int[] array = getIntArray(number);
        for (int i = 0; i < array.length; i++) {
            int n = array[i];
            batch.draw(large_chars[n], x + lchar_width * i, y, lchar_width, lchar_height);
        }
        batch.end();
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

    private static TextureRegion getSMinus() {
        return small_chars[10];
    }

    private static TextureRegion getLMinus() {
        return large_chars[11];
    }

    private static TextureRegion getLPlus() {
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
