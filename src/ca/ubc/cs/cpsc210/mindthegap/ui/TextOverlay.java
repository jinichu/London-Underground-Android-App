package ca.ubc.cs.cpsc210.mindthegap.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * Text overlay to display text at foot of map.
 */
public class TextOverlay extends Overlay {
    private static final float SCREEN_USE = 0.85f; // use SCREEN_USE % of width of screen
    private List<String> lines;
    private String text;
    private Paint paint;
    private int textHeight;

    public TextOverlay(ResourceProxy pResourceProxy, String text) {
        super(pResourceProxy);
        this.text = text;
        lines = new LinkedList<String>();
        textHeight = 0;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setARGB(192, 40, 40, 40);
        paint.setTextSize(24);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Align.LEFT);
    }

    @Override
    protected void draw(Canvas canvas, MapView view, boolean shadow) {
        if (shadow) return;

        lines = new LinkedList<String>();
        splitLines(paint, view.getWidth());

        final Projection pj = view.getProjection();
        Rect screenRect = pj.getScreenRect();
        Point screenPosition = new Point();

        screenPosition.x = (int) (screenRect.left
                + screenRect.width() * (1 - SCREEN_USE) / 2);
        screenPosition.y = screenRect.bottom - (lines.size() + 1) * textHeight;

        for(String line : lines) {
            canvas.drawText(line, screenPosition.x, screenPosition.y, paint);
            screenPosition.y += textHeight;
        }
    }

    /**
     * Split string to be displayed into multiple lines of text
     * as necessary so that text does not run off edge of canvas
     * @param paint  current paint used to draw text
     * @param width  current width of view onto which text will be drawn
     */
    private void splitLines(Paint paint, int width) {
        StringTokenizer tokenizer = new StringTokenizer(text);
        Rect bounds = new Rect();
        String line = new String();
        String next;
        String test;

        width = (int) (width * SCREEN_USE);

        paint.getTextBounds(text, 0, text.length(), bounds);
        textHeight = bounds.height();

        while (tokenizer.hasMoreTokens()) {
            next = tokenizer.nextToken();
            test = line + next;
            paint.getTextBounds(test, 0, test.length(), bounds);
            if (bounds.width() >= width) {
                lines.add(line);
                line = next + " ";
            }
            else
                line += next + " ";
        }

        lines.add(line);
    }

}
