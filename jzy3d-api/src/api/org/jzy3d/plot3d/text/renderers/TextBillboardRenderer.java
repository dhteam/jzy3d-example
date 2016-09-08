package org.jzy3d.plot3d.text.renderers;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.rendering.compat.GLES2CompatUtils;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.text.AbstractTextRenderer;
import org.jzy3d.plot3d.text.ITextRenderer;
import org.jzy3d.plot3d.text.align.Halign;
import org.jzy3d.plot3d.text.align.Valign;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.glu.GLU;

/**
 * A {@link TextBillboardRenderer} allows writing 2d text always facing the
 * Camera of a 3d Scene. <br>
 * TextBillboard provides the pixel definition of all characters of the ASCII
 * alphabet. A default bitmap (plain rectangle) is provided for unrecognized
 * characters (those that do not have an ASCII code). The bitmap library is
 * static, and thus no overhead is generated by the use of several instances of
 * TextBillboard. <br>
 * It is however not necessary to have an instance of TextBillboard for each
 * drawn string. <br>
 * <code>
 * String s          = new String("2d text in 3d Scene");<br>
 * TextBillboard txt = new TextBillboard();<br>
 * BoundingBox3d box;<br>
 * <br>
 * txt.drawText(gl, s, Coord3d.ORIGIN, Halign.LEFT, Valign.GROUND, Color.BLACK);<br>
 * box = txt.drawText(gl, glu, cam, s, Coord3d.ORIGIN, Halign.LEFT, Valign.GROUND, Color.BLACK);<br>
 * </code> <br>
 * <b>Layout constants</b> <br>
 * As demonstrated in the previous example, the {@link TextBillboardRenderer}
 * handles vertical and horizontal layout of text according to the given text
 * coordinate. <br>
 * The following picture illustrates the text layout when using the various
 * layout constants <img
 * src="plot3d/primitives/doc-files/TextBillboardBillboard-1.gif">
 * 
 * 
 * 
 * @author Martin Pernollet
 */
public class TextBillboardRenderer extends AbstractTextRenderer implements ITextRenderer {
    /**
     * The TextBillboard class provides support for drawing ASCII characters Any
     * non ascii caracter will be replaced by a square.
     */
    public TextBillboardRenderer() {
    }

    @Override
    public void drawSimpleText(GL gl, GLU glu, Camera cam, String s, Coord3d position, Color color) {
        glRaster(gl, position, color);

        printString(gl, s, Halign.RIGHT, Valign.GROUND);
    }

    /** Draw a string at the specified position. */
    public void drawText(GL gl, String s, Coord3d position, Halign halign, Valign valign, Color color) {
    }

    /**
     * Draw a string at the specified position and compute the 3d volume
     * occupied by the string according to the current Camera configuration.
     */
    @Override
    public BoundingBox3d drawText(GL gl, GLU glu, Camera cam, String s, Coord3d position, Halign halign, Valign valign, Color color, Coord2d screenOffset, Coord3d sceneOffset) {
        glRaster(gl, position, color);
        BillBoardSize dims = printString(gl, s, halign, valign);
        BoundingBox3d txtBounds = computeTextBounds(gl, glu, cam, position, dims);
        return txtBounds;
    }

    public void glRaster(GL gl, Coord3d position, Color color) {
        if (gl.isGL2()) {
            gl.getGL2().glColor3f(color.r, color.g, color.b);
            gl.getGL2().glRasterPos3f(position.x, position.y, position.z);
        } else {
            GLES2CompatUtils.glColor3f(color.r, color.g, color.b);
            GLES2CompatUtils.glRasterPos3f(position.x, position.y, position.z);
        }
    }

