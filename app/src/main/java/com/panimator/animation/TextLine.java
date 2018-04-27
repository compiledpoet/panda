package com.panimator.animation;

import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Size;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASANDA on 2018/01/18.
 * for Pandaphic
 */

public class TextLine {

    private final String text;
    private final Size size;

    public TextLine(String pText, Size pSize){
        this.text = pText;
        this.size = pSize;
    }

    public Size getSize(){
        return this.size;
    }

    public String getText(){
        return this.text;
    }

    public static TextLine[] getLines(String text, Paint drawingPaint, int boundsWidth, int padding){
        List<TextLine> lines = new ArrayList<>();
        String[] words = text.split(" ");
        String currentLine = " ";
        int currentLineSize = 0;

        for(int pos = 0; pos < words.length; pos++){
            String word = words[pos] + " ";
            Rect bounds = new Rect();
            drawingPaint.getTextBounds(word, 0, word.length(), bounds);
            int nextSize = currentLineSize + bounds.width();
            if(nextSize >= boundsWidth - 2*padding){
                lines.add(new TextLine(currentLine, new Size(currentLineSize, (int)drawingPaint.getTextSize())));
                currentLineSize = bounds.width();
                currentLine = " " + word;
            }else{
                currentLine +=  word;
                currentLineSize = nextSize;
            }
        }
        lines.add(new TextLine(currentLine, new Size(currentLineSize, (int)drawingPaint.getTextSize())));
        return lines.toArray(new TextLine[lines.size()]);
    }


}
