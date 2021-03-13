/*
 * MIT License
 *
 * Copyright (c) 2019 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.guitesting.swing.internal;

import org.eclipse.jdt.annotation.Nullable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

public final class ImageCompare {

    private ImageCompare() {
    }

    public static ImageCompare newImageCompare() {
        return new ImageCompare();
    }

    /**
     * Returns the size of the image.
     *
     * @param image a (completely loaded) Image
     */
    //TODO move to commons/util
    private static Dimension getSize(Image image) {
        Dimension size = new Dimension(
                image.getWidth(null),
                image.getHeight(null));
        if (size.getWidth() < 0 || size.getHeight() < 0) {
            throw new IllegalArgumentException(
                    "Image is not loaded completely.");
        }
        return size;
    }

    /**
     * @param pixels array of pixels in INT_ARGB format (e.g. as returned by
     *               PixelGrabber.getPixels())
     */
    private static BufferedImage getImageFromPixels(
            int[] pixels, int width, int height) {

        if (pixels.length != width * height) {
            throw new IllegalArgumentException(
                    "pixels must contain width*height items (" + pixels.length
                            + " != " + width + "*" + height + ")");
        }
        BufferedImage image = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    /**
     * @param image [must be loaded completely]
     * @return array of pixels in INT_ARGB format
     */
    private static int[] getPixels(Image image) {
        Dimension size = getSize(image);
        int[] pixels = new int[size.width * size.height];
        PixelGrabber pg = new PixelGrabber(
                image, 0, 0, size.width, size.height, pixels, 0, size.width);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "Unexpected interrupt when retrieving image pixels", e);
        }
        return pixels;
    }

    /**
     * @return pixel in INT_ARGB format
     */
    private static int getPixel(int red, int green, int blue, int alpha) {
        return ((alpha << 24) & 0xff000000) | ((red << 16) & 0xff0000)
                | ((green << 8) & 0xff00) | ((blue) & 0xff);
    }


    /**
     * @param pixel in INT_ARGB format
     */
    private static Color getColorOfPixel(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new Color(red, green, blue, alpha);
    }

    private static Dimension max(Dimension size1, Dimension size2) {
        return new Dimension(
                Math.max(size1.width, size2.width),
                Math.max(size1.height, size2.height));
    }

    /**
     * @return pixel in INT_ARGB format
     */
    private static int getPixel(Color color) {
        return getPixel(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                color.getAlpha());
    }


    /**
     * Returns an image marking the differences between imageA and imageB with
     * {@link Color#black} pixels on a (transparent) white canvas,
     * or {@code null} when imageA and imageB are not different.
     *
     * <p>The images are compared pixel by pixel and all pixel that are not
     * equal or that only exist in one image are marked {@link Color#black}.</p>
     */
    @Nullable
    public BufferedImage differenceMask(Image imageA, Image imageB) {

        Dimension sizeA = getSize(imageA);
        Dimension sizeB = getSize(imageB);
        Dimension size = max(sizeA, sizeB);
        int h = size.height;
        int w = size.width;

        int whiteTransparentPixel = getWhiteTransparentPixel();
        int blackPixel = getPixel(Color.black);
        int[] pixelsA = getPixels(imageA);
        int[] pixelsB = getPixels(imageB);
        int[] pixelsResult = new int[size.width * size.height];

        boolean imagesDiffer = false;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int color;
                boolean hasPixelA = x < sizeA.width && y < sizeA.height;
                boolean hasPixelB = x < sizeB.width && y < sizeB.height;
                int pixelA = hasPixelA ? pixelsA[y * sizeA.width + x] : 0;
                int pixelB = hasPixelB ? pixelsB[y * sizeB.width + x] : 0;
                if (hasPixelA && hasPixelB && pixelA == pixelB) {
                    color = whiteTransparentPixel;
                } else {
                    imagesDiffer = true;
                    color = blackPixel;
                }
                pixelsResult[y * w + x] = color;
            }
        }

        return imagesDiffer ? getImageFromPixels(pixelsResult, w, h) : null;
    }

    /**
     * Returns a new image of transparent white pixels with the same size as
     * originalImage
     */
    public BufferedImage transparentImage(Image originalImage) {

        Dimension size = getSize(originalImage);
        int h = size.height;
        int w = size.width;

        int whiteTransparentPixel = getWhiteTransparentPixel();
        int[] pixelsResult = new int[size.width * size.height];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixelsResult[y * w + x] = whiteTransparentPixel;
            }
        }

        return getImageFromPixels(pixelsResult, w, h);
    }

    private int getWhiteTransparentPixel() {
        return getPixel(new Color(255, 255, 255, 0));
    }
}