    public BoundingBox3d computeTextBounds(GL gl, GLU glu, Camera cam, Coord3d position, BillBoardSize dims) {
        Coord3d posScreen = cam.modelToScreen(gl, glu, position);
        Coord3d botLeft = new Coord3d();
        Coord3d topRight = new Coord3d();

        botLeft.x = posScreen.x + dims.xoffset;
        botLeft.y = posScreen.y + dims.yoffset;
        botLeft.z = posScreen.z;
        topRight.x = botLeft.x + dims.width;
        topRight.y = botLeft.y + dims.height;
        topRight.z = botLeft.z;

        BoundingBox3d txtBounds = new BoundingBox3d();
        txtBounds.add(cam.screenToModel(gl, glu, botLeft));
        txtBounds.add(cam.screenToModel(gl, glu, topRight));
        return txtBounds;
    }

    /********************************************************************/

    /**
     * Performs actual display of the string.
     * 
     * @param gl
     *            GL2 context.
     * @param s
     *            the String that must be displayed.
     * @param halign
     *            the horizontal alignment constant.
     * @param valign
     *            the vertical alignment constant.
     * @throws an
     *             IllegalArgumentException if the vertical or horizontal
     *             alignment constant value is unknown.
     */
    private BillBoardSize printString(GL gl, String s, Halign halign, Valign valign) {
        byte[] acodes = s.getBytes();
        int nchar = s.length();
        float xorig = 0.0f;
        float yorig = 2.0f;
        float xmove = charWidth + charOffset;
        float ymove = 0.0f;

        // Compute horizontal alignment
        if (halign == Halign.RIGHT)
            ;/* xorig = xorig; */
        else if (halign == Halign.CENTER)
            xorig = nchar * xmove / 2;
        else if (halign == Halign.LEFT)
            xorig = nchar * xmove;
        else
            throw new IllegalArgumentException("Horizontal alignement constant unknown: " + halign);

        // Compute vertical alignment
        if (valign == Valign.TOP)
            yorig = 0.0f;
        else if (valign == Valign.GROUND)
            ;/* yorig = yorig; */
        else if (valign == Valign.CENTER)
            yorig = charHeight / 2;
        else if (valign == Valign.BOTTOM)
            yorig = charHeight;
        else
            throw new IllegalArgumentException("Vertical alignement constant unknown: " + valign);

        // Draw the bitmaps
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        int idx;
        for (int c = 0; c < acodes.length; c++) {
            idx = acodes[c] - 32;
            if (idx < 0 || idx > ascii.length)
                glBitmap(gl, charWidth, charHeight, xorig, yorig, xmove, ymove, nonascii, 0);
            else
                glBitmap(gl, charWidth, charHeight, xorig, yorig, xmove, ymove, ascii[idx], 0);
        }

        // Compute occupied space
        return new BillBoardSize(xmove * nchar, charHeight, -xorig, -yorig);
    }

    /********************************************************************/

    private void glBitmap(GL gl, int charWidth2, int charHeight2, float xorig, float yorig, float xmove, float ymove, byte[] nonascii2, int i) {
        if (gl.isGL2()) {
            gl.getGL2().glBitmap(charWidth, charHeight, xorig, yorig, xmove, ymove, nonascii, 0);
        } else {
            GLES2CompatUtils.glBitmap(charWidth, charHeight, xorig, yorig, xmove, ymove, nonascii, 0);
        }

    }

    /**
     * Provides information on the 2d space occupied by a
     * {@link TextBillboardRenderer}
     */
    private class BillBoardSize {
        BillBoardSize(float width, float height, float xoffset, float yoffset) {
            this.width = width;
            this.height = height;
            this.xoffset = xoffset;
            this.yoffset = yoffset;
        }

        float width;
        float height;
        float xoffset;
        float yoffset;
    }

    /********************************************************************/

    private static int charHeight = 13; // px heigth
    private static int charWidth = 8; // px width
    private static int charOffset = 2; // px between 2 characters

