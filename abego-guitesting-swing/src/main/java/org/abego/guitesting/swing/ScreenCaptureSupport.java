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

package org.abego.guitesting.swing;

import org.abego.commons.seq.Seq;
import org.abego.commons.timeout.TimeoutSupplier;
import org.abego.commons.timeout.Timeoutable;
import org.eclipse.jdt.annotation.Nullable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URI;
import java.time.Duration;

/**
 * Support for screen captures/screenshots of the screen or {@link Component}s.
 */
public interface ScreenCaptureSupport extends TimeoutSupplier {

    /**
     * Returns an image/screenshot of the rectangle of the screen.
     *
     * @param screenRect a {@link Rectangle} on the screen
     * @return an image of the rectangle of the screen.
     */
    BufferedImage captureScreen(Rectangle screenRect);

    /**
     * Returns an image/screenshot of the {@code component}, or of the
     * {@code rectangle} of the {@code component}, if {@code rectangle}
     * is not {@code null}.
     *
     * @param component the {@link Component} to take a screenshot of.
     * @param rectangle the area of the component to take a screenshot of
     *                  (in coordinates relative to the component), or
     *                  {@code null} when a screenshot of the full component
     *                  should be returned.
     * @return an image of the {@code component} or a part of it
     */
    BufferedImage captureScreen(Component component,
                                @Nullable Rectangle rectangle);

    /**
     * Returns an image/screenshot of the {@code component}.
     *
     * @param component the {@link Component} to take a screenshot of.
     * @return an image/screenshot of the {@code component}
     */
    BufferedImage captureScreen(Component component);

    /**
     * Returns the difference between {@code imageA} and {@code imageB} as an
     * {@link ImageDifference} object.
     */
    ImageDifference difference(Image imageA, Image imageB);

    /**
     * Waits until the {@code component}, or the {@code rectangle} of the
     * {@code component}, if {@code rectangle} is not {@code null},
     * matches one of the {@code expectedImages}.
     *
     * @param component      the {@link Component} to compare with the
     *                       {@code expectedImages}
     * @param rectangle      the area of the component (in coordinates relative
     *                       to the component)to compare with the
     *                       {@code expectedImages}. When {@code null}
     *                       the full component is compared
     * @param expectedImages the images to compare the given area with
     */
    @Timeoutable
    void waitUntilScreenshotMatchesImage(
            Component component,
            @Nullable Rectangle rectangle,
            Image... expectedImages);

    /**
     * Waits until the {@code component} matches one of the
     * {@code expectedImages}.
     *
     * @param component      the {@link Component} to compare with the
     *                       {@code expectedImages}
     * @param expectedImages the images to compare the {@code component} with.
     */
    @Timeoutable
    void waitUntilScreenshotMatchesImage(
            Component component,
            Image... expectedImages);

    /**
     * Returns the value of the {@code generateSnapshotIfMissing} property.
     *
     * @see #waitUntilScreenshotMatchesSnapshot(Component, Rectangle, String)
     */
    boolean getGenerateSnapshotIfMissing();

    /**
     * Sets the {@code generateSnapshotIfMissing} property to the {@code value}.
     *
     * @see #waitUntilScreenshotMatchesSnapshot(Component, Rectangle, String)
     */
    void setGenerateSnapshotIfMissing(boolean value);

    /**
     * Returns the value of the {@code delayBeforeNewSnapshot} property.
     *
     * @see #waitUntilScreenshotMatchesSnapshot(Component, Rectangle, String)
     */
    Duration getDelayBeforeNewSnapshot();

    /**
     * Sets the {@code delayBeforeNewSnapshot} property to the {@code duration}.
     *
     * @see #waitUntilScreenshotMatchesSnapshot(Component, Rectangle, String)
     */
    void setDelayBeforeNewSnapshot(Duration duration);

    /**
     * Waits until the {@code component}, or the {@code rectangle} of the
     * {@code component}, if {@code rectangle} is not {@code null},
     * matches one of the images defined for the snapshot with the given
     * {@code snapshotName}.
     *
     * <p>When the snapshot does not yet have images the behaviour depends on
     * the property {@code generateSnapshotIfMissing}
     * (see {@link #getGenerateSnapshotIfMissing()}):</p>
     * <ul>
     *     <li>{@code generateSnapshotIfMissing == true}: after a delay of {@link #getDelayBeforeNewSnapshot()}
     *     a screenshot of the given area is stored as the first image of this
     *     snapshot and the method returns normally.</li>
     *     <li>{@code generateSnapshotIfMissing == false}: the method throws
     *     a {@link UndefinedSnapshotException}</li>
     * </ul>
     *
     * <p>When the snapshot has images but the given area does not match any
     * of these images, even after the duration defined by {@link #timeout()},
     * the method throws a {@link ImageNotMatchingSnapshotException}.
     *
     * @param component    the {@link Component} to compare with the images of
     *                     the snapshot
     * @param rectangle    the area of the component (in coordinates relative
     *                     to the component) to compare with the images of the
     *                     snapshot.
     *                     When {@code null} the full component is compared.
     * @param snapshotName a Java identifier to identify the snapshot. The
     *                     snapshotName must be unique within the method calling
     *                     this method.
     */
    @Timeoutable
    void waitUntilScreenshotMatchesSnapshot(
            Component component,
            @Nullable Rectangle rectangle,
            String snapshotName)
            throws
            UndefinedSnapshotException, ImageNotMatchingSnapshotException;

