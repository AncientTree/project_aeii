package net.toyknight.aeii.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.toyknight.aeii.utils.GraphicsUtil;

/**
 * @author toyknight 12/9/2016.
 */
public class Font {

    private final int[] SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    private final int schar_width;
    private final int schar_height;
    private final int lchar_width;
    private final int lchar_height;

    private TextureRegion[] small_chars;
    private TextureRegion[] large_chars;

    private GlyphLayout font_layout = new GlyphLayout();

    protected Font(int ts) {
        schar_width = ts * 6 / 24;
        schar_height = ts * 7 / 24;
        lchar_width = ts * 8 / 24;
        lchar_height = ts * 11 / 24;
    }

    public void initialize() {
        Texture sheet_small_chars = AER.resources.getSmallCharacterTexture();
        small_chars = GraphicsUtil.createFrames(sheet_small_chars, 12, 1);
        Texture sheet_large_chars = AER.resources.getLargeCharacterTexture();
        large_chars = GraphicsUtil.createFrames(sheet_large_chars, 13, 1);

    }

    public BitmapFont getTitleFont() {
        return AER.resources.getTitleFont();
    }

    public BitmapFont getTextFont() {
        return AER.resources.getTextFont();
    }

    public void setTitleAlpha(float alpha) {
        Color color = getTitleFont().getColor();
        getTitleFont().setColor(color.r, color.g, color.b, alpha);
    }

    public void setTitleColor(Color color) {
        float alpha = getTitleFont().getColor().a;
        getTitleFont().setColor(color.r, color.g, color.b, alpha);
    }

    public GlyphLayout getTitleLayout(String str) {
        font_layout.setText(getTitleFont(), str);
        return font_layout;
    }

    public void drawTitle(Batch batch, String str, float x, float y) {
        getTitleFont().draw(batch, str, x, y);
    }

    public void drawTitleCenter(Batch batch, String str, float target_x, float target_y, float target_width, float target_height) {
        font_layout.setText(getTitleFont(), str);
        float x = target_x + (target_width - font_layout.width) / 2;
        float y = target_y + (target_height - font_layout.height) / 2 + font_layout.height;
        drawTitle(batch, str, x, y);
    }

    public void setTextAlpha(float alpha) {
        Color color = getTextFont().getColor();
        getTextFont().setColor(color.r, color.g, color.b, alpha);
    }

    public void setTextColor(Color color) {
        float alpha = getTextFont().getColor().a;
        getTextFont().setColor(color.r, color.g, color.b, alpha);
    }

    public GlyphLayout getTextLayout(String str) {
        font_layout.setText(getTextFont(), str);
        return font_layout;
    }

    public void drawText(Batch batch, String str, float x, float y) {
        getTextFont().draw(batch, str, x, y);
    }

    public void drawTextCenter(Batch batch, String str, float target_x, float target_y, float target_width, float target_height) {
        font_layout.setText(getTextFont(), str);
        float x = target_x + (target_width - font_layout.width) / 2;
        float y = target_y + (target_height - font_layout.height) / 2 + font_layout.height;
        drawText(batch, str, x, y);
    }

    public void drawNegativeSNumber(Batch batch, int number, int x, int y) {
        batch.draw(getSMinus(), x, y, schar_width, schar_height);
        drawSNumber(batch, number, x + schar_width, y);
    }

    public void drawSNumber(Batch batch, int number, float x, float y) {
        int[] array = getIntArray(number);
        for (int i = 0; i < array.length; i++) {
            int n = array[i];
            batch.draw(small_chars[n], x + schar_width * i, y, schar_width, schar_height);
        }
        batch.flush();
    }

    public void drawTileDefenceBonus(Batch batch, int defence_bonus, int x, int y) {
        batch.draw(small_chars[11], x, y, schar_width, schar_height);
        drawSNumber(batch, defence_bonus, x + schar_width, y);
    }

    public void drawLFraction(Batch batch, int molecule, int denominator, int x, int y) {
        drawLNumber(batch, molecule, x, y);
        batch.draw(large_chars[10], x + getLNumberWidth(molecule, false), y, lchar_width, lchar_height);
        drawLNumber(batch, denominator, x + getLNumberWidth(molecule, false) + lchar_width, y);
    }

    public void drawPositiveLNumber(Batch batch, int number, int x, int y) {
        batch.draw(getLPlus(), x, y, lchar_width, lchar_height);
        drawLNumber(batch, number, x + lchar_width, y);
    }

    public void drawNegativeLNumber(Batch batch, int number, int x, int y) {
        batch.draw(getLMinus(), x, y, lchar_width, lchar_height);
        drawLNumber(batch, number, x + lchar_width, y);
    }

    public void drawLNumber(Batch batch, int number, float x, float y) {
        int[] array = getIntArray(number);
        for (int i = 0; i < array.length; i++) {
            int n = array[i];
            batch.draw(large_chars[n], x + lchar_width * i, y, lchar_width, lchar_height);
        }
        batch.flush();
    }

    public int getSNumberWidth(int number, boolean signed) {
        if (signed) {
            return schar_width * getDigitsOfInt(number) + schar_width;
        } else {
            return schar_width * getDigitsOfInt(number);
        }
    }

    public int getSCharHeight() {
        return schar_height;
    }

    public int getLFractionWidth(int molecule, int denominator) {
        return getLNumberWidth(molecule, false) + getLNumberWidth(denominator, false) + lchar_width;
    }

    public int getLNumberWidth(int number, boolean signed) {
        if (signed) {
            return lchar_width * getDigitsOfInt(number) + lchar_width;
        } else {
            return lchar_width * getDigitsOfInt(number);
        }
    }

    public int getLCharHeight() {
        return lchar_height;
    }

    public TextureRegion getSMinus() {
        return small_chars[10];
    }

    public TextureRegion getLMinus() {
        return large_chars[11];
    }

    public TextureRegion getLPlus() {
        return large_chars[12];
    }

    private int getDigitsOfInt(int n) {
        for (int i = 0; ; i++) {
            if (n <= SIZE_TABLE[i]) {
                return i + 1;
            }
        }
    }

    private int[] getIntArray(int n) {
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