    static private byte ascii[][] = { // each of the 95 line is a letter, each
            // of the (charHeight) byte of a line
            // represent a raw of (charWidth) pixels
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x18, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18,
                    (byte) 0x18 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x36, (byte) 0x36, (byte) 0x36,
                    (byte) 0x36 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x66, (byte) 0x66, (byte) 0xff, (byte) 0x66, (byte) 0x66, (byte) 0xff, (byte) 0x66, (byte) 0x66, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x7e, (byte) 0xff, (byte) 0x1b, (byte) 0x1f, (byte) 0x7e, (byte) 0xf8, (byte) 0xd8, (byte) 0xff, (byte) 0x7e,
                    (byte) 0x18 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x0e, (byte) 0x1b, (byte) 0xdb, (byte) 0x6e, (byte) 0x30, (byte) 0x18, (byte) 0x0c, (byte) 0x76, (byte) 0xdb, (byte) 0xd8,
                    (byte) 0x70 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0xc6, (byte) 0xcf, (byte) 0xd8, (byte) 0x70, (byte) 0x70, (byte) 0xd8, (byte) 0xcc, (byte) 0xcc, (byte) 0x6c,
                    (byte) 0x38 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x1c, (byte) 0x0c,
                    (byte) 0x0e },
            { (byte) 0x00, (byte) 0x00, (byte) 0x0c, (byte) 0x18, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x18,
                    (byte) 0x0c },
            { (byte) 0x00, (byte) 0x00, (byte) 0x30, (byte) 0x18, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x18,
                    (byte) 0x30 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x99, (byte) 0x5a, (byte) 0x3c, (byte) 0xff, (byte) 0x3c, (byte) 0x5a, (byte) 0x99, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0xff, (byte) 0xff, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x30, (byte) 0x18, (byte) 0x1c, (byte) 0x1c, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x38, (byte) 0x38, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x60, (byte) 0x60, (byte) 0x30, (byte) 0x30, (byte) 0x18, (byte) 0x18, (byte) 0x0c, (byte) 0x0c, (byte) 0x06, (byte) 0x06, (byte) 0x03,
                    (byte) 0x03 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x3c, (byte) 0x66, (byte) 0xc3, (byte) 0xe3, (byte) 0xf3, (byte) 0xdb, (byte) 0xcf, (byte) 0xc7, (byte) 0xc3, (byte) 0x66,
                    (byte) 0x3c },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x78, (byte) 0x38,
                    (byte) 0x18 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xc0, (byte) 0xc0, (byte) 0x60, (byte) 0x30, (byte) 0x18, (byte) 0x0c, (byte) 0x06, (byte) 0x03, (byte) 0xe7,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0x03, (byte) 0x03, (byte) 0x07, (byte) 0x7e, (byte) 0x07, (byte) 0x03, (byte) 0x03, (byte) 0xe7,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0xff, (byte) 0xcc, (byte) 0x6c, (byte) 0x3c, (byte) 0x1c,
                    (byte) 0x0c },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0x03, (byte) 0x03, (byte) 0x07, (byte) 0xfe, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0,
                    (byte) 0xff },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0xc3, (byte) 0xc3, (byte) 0xc7, (byte) 0xfe, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xe7,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x18, (byte) 0x0c, (byte) 0x06, (byte) 0x03, (byte) 0x03, (byte) 0x03,
                    (byte) 0xff },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0xc3, (byte) 0xc3, (byte) 0xe7, (byte) 0x7e, (byte) 0xe7, (byte) 0xc3, (byte) 0xc3, (byte) 0xe7,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x7f, (byte) 0xe7, (byte) 0xc3, (byte) 0xc3, (byte) 0xe7,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x38, (byte) 0x38, (byte) 0x00, (byte) 0x00, (byte) 0x38, (byte) 0x38, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x30, (byte) 0x18, (byte) 0x1c, (byte) 0x1c, (byte) 0x00, (byte) 0x00, (byte) 0x1c, (byte) 0x1c, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x0c, (byte) 0x18, (byte) 0x30, (byte) 0x60, (byte) 0xc0, (byte) 0x60, (byte) 0x30, (byte) 0x18, (byte) 0x0c,
                    (byte) 0x06 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x30, (byte) 0x18, (byte) 0x0c, (byte) 0x06, (byte) 0x03, (byte) 0x06, (byte) 0x0c, (byte) 0x18, (byte) 0x30,
                    (byte) 0x60 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x18, (byte) 0x0c, (byte) 0x06, (byte) 0x03, (byte) 0xc3, (byte) 0xc3,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0x3f, (byte) 0x60, (byte) 0xcf, (byte) 0xdb, (byte) 0xd3, (byte) 0xdd, (byte) 0xc3, (byte) 0x7e, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xff, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0x66, (byte) 0x3c,
                    (byte) 0x18 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xfe, (byte) 0xc7, (byte) 0xc3, (byte) 0xc3, (byte) 0xc7, (byte) 0xfe, (byte) 0xc7, (byte) 0xc3, (byte) 0xc3, (byte) 0xc7,
                    (byte) 0xfe },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xe7,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0xfc, (byte) 0xce, (byte) 0xc7, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc7, (byte) 0xce,
                    (byte) 0xfc },
            { (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xfc, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0,
                    (byte) 0xff },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xfc, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0,
                    (byte) 0xff },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0xc3, (byte) 0xc3, (byte) 0xcf, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xe7,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xff, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3,
                    (byte) 0xc3 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7c, (byte) 0xee, (byte) 0xc6, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06,
                    (byte) 0x06 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0xc6, (byte) 0xcc, (byte) 0xd8, (byte) 0xf0, (byte) 0xe0, (byte) 0xf0, (byte) 0xd8, (byte) 0xcc, (byte) 0xc6,
                    (byte) 0xc3 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0,
                    (byte) 0xc0 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xdb, (byte) 0xff, (byte) 0xff, (byte) 0xe7,
                    (byte) 0xc3 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc7, (byte) 0xc7, (byte) 0xcf, (byte) 0xcf, (byte) 0xdf, (byte) 0xdb, (byte) 0xfb, (byte) 0xf3, (byte) 0xf3, (byte) 0xe3,
                    (byte) 0xe3 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xe7,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xfe, (byte) 0xc7, (byte) 0xc3, (byte) 0xc3, (byte) 0xc7,
                    (byte) 0xfe },
            { (byte) 0x00, (byte) 0x00, (byte) 0x3f, (byte) 0x6e, (byte) 0xdf, (byte) 0xdb, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0x66,
                    (byte) 0x3c },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0xc6, (byte) 0xcc, (byte) 0xd8, (byte) 0xf0, (byte) 0xfe, (byte) 0xc7, (byte) 0xc3, (byte) 0xc3, (byte) 0xc7,
                    (byte) 0xfe },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0x03, (byte) 0x03, (byte) 0x07, (byte) 0x7e, (byte) 0xe0, (byte) 0xc0, (byte) 0xc0, (byte) 0xe7,
                    (byte) 0x7e },
            { (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18,
                    (byte) 0xff },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xe7, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3,
                    (byte) 0xc3 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x3c, (byte) 0x3c, (byte) 0x66, (byte) 0x66, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3,
                    (byte) 0xc3 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0xe7, (byte) 0xff, (byte) 0xff, (byte) 0xdb, (byte) 0xdb, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3,
                    (byte) 0xc3 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0x66, (byte) 0x66, (byte) 0x3c, (byte) 0x3c, (byte) 0x18, (byte) 0x3c, (byte) 0x3c, (byte) 0x66, (byte) 0x66,
                    (byte) 0xc3 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x3c, (byte) 0x3c, (byte) 0x66, (byte) 0x66,
                    (byte) 0xc3 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xc0, (byte) 0xc0, (byte) 0x60, (byte) 0x30, (byte) 0x7e, (byte) 0x0c, (byte) 0x06, (byte) 0x03, (byte) 0x03,
                    (byte) 0xff },
            { (byte) 0x00, (byte) 0x00, (byte) 0x3c, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
                    (byte) 0x3c },
            { (byte) 0x00, (byte) 0x03, (byte) 0x03, (byte) 0x06, (byte) 0x06, (byte) 0x0c, (byte) 0x0c, (byte) 0x18, (byte) 0x18, (byte) 0x30, (byte) 0x30, (byte) 0x60,
                    (byte) 0x60 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x3c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c,
                    (byte) 0x3c },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0x66, (byte) 0x3c,
                    (byte) 0x18 },
            { (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x38, (byte) 0x30,
                    (byte) 0x70 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0xc3, (byte) 0xc3, (byte) 0x7f, (byte) 0x03, (byte) 0xc3, (byte) 0x7e, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xfe, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xfe, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0,
                    (byte) 0xc0 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xc3, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc3, (byte) 0x7e, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0x7f, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03,
                    (byte) 0x03 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0xc0, (byte) 0xc0, (byte) 0xfe, (byte) 0xc3, (byte) 0xc3, (byte) 0x7e, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xfc, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x33,
                    (byte) 0x1e },
            { (byte) 0x7e, (byte) 0xc3, (byte) 0x03, (byte) 0x03, (byte) 0x7f, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0x7e, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xfe, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0,
                    (byte) 0xc0 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x00, (byte) 0x00, (byte) 0x18,
                    (byte) 0x00 },
            { (byte) 0x38, (byte) 0x6c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x0c, (byte) 0x00, (byte) 0x00, (byte) 0x0c,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc6, (byte) 0xcc, (byte) 0xf8, (byte) 0xf0, (byte) 0xd8, (byte) 0xcc, (byte) 0xc6, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0,
                    (byte) 0xc0 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18,
                    (byte) 0x78 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xdb, (byte) 0xdb, (byte) 0xdb, (byte) 0xdb, (byte) 0xdb, (byte) 0xdb, (byte) 0xfe, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0xfc, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7c, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0x7c, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xfe, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xfe, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x7f, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0x7f, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xc0, (byte) 0xe0, (byte) 0xfe, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xfe, (byte) 0x03, (byte) 0x03, (byte) 0x7e, (byte) 0xc0, (byte) 0xc0, (byte) 0x7f, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x1c, (byte) 0x36, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xfc, (byte) 0x30, (byte) 0x30, (byte) 0x30,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0xc6, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x3c, (byte) 0x3c, (byte) 0x66, (byte) 0x66, (byte) 0xc3, (byte) 0xc3, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0xe7, (byte) 0xff, (byte) 0xdb, (byte) 0xc3, (byte) 0xc3, (byte) 0xc3, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xc3, (byte) 0x66, (byte) 0x3c, (byte) 0x18, (byte) 0x3c, (byte) 0x66, (byte) 0xc3, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0xc0, (byte) 0x60, (byte) 0x60, (byte) 0x30, (byte) 0x18, (byte) 0x3c, (byte) 0x66, (byte) 0x66, (byte) 0xc3, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x60, (byte) 0x30, (byte) 0x18, (byte) 0x0c, (byte) 0x06, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x0f, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x38, (byte) 0xf0, (byte) 0x38, (byte) 0x18, (byte) 0x18, (byte) 0x18,
                    (byte) 0x0f },
            { (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x18,
                    (byte) 0x18 },
            { (byte) 0x00, (byte) 0x00, (byte) 0xf0, (byte) 0x18, (byte) 0x18, (byte) 0x18, (byte) 0x1c, (byte) 0x0f, (byte) 0x1c, (byte) 0x18, (byte) 0x18, (byte) 0x18,
                    (byte) 0xf0 },
            { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x8f, (byte) 0xf1, (byte) 0x60, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00 } };

    static private byte nonascii[] = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff };
}
