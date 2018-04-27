package com.panimator.animators.kinetictypography;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

import com.panimator.animation.AndroidAnimationSession;
import com.panimator.animation.AndroidAnimator;
import com.panimator.codeBlue.display.Plane;
import com.panimator.codeBlue.rendering.RenderingSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASANDA on 2017/12/21.
 */

public class KineticTypography extends AndroidAnimator<Bitmap> {
    public static final int END_PADDING_FRAMES = 2;
    private static final String TAG = "KineticTypography";
    private static final int HALF_MATRIX_COUNT;
    private static final double SPACING_RATIO = 3;
    private static final double ROOM_SPACING = 23.04;
    private Paint drawingPaint;
    private short HALF_ROW_SIZE;

    static {
        HALF_MATRIX_COUNT = (int)(DrawingMatrix.MATRIX_COUNT / 2.00);
    }


    @Override
    protected void onPrepare(RenderingSession<Bitmap> renderingSession) {
        drawingPaint = new Paint();
        drawingPaint.setColor(Color.WHITE);
        drawingPaint.setStyle(Paint.Style.FILL);
        HALF_ROW_SIZE = (short)((double)renderingSession.getBundle().pull(AndroidAnimationSession.SESSION_KEY_CANVAS).getWidth() / ROOM_SPACING);
        drawingPaint.setTextSize(HALF_ROW_SIZE);
        drawingPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void onRender(RenderingSession<Bitmap> renderingSession) {

        String[] words =  renderingSession.getBundle().pull(TextCollectorPlane.SESSION_KEY_TEXT);
        drawingPaint.setTypeface(renderingSession.getBundle().pull(TextCollectorPlane.SESSION_KEY_FONT));
        int[] colors = renderingSession.getBundle().pull(TextCollectorPlane.SESSION_KEY_COLORS);


        TextTile[] textTiles = createTextTiles(words);

        ArrayList<TileColumn> Columns = new ArrayList<>();
        for(int pos = 0; pos < textTiles.length; pos += 3){
            TextTile startTile = textTiles[pos];
            if(pos + 1 < textTiles.length && pos + 2 < textTiles.length){
                TextTile middleTile = textTiles[pos + 1], endTile = textTiles[pos + 2];
                Columns.add(getColumnForTriple(startTile, middleTile, endTile));
            }else if(pos + 1 < textTiles.length){
                TextTile middleTile = textTiles[pos + 1];
                Columns.add(getColumnForDouble(startTile, middleTile));
            }else{
                Columns.add(getColumnForSingle(startTile));
            }
        }
        //draw Columns
        Bitmap canvas = renderingSession.getBundle().pull(AndroidAnimationSession.SESSION_KEY_CANVAS);
        Canvas drawingCanvas = new Canvas(canvas);
        DrawingMatrixBundle[] drawingMatrixBundles = getDrawingBundles(Columns, drawingCanvas, 20);

        int columnindex = 0;
        for(DrawingMatrixBundle drawingMatrixBundle : drawingMatrixBundles){
            refreshCanvas(drawingCanvas, Color.parseColor("#212121"));
            Rect Bounds = drawingMatrixBundle.getBounds();
            int marginTop = Bounds.top, marginStart = Bounds.left;
            Shader gradient = new LinearGradient(marginStart, marginTop, Bounds.right, Bounds.bottom, colors, null, Shader.TileMode.MIRROR);
            drawingPaint.setShader(gradient);

            int prevTop = marginTop;
            for(int pos  = 0; pos < drawingMatrixBundle.getDrawingMatrices().length; pos++){
                TileColumn currentColumn = Columns.get(columnindex);
                columnindex++;
                DrawingMatrix drawingMatrix = drawingMatrixBundle.getDrawingMatrices()[pos];

                int Top = prevTop,
                        center = Top + drawingMatrix.getFirstRowHeight(),
                        verticalHalfSpacing = 0, horizontalHalfSpacing = 0;

                for(int index = 0; index < currentColumn.renderableBlocks.length; index++){
                    RenderableBlock renderableBlock = currentColumn.getRenderableBlocks()[index];
                    short startIndex = (short)renderableBlock.getColumnBlockIndex();
                    Matrix matrix = drawingMatrix.get(startIndex),
                            lastMatrix = drawingMatrix.get(startIndex + (renderableBlock.getWidthSpan() - 1));
                    int positionX =  marginStart + drawingMatrix.getMatrixStart((short)renderableBlock.getColumnBlockIndex()),
                            positionY = center + ((adjustHeightForIndex(startIndex) == 1)? matrix.getHeight() + verticalHalfSpacing : -verticalHalfSpacing);
                    if(renderableBlock.getHeightSpan() > 1){
                        int belowMatrixIndex = HALF_MATRIX_COUNT + renderableBlock.getColumnBlockIndex();
                        Matrix belowMatrix = drawingMatrix.get(belowMatrixIndex),
                                lastBelowMatrix = drawingMatrix.get(belowMatrixIndex + (renderableBlock.getWidthSpan() - 1));
                        lastBelowMatrix.setWidth((short)(lastBelowMatrix.getWidth() + (horizontalHalfSpacing * 2)));
                        drawingPaint.setTextSize((int)((matrix.getHeight() + belowMatrix.getHeight()) - SPACING_RATIO ));
                        positionY += drawingMatrix.getSecondRowHeight();
                    }else{
                        drawingPaint.setTextSize((int)(matrix.getHeight() - SPACING_RATIO));
                    }
                    lastMatrix.setWidth((short)(lastMatrix.getWidth() + (horizontalHalfSpacing * 2)));
                    drawingCanvas.drawText(renderableBlock.getTextTile().text, positionX, positionY, drawingPaint);
                    renderingSession.returnFrame(canvas);
                }

                prevTop += (drawingMatrix.getFirstRowHeight() + drawingMatrix.getSecondRowHeight()) + (verticalHalfSpacing * 2);
                //  Log.i(TAG, currentColumn.toString() + "|" + pos);
            }
        }

        for(int pos = 0; pos < END_PADDING_FRAMES; pos++){
            renderingSession.returnFrame(canvas);
        }
    }

    @Override
    public Class<? extends Plane> getMainInputCollector() {
        return TextCollectorPlane.class;
    }

    @Override
    public String getTitle() {
        return "Kinetic Typography";
    }



    private DrawingMatrixBundle[] getDrawingBundles(ArrayList<TileColumn> columns, Canvas drawingCanvas, int padding) {
        double largestWidth = 0,
                largestHeight = 0;
        ArrayList<DrawingMatrixBundle> drawingMatrixBundles = new ArrayList<>();
        ArrayList<DrawingMatrix> drawingMatrices = new ArrayList<>();
        for(int pos = 0; pos < columns.size(); pos++){
            TileColumn currentColumn = columns.get(pos);
            DrawingMatrix drawingMatrix = currentColumn.CalculateDrawingMatrices(drawingPaint, HALF_ROW_SIZE);

            int width = drawingMatrix.getWidth(),
                    height = drawingMatrix.getHeight();
            if(width > largestWidth){ largestWidth = width; }
            if(largestHeight + height  > drawingCanvas.getHeight() - (2 * padding)){

                int canvasCenterWidth = (int)(drawingCanvas.getWidth() / 2.00),
                        canvasCenterHeight = (int)(drawingCanvas.getHeight() / 2.00),

                        boundsCenterWidth = (int)(largestWidth / 2.00),
                        boundsCenterHeight = (int)(largestHeight / 2.00);
                int x = canvasCenterWidth - boundsCenterWidth,
                        y = canvasCenterHeight - boundsCenterHeight;

               DrawingMatrixBundle drawingMatrixBundle = new DrawingMatrixBundle(drawingMatrices.toArray(new DrawingMatrix[drawingMatrices.size()]), new Rect(x, y, x + (int)largestWidth, y + (int)largestHeight));
                drawingMatrixBundles.add(drawingMatrixBundle);
                largestHeight = 0;
                drawingMatrices.clear();
                drawingMatrices.add(drawingMatrix);
            }else{
                drawingMatrices.add(drawingMatrix);
            }
            largestHeight += height;

        }

        int canvasCenterWidth = (int)(drawingCanvas.getWidth() / 2.00),
                canvasCenterHeight = (int)(drawingCanvas.getHeight() / 2.00),

                boundsCenterWidth = (int)(largestWidth / 2.00),
                boundsCenterHeight = (int)(largestHeight / 2.00);
        int x = canvasCenterWidth - boundsCenterWidth,
                y = canvasCenterHeight - boundsCenterHeight;

        DrawingMatrixBundle drawingMatrixBundle = new DrawingMatrixBundle(drawingMatrices.toArray(new DrawingMatrix[drawingMatrices.size()]), new Rect(x, y, x + (int)largestWidth, y + (int)largestHeight));
        drawingMatrixBundles.add(drawingMatrixBundle);
        drawingMatrices.clear();

        return drawingMatrixBundles.toArray(new DrawingMatrixBundle[drawingMatrixBundles.size()]);
    }

    public  static int getStringWidth(String text, Paint pPaint) {
        Rect bounds = new Rect();
        pPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width();
    }

    private static int adjustHeightForIndex(int index) {
        return (index < HALF_MATRIX_COUNT)? 0 : 1;
    }

    private TileColumn getColumnForTriple(TextTile startTile, TextTile middleTile, TextTile endTile) {
        switch (startTile.getTileType()){
            case TextTile.TILE_TYPE_SQUARE:
                switch (middleTile.getTileType()){
                    case TextTile.TILE_TYPE_SQUARE:
                        switch (endTile.getTileType()){
                            case TextTile.TILE_TYPE_SQUARE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 2, 2), new RenderableBlock(middleTile, 2, 2, 2), new RenderableBlock(endTile, 4, 2, 2) });
                            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 2, 2), new RenderableBlock(middleTile, 2, 1, 1), new RenderableBlock(endTile, 8, 4, 1) });
                            case TextTile.TILE_TYPE_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 1, 1), new RenderableBlock(middleTile, 1, 1, 1), new RenderableBlock(endTile, 6, 6, 1) });
                        }
                        break;
                    case TextTile.TILE_TYPE_MINI_RECTANGLE:
                        switch (endTile.getTileType()){
                            case TextTile.TILE_TYPE_SQUARE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 2, 2), new RenderableBlock(middleTile, 2, 4, 1), new RenderableBlock(endTile, 8, 1, 1) });
                            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 2, 2), new RenderableBlock(middleTile, 2, 4, 1), new RenderableBlock(endTile, 8, 4, 1) });
                            case TextTile.TILE_TYPE_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 1, 1), new RenderableBlock(middleTile, 1, 5, 1), new RenderableBlock(endTile, 6, 6, 1) });
                        }
                        break;
                    case TextTile.TILE_TYPE_RECTANGLE:
                        switch (endTile.getTileType()){
                            case TextTile.TILE_TYPE_SQUARE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 2, 2), new RenderableBlock(middleTile, 2, 4, 1), new RenderableBlock(endTile, 8, 1, 1) });
                            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                                case TextTile.TILE_TYPE_RECTANGLE:
                                    return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 1, 1), new RenderableBlock(middleTile, 1, 5, 1), new RenderableBlock(endTile, 6, 4 + middleTile.getTileType(), 1) });
                        }
                        break;
                }
                break;
            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                switch (middleTile.getTileType()){
                    case TextTile.TILE_TYPE_SQUARE:
                        switch (endTile.getTileType()){
                            case TextTile.TILE_TYPE_SQUARE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 4, 1), new RenderableBlock(middleTile, 6, 1, 1), new RenderableBlock(endTile, 7, 1, 1) });
                            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 4, 1), new RenderableBlock(middleTile, 4, 2, 2), new RenderableBlock(endTile, 6, 4, 1) });
                            case TextTile.TILE_TYPE_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 5, 1), new RenderableBlock(middleTile, 5, 1, 1), new RenderableBlock(endTile, 6, 6, 1) });
                        }
                        break;
                    case TextTile.TILE_TYPE_MINI_RECTANGLE:
                        switch (endTile.getTileType()){
                            case TextTile.TILE_TYPE_SQUARE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 4, 1), new RenderableBlock(endTile, 10, 1, 1) });
                            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 3, 1), new RenderableBlock(middleTile, 3, 3, 1), new RenderableBlock(endTile, 6, 6, 1) });
                            case TextTile.TILE_TYPE_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 3, 1), new RenderableBlock(middleTile, 3, 3, 1), new RenderableBlock(endTile, 6, 6, 1) });
                        }
                        break;
                    case TextTile.TILE_TYPE_RECTANGLE:
                        switch (endTile.getTileType()){
                            case TextTile.TILE_TYPE_SQUARE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 5, 1), new RenderableBlock(endTile, 11, 1, 1) });
                            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 3, 1), new RenderableBlock(middleTile, 6, 6, 1), new RenderableBlock(endTile, 3, 3, 1) });
                                case TextTile.TILE_TYPE_RECTANGLE:
                                    return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 2, 1), new RenderableBlock(middleTile, 2, 4, 1), new RenderableBlock(endTile, 6, 6, 1) });
                        }
                        break;
                }
                break;
            case TextTile.TILE_TYPE_RECTANGLE:
                switch (middleTile.getTileType()){
                    case TextTile.TILE_TYPE_SQUARE:
                        switch (endTile.getTileType()){
                            case TextTile.TILE_TYPE_SQUARE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 1, 1), new RenderableBlock(endTile, 7, 1, 1) });
                            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 1, 1), new RenderableBlock(endTile, 7, 5, 1) });
                            case TextTile.TILE_TYPE_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 5, 1), new RenderableBlock(middleTile, 5, 1, 1), new RenderableBlock(endTile, 6, 6, 1) });
                        }
                        break;
                    case TextTile.TILE_TYPE_MINI_RECTANGLE:
                        switch (endTile.getTileType()){
                            case TextTile.TILE_TYPE_SQUARE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 4, 1), new RenderableBlock(endTile, 10, 1, 1) });
                            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 3, 1), new RenderableBlock(endTile, 9, 3, 1) });
                                case TextTile.TILE_TYPE_RECTANGLE:
                                    return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 2, 1), new RenderableBlock(endTile, 8, 4, 1) });
                        }
                        break;
                    case TextTile.TILE_TYPE_RECTANGLE:
                        switch (endTile.getTileType()){
                            case TextTile.TILE_TYPE_SQUARE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 5, 1), new RenderableBlock(endTile, 11, 1, 1) });
                            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                                return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 4, 1), new RenderableBlock(endTile, 10, 2, 1) });
                                case TextTile.TILE_TYPE_RECTANGLE:
                                    return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 3, 1), new RenderableBlock(endTile, 9, 3, 1) });
                        }
                        break;
                }
                break;
        }
        return null;
    }

    private TileColumn getColumnForDouble(TextTile startTile, TextTile middleTile) {
        switch (startTile.getTileType()){
            case TextTile.TILE_TYPE_SQUARE:
                switch (middleTile.getTileType()) {
                    case TextTile.TILE_TYPE_SQUARE:
                        return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 2, 2), new RenderableBlock(middleTile, 2, 2, 2) });
                    case TextTile.TILE_TYPE_MINI_RECTANGLE:
                        return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 2, 2), new RenderableBlock(middleTile, 2, 4, 2) });
                    case TextTile.TILE_TYPE_RECTANGLE:
                        return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 1, 1), new RenderableBlock(middleTile, 1, 5, 1) });
                    default:
                        return null;
                }
            case TextTile.TILE_TYPE_MINI_RECTANGLE:
                switch (middleTile.getTileType()) {
                    case TextTile.TILE_TYPE_SQUARE:
                        return new TileColumn(new RenderableBlock[]{new RenderableBlock(startTile, 0, 4, 2), new RenderableBlock(middleTile, 4, 2, 2)});
                    case TextTile.TILE_TYPE_MINI_RECTANGLE:
                        return new TileColumn(new RenderableBlock[]{new RenderableBlock(startTile, 0, 3, 2), new RenderableBlock(middleTile, 3, 3, 2)});
                    case TextTile.TILE_TYPE_RECTANGLE:
                        return new TileColumn(new RenderableBlock[]{new RenderableBlock(startTile, 0, 4, 1), new RenderableBlock(middleTile, 6, 2 + (2 * middleTile.getTileType()), 1)});
                    default:
                        return null;
                }
            case TextTile.TILE_TYPE_RECTANGLE:
                switch (middleTile.getTileType()) {
                    case TextTile.TILE_TYPE_SQUARE:
                        return new TileColumn(new RenderableBlock[]{new RenderableBlock(startTile, 0, 5, 1), new RenderableBlock(middleTile, 5, 1, 1)});
                    case TextTile.TILE_TYPE_MINI_RECTANGLE:
                        case TextTile.TILE_TYPE_RECTANGLE:
                            return new TileColumn(new RenderableBlock[]{new RenderableBlock(startTile, 0, 6, 1), new RenderableBlock(middleTile, 6, 2 + (2 * middleTile.getTileType()), 1)});
                    default:
                        return null;
                }
                default:
                    return null;
        }
    }

    private TileColumn getColumnForSingle(TextTile startTile) {
        return new TileColumn(new RenderableBlock[]{ new RenderableBlock(startTile, 0, 2 + (2 * startTile.getTileType()), 2) });
    }

    private TextTile[] createTextTiles(String[] words) {
        TextTile[] tiles = new TextTile[words.length];
        for(int pos = 0; pos < tiles.length; pos++){
            tiles[pos] = new TextTile(words[pos]);
        }
        return tiles;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[0];
    }

    private static class TextTile{
        public static final int TILE_TYPE_SQUARE = 0, TILE_TYPE_MINI_RECTANGLE = 1, TILE_TYPE_RECTANGLE = 2;
        private final String text;
        private final int tileType;

        public TextTile(String pText){
            this.text = pText;
            this.tileType = this.getTileType(this.text);
        }

        private int getTileType(String text) {
            int textLength = text.length();
            if(textLength <= 2){
                return TILE_TYPE_SQUARE;
            }else if(textLength  > 2 && textLength <= 5){
                return TILE_TYPE_MINI_RECTANGLE;
            }else{
                return TILE_TYPE_RECTANGLE;
            }
        }

        public String getText(){ return this.text; }
        public int getTileType(){ return this.tileType; }

        @Override
        public String toString() {
            return "Text=\"" + text + "\" tileType=\"" + tileType + "\"";
        }
    }
    
    private static class RenderableBlock{
        private final int columnBlockIndex, widthSpan, heightSpan;
        private final TextTile textTile;

        public RenderableBlock(TextTile pTextTile,int pColumnBlockIndex, int pWidthSpan, int pHeightSpan){
            this.textTile = pTextTile;
            this.columnBlockIndex = pColumnBlockIndex;
            this.widthSpan = pWidthSpan;
            this.heightSpan = pHeightSpan;
        }

        public int getColumnBlockIndex() {
            return columnBlockIndex;
        }

        public int getWidthSpan() {
            return widthSpan;
        }

        public int getHeightSpan() {
            return heightSpan;
        }



        public TextTile getTextTile() {
            return textTile;
        }

        @Override
        public String toString() {
            return "RenderableBlock: " + getTextTile() + ": widthSpan=\"" + getWidthSpan() + "\" heightSpan=\"" + getHeightSpan() + "\" ColumnIndex=\"" + getColumnBlockIndex() + "\"";
        }
    }

    public static class TileColumn{
        private RenderableBlock[] renderableBlocks;

        public TileColumn(RenderableBlock[] pRenderableBlocks) {
            this.renderableBlocks = pRenderableBlocks;
        }

        public RenderableBlock[] getRenderableBlocks(){ return renderableBlocks; }

        public DrawingMatrix CalculateDrawingMatrices(Paint drawingPaint, short matrixSize){
            float originalTextSize = drawingPaint.getTextSize();
            short firstSubRowHeight = 0, secondSubRowHeight = 0;
            DrawingMatrix drawingMatrix = new DrawingMatrix(matrixSize);

            List<RenderableBlock> spanningBlock = new ArrayList<>();
            for(int pos = 0; pos < renderableBlocks.length; pos++){
                RenderableBlock renderableBlock = renderableBlocks[pos];
                if(renderableBlock.getHeightSpan() > 1){
                    spanningBlock.add(renderableBlock);
                }else{
                    int subRow = adjustHeightForIndex(renderableBlock.columnBlockIndex);
                    double maxWidth = (matrixSize * renderableBlock.getWidthSpan());
                    double textWidth = getStringWidth(renderableBlock.getTextTile().text, drawingPaint);
                    double widthRatio = maxWidth / textWidth;

                    short scaledHeight = (short)(drawingPaint.getTextSize() * widthRatio);
                    drawingPaint.setTextSize(scaledHeight);
                    double scaledWidth = getStringWidth(renderableBlock.getTextTile().getText(), drawingPaint);
                    double scaleRatio = (double)scaledHeight / scaledWidth;

                    drawingPaint.setTextSize(originalTextSize);
                    if(subRow == 0){
                        //firstSubRow
                        if(scaleRatio > 1.1){
                            scaledHeight = (firstSubRowHeight != 0)? firstSubRowHeight : (short)originalTextSize;
                        }
                        if(firstSubRowHeight < scaledHeight){
                            firstSubRowHeight = scaledHeight;
                        }
                    }else{
                        //SecondSubRow
                        if(scaleRatio > 1.1){
                            scaledHeight = (secondSubRowHeight != 0)? secondSubRowHeight : (short)originalTextSize;
                        }
                        if(secondSubRowHeight < scaledHeight){
                            secondSubRowHeight = scaledHeight;
                        }
                    }

                    for(int x = renderableBlock.columnBlockIndex; x < (renderableBlock.getColumnBlockIndex() + renderableBlock.getWidthSpan()); x++){
                        drawingMatrix.get(x).setHeight(scaledHeight);
                    }
                }
            }

            firstSubRowHeight = (firstSubRowHeight <= 0)? matrixSize : firstSubRowHeight;
            secondSubRowHeight = (secondSubRowHeight <= 0)? matrixSize : secondSubRowHeight;

            short halfHeight = (short)((double)(firstSubRowHeight + secondSubRowHeight) / 2.00);
            drawingMatrix.setFirstRowHeight(firstSubRowHeight);
            drawingMatrix.setSecondRowHeight(secondSubRowHeight);
            for (int pos = 0; pos < spanningBlock.size(); pos++){
                RenderableBlock renderableBlock = spanningBlock.get(pos);
                drawingPaint.setTextSize(halfHeight * 2);
                short matrixWidth = (short)((double)getStringWidth(renderableBlock.getTextTile().text, drawingPaint) / (double)renderableBlock.getWidthSpan());
                for(int y = 0; y < renderableBlock.heightSpan; y++){
                    for(int x = 0; x < renderableBlock.getWidthSpan(); x++){
                        int startIndex = (renderableBlock.getColumnBlockIndex()  + (HALF_MATRIX_COUNT * y) );
                        //Log.i(TAG, startIndex + "|" + x + "|" + renderableBlock.getColumnBlockIndex() + "|" +  (HALF_MATRIX_COUNT * row) + "|" + y + "|" + renderableBlock.getHeightSpan());
                        Matrix matrix = drawingMatrix.get(startIndex + x);
                        matrix.setWidth(matrixWidth);
                        matrix.setHeight(halfHeight);
                    }
                }
            }
            drawingPaint.setTextSize(originalTextSize);
            return drawingMatrix;
        }

        @Override
        public String toString() {
            String meString = "";
            for(int pos =0; pos < renderableBlocks.length; pos++){
                meString += renderableBlocks[pos].toString() + "\n";
            }
            return meString;
        }
    }
}
