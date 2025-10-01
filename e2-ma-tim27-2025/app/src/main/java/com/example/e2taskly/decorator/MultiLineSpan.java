package com.example.e2taskly.decorator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;
import androidx.annotation.NonNull;
import java.util.List;

public class MultiLineSpan implements LineBackgroundSpan {

    private final float padding;
    private final float lineHeight;
    private final float lineSpacing;
    private final List<Integer> colors;

    /**
     * @param colors Lista boja (kao integer) koje treba iscrtati
     * @param padding Procenat širine bloka za bočni padding (npr. 0.2f za 20%)
     * @param lineHeight Debljina jedne linije u pikselima
     * @param lineSpacing Razmak između linija u pikselima
     */
    public MultiLineSpan(List<Integer> colors, float padding, float lineHeight, float lineSpacing) {
        this.colors = colors;
        this.padding = padding;
        this.lineHeight = lineHeight;
        this.lineSpacing = lineSpacing;
    }

    @Override
    public void drawBackground(
            @NonNull Canvas canvas, @NonNull Paint paint,
            int left, int right, int top, int baseline, int bottom,
            @NonNull CharSequence text, int start, int end, int lnum
    ) {
        int oldColor = paint.getColor();

        float sidePadding = (right - left) * padding;
        float currentBottom = bottom; // Počinjemo od samog dna ćelije

        // Petlja koja prolazi kroz sve boje i iscrtava ih jednu iznad druge
        for (Integer color : colors) {
            paint.setColor(color);
            canvas.drawRect(
                    left + sidePadding,
                    currentBottom - lineHeight, // gornja ivica linije
                    right - sidePadding,
                    currentBottom,              // donja ivica linije
                    paint
            );

            // Pomeramo "dno" za sledeću liniju nagore
            currentBottom -= (lineHeight + lineSpacing);
        }

        paint.setColor(oldColor);
    }
}