    /**
     * Waits until the {@code component} matches one of the images defined for
     * the snapshot with the given {@code snapshotName}.
     *
     * <p>When the snapshot does not yet have images the behaviour depends on
     * the property {@code generateSnapshotIfMissing}
     * (see {@link #getGenerateSnapshotIfMissing()}):</p>
     * <ul>
     *     <li>{@code generateSnapshotIfMissing == true}: after a delay of {@link #getDelayBeforeNewSnapshot()}
     *     a screenshot of the {@code component} is stored as the first image
     *     of this snapshot and the method returns normally.</li>
     *     <li>{@code generateSnapshotIfMissing == false}: the method throws
     *     a {@link UndefinedSnapshotException}</li>
     * </ul>
     *
     * <p>When the snapshot has images but the {@code component} does not match
     * any of these images, even after the duration defined by
     * {@link #timeout()}, the method throws a {@link ImageNotMatchingSnapshotException}.
     *
     * @param component    the {@link Component} to compare with the images of
     *                     the snapshot
     * @param snapshotName a Java identifier to identify the snapshot. The
     *                     snapshotName must be unique within the method calling
     *                     this method.
     */
    @Timeoutable
    void waitUntilScreenshotMatchesSnapshot(
            Component component, String snapshotName)
            throws
            UndefinedSnapshotException, ImageNotMatchingSnapshotException;

    /**
     * Writes the {@code image} to the given {@code file}.
     */
    void writeImage(RenderedImage image, File file);

    /**
     * Returns the {@link Image} read from the given {@code file}.
     */
    BufferedImage readImage(File file);

    /**
     * The difference between two {@link Image}s ({@code imageA} and {@code imageB}).
     */
    interface ImageDifference {

        /**
         * Returns {@code true} when the images don't match, i.e. there are
         * differences between the images, {@code false} otherwise.
         */
        default boolean imagesAreDifferent() {
            return getDifferenceKind() != Kind.NO_DIFFERENCE;
        }

        /**
         * Returns the color to use by default to indicate a pixel in
         * {@code imageA} does not match a pixel in {@code imageB}.
         * <p>See {@link #getDifferenceImage()}.</p>
         */
        Color getDefaultDiffColor();

        /**
         * Returns the first image of the comparison.
         */
        Image getImageA();

        /**
         * Returns the second image of the comparison.
         */
        Image getImageB();

        /**
         * Returns an {@link Image} with pixels having the same color as in
         * {@code imageA} if the pixel matches the pixel in {@code imageB},
         * and {@code diffColor} if the pixels do not match.
         */
        Image getDifferenceImage(Color diffColor);

        /**
         * Returns an {@link Image} with pixels having the same color as in
         * {@code imageA} if the pixel matches the pixel in {@code imageB},
         * and {@link #getDefaultDiffColor()} if the pixels do not match.
         */
        Image getDifferenceImage();

        /**
         * Returns an {@link Image} with pixels having in {@link Color#white}
         * if the pixel matches in {@code imageA} in {@code imageB},
         * and {@code maskColor} if the pixels do not match.
         */
        Image getDifferenceMaskImage(Color maskColor);

        /**
         * Returns an {@link Image} with pixels having in {@link Color#white}
         * if the pixel matches in {@code imageA} in {@code imageB},
         * and {@link Color#black} if the pixels do not match.
         */
        Image getDifferenceMaskImage();

        /**
         * Returns the {@link Kind} of the difference between the two images.
         */
        Kind getDifferenceKind();

        enum Kind {
            /**
             * No differences between the images.
             */
            NO_DIFFERENCE,
            /**
             * The images have difference sizes.
             */
            DIFFERENT_SIZE,
            /**
             * The content of the images does not match.
             */
            DIFFERENT_CONTENT
        }
    }

    /**
     * No snapshot defined with the given snapshot name.
     */
    class UndefinedSnapshotException extends GuiTestingException {
        private final Description description;

        public UndefinedSnapshotException(Description description) {
            super(description.getMessage());
            this.description = description;
        }

        public Description getDescription() {
            return description;
        }

        interface Description {
            String getMessage();

            String getSnapshotName();

            URI getSnapshotContainer();

            URI getActualImage();
        }
    }

    /**
     * The image does not match any of the images in the snapshot.
     */
    class ImageNotMatchingSnapshotException extends GuiTestingException {
        private final Description description;

        public ImageNotMatchingSnapshotException(Description description) {
            super(description.getMessage());
            this.description = description;
        }

        public Description getDescription() {
            return description;
        }

        interface Description {
            String getMessage();

            String getSnapshotName();

            URI getSnapshotContainer();

            URI getActualImage();

            Seq<URI> getSnapshotImages();

            Seq<URI> getDifferenceImages();

            Seq<URI> getDifferenceMaskImages();
        }
    }
}
