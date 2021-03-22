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
import java.net.URL;
import java.time.Duration;

/**
 * Support for screen captures/screenshots of the screen or {@link Component}s.
 */
public interface ScreenCaptureSupport extends TimeoutSupplier {
    String SNAPSHOT_NAME_DEFAULT = "snapshot"; //NON-NLS

    /**
     * Returns true when the "inner" bounds of a JFrame should be used when
     * screen capturing a JFrame, false when the "normal" bounds of a JFrame
     * should be used.
     *
     * <p>The bounds of a {@link javax.swing.JFrame} are slightly larger than
     * the "obvious"* area covered on the screen as the bounds also include
     * the area occupied by the "drop shadow" painted around the frame.
     * The drop shadow is semi-transparent so pixels from the desktop (or other
     * things behind the JFrame) may "shine through". Therefore a screenshot of
     * the frame with its* "normal" bounds would also include pixels not in the
     * control of the frame and thus screenshots of a JFrame may differ even if
     * the "inner" part of the JFrame didn't changed.</p>
     *
     * <p>To get more reproducible results one may use a smaller ("inner")
     * rectangle for JFrame screenshots. This inner rectangle includes the root
     * pane and the menu bar but not the border or "drop shadow" areas.</p>
     *
     * <p>This property has no effect when a rectangle is explicitly specified
     * when doing a screen capture of a JFrame.</p>
     *
     * @return true when the "inner" bounds of a JFrame should be used when
     * screen capturing a JFrame, false when the "normal" bounds of a JFrame
     * should be used
     */
    boolean getUseInnerJFrameBounds();

    /**
     * Sets the {@code useInnerJFrameBounds} property to the {@code value}.
     *
     * <p>See {@link #getUseInnerJFrameBounds()}.</p>
     *
     * @param value the new value of the {@code useInnerJFrameBounds} property
     */
    void setUseInnerJFrameBounds(boolean value);

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
     * Returns how tolerant images are compared, in percent.
     *
     * <p>0: each pixel must match exactly,</p>
     * <p>100: even a white and black pixel are considered equal.</p>
     *
     * @return the tolerance when comparing images, in percent
     */
    int getImageDifferenceTolerancePercentage();

    /**
     * Sets the {@code imageDifferenceTolerancePercentage} property to {@code value}.
     *
     * <p>See {@link #getImageDifferenceTolerancePercentage()}</p>
     *
     * @param value the value to set the {@code imageDifferenceTolerancePercentage}
     *              property to, in percent
     */
    void setImageDifferenceTolerancePercentage(int value);

    /**
     * Returns the difference between {@code imageA} and {@code imageB} as an
     * {@link ImageDifference} object.
     *
     * @param imageA an {@link Image} to compare with the other
     * @param imageB an {@link Image} to compare with the other
     * @return the difference between {@code imageA} and {@code imageB} as an
     * {@link ImageDifference} object
     */
    ImageDifference imageDifference(BufferedImage imageA, BufferedImage imageB);

    /**
     * Returns the difference between {@code imageA} and {@code imageB} as an
     * an image with {@link Color#black} pixels marking the parts of that differ
     * in both images or only exist in one of them.
     *
     * <p>The parts that don't differ are transparent white.</p>
     *
     * @param imageA an {@link Image} to compare with the other
     * @param imageB an {@link Image} to compare with the other
     * @return the difference between {@code imageA} and {@code imageB} as an
     * an {@link Image} with {@link Color#black} pixels marking the parts of that differ
     * in both images or only exist in one of them
     */
    BufferedImage imageDifferenceMask(BufferedImage imageA, BufferedImage imageB);

    /**
     * Writes the {@code image} to the given {@code file}.
     *
     * @param image the {@link Image} to write
     * @param file  the {@link File} to write the image to
     */
    void writeImage(RenderedImage image, File file);

    /**
     * Returns the {@link Image} read from the given {@code file}.
     *
     * @param file the {@link File} to read the image from
     * @return the {@link Image} read from {@code file}
     */
    BufferedImage readImage(File file);

    /**
     * Returns the {@link Image} read from the given {@code url}.
     *
     * @param url the {@link java.net.URI} to read the image from.
     * @return the {@link Image} read from {@code url}
     */
    BufferedImage readImage(URL url);

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
     * @return the {@link Image} of the screenshot
     */
    @Timeoutable
    BufferedImage waitUntilScreenshotMatchesImage(
            Component component,
            @Nullable Rectangle rectangle,
            BufferedImage... expectedImages);

    /**
     * Waits until the {@code component} matches one of the
     * {@code expectedImages}.
     *
     * @param component      the {@link Component} to compare with the
     *                       {@code expectedImages}
     * @param expectedImages the images to compare the {@code component} with.
     * @return the {@link Image} of the screenshot
     */
    @Timeoutable
    BufferedImage waitUntilScreenshotMatchesImage(
            Component component,
            BufferedImage... expectedImages);

    /**
     * Returns the value of the {@code generateSnapshotIfMissing} property.
     *
     * @return the value of the {@code generateSnapshotIfMissing} property
     * @see #waitUntilScreenshotMatchesSnapshot(Component, Rectangle, String)
     */
    boolean getGenerateSnapshotIfMissing();

    /**
     * Sets the {@code generateSnapshotIfMissing} property to the {@code value}.
     *
     * @param value the value to set the {@code generateSnapshotIfMissing} property to
     * @see #waitUntilScreenshotMatchesSnapshot(Component, Rectangle, String)
     */
    void setGenerateSnapshotIfMissing(boolean value);

    /**
     * Returns the value of the {@code delayBeforeNewSnapshot} property.
     *
     * @return the value of the {@code delayBeforeNewSnapshot} property
     * @see #waitUntilScreenshotMatchesSnapshot(Component, Rectangle, String)
     */
    Duration getDelayBeforeNewSnapshot();

    /**
     * Sets the {@code delayBeforeNewSnapshot} property to the {@code duration}.
     *
     * @param duration the value to set the {@code delayBeforeNewSnapshot} property to
     * @see #waitUntilScreenshotMatchesSnapshot(Component, Rectangle, String)
     */
    void setDelayBeforeNewSnapshot(Duration duration);

    /**
     * Returns the path to the test resources directory, relative to the
     * Maven project directory.
     *
     * @return the path to the test resources directory
     */
    String getTestResourcesDirectoryPath();

    /**
     * Sets the path to the test resources directory to {@code path}.

     * @param path the new path to the test resources directory (relative to the
     * Maven project directory)
     */
    void setTestResourcesDirectoryPath(String path);

    /**
     * Returns the absolute name of the snapshot the will be created in that
     * current context, taking `name` and the calling method(s) into account.
     *
     * <p>
     * <em>(When {@code name} is {@code null} assume {@code name}
     * is {@code "snapshot"}.)</em>
     * <ul>
     *     <li>
     *          A {@code name} starting with {@code '/'} defines an absolute
     *          name and is returned "as is", otherwise
     *     </li>
     *     <li>
     *         when (directly or indirectly) called from a JUnit 5 {@code @Test}
     *         or {@code @ParameterizedTest} method {@code testMethodName} in a
     *         class {@code a.b.c.TestClass} the snapshot name is
     *         {@code "/a/b/c/TestClass.testMethodName-%s"}, with {@code name}
     *         as {@code %s}, otherwise
     *     </li>
     *     <li>
     *         when directly called from a method {@code methodName} in a
     *         class {@code a.b.c.CallingClass} the snapshot name is
     *         {@code "/a/b/c/CallingClass.methodName-%s"}, with {@code name}
     *         as {@code %s}.
     *     </li>
     * </ul>
     *
     * @param name the (relative or absolute) name of the snapshot, or {@code null}
     * @return the absolute name of the snapshot
     */
    String getSnapshotName(@Nullable String name);

    /**
     * Returns the images of the snapshot with the given name.
     *
     * @param name the name of the snapshot
     * @return the images of the snapshot with the given name
     */
    BufferedImage[] getImagesOfSnapshot(String name);

    /**
     * Returns the images of the snapshot {@link ScreenCaptureSupport#SNAPSHOT_NAME_DEFAULT}
     *
     * @return the images of the snapshot {@link ScreenCaptureSupport#SNAPSHOT_NAME_DEFAULT}
     */
    default BufferedImage[] getImagesOfSnapshot() {
        return getImagesOfSnapshot(SNAPSHOT_NAME_DEFAULT);
    }


    /**
     * Waits until the screenshot of the {@code component}, or of the
     * {@code rectangle} of the {@code component}, if {@code rectangle} is not {@code null},
     * matches one of the images defined for the snapshot with the given
     * {@code snapshotName}, and returns the image.
     *
     * <p>When the snapshot does not yet have images the behaviour depends on
     * the property {@code generateSnapshotIfMissing}
     * (see {@link #getGenerateSnapshotIfMissing()}):</p>
     * <ul>
     *     <li>{@code generateSnapshotIfMissing == true}: after a delay of {@link #getDelayBeforeNewSnapshot()}
     *     a screenshot of the given area is stored as the first image of this
     *     snapshot and the method returns normally.</li>
     *     <li>{@code generateSnapshotIfMissing == false}: the method throws
     *     a {@link GuiTestingException}</li>
     * </ul>
     *
     * <p>When the snapshot has images but the given area does not match any
     * of these images, even after the duration defined by {@link #timeout()},
     * the method throws a {@link GuiTestingException}.
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
     * @return the screenshot image
     */
    @Timeoutable
    BufferedImage waitUntilScreenshotMatchesSnapshot(
            Component component,
            @Nullable Rectangle rectangle,
            String snapshotName)
            throws GuiTestingException;

    /**
     * Waits until the screenshot of the {@code component}, or of the
     * {@code rectangle} of the {@code component}, if {@code rectangle} is not {@code null},
     * matches one of the images defined for the snapshot named
     * {@link ScreenCaptureSupport#SNAPSHOT_NAME_DEFAULT},
     * and returns the image.
     *
     *
     * <p>When the snapshot does not yet have images the behaviour depends on
     * the property {@code generateSnapshotIfMissing}
     * (see {@link #getGenerateSnapshotIfMissing()}):</p>
     * <ul>
     *     <li>{@code generateSnapshotIfMissing == true}: after a delay of {@link #getDelayBeforeNewSnapshot()}
     *     a screenshot of the given area is stored as the first image of this
     *     snapshot and the method returns normally.</li>
     *     <li>{@code generateSnapshotIfMissing == false}: the method throws
     *     a {@link GuiTestingException}</li>
     * </ul>
     *
     * <p>When the snapshot has images but the given area does not match any
     * of these images, even after the duration defined by {@link #timeout()},
     * the method throws a {@link GuiTestingException}.
     *
     * <p>See also {@link #waitUntilScreenshotMatchesSnapshot(Component, Rectangle, String)}</p>
     *
     * @param component the {@link Component} to compare with the images of
     *                  the snapshot
     * @param rectangle the area of the component (in coordinates relative
     *                  to the component) to compare with the images of the
     *                  snapshot.
     *                  When {@code null} the full component is compared.
     * @return the screenshot image
     */
    @Timeoutable
    default BufferedImage waitUntilScreenshotMatchesSnapshot(
            Component component,
            @Nullable Rectangle rectangle)
            throws GuiTestingException {
        return waitUntilScreenshotMatchesSnapshot(component, rectangle, SNAPSHOT_NAME_DEFAULT);
    }

    /**
     * Waits until the screenshot of the {@code component}
     * matches one of the images defined for
     * the snapshot with the given {@code snapshotName}, and returns the image.
     *
     * <p>When the snapshot does not yet have images the behaviour depends on
     * the property {@code generateSnapshotIfMissing}
     * (see {@link #getGenerateSnapshotIfMissing()}):</p>
     * <ul>
     *     <li>{@code generateSnapshotIfMissing == true}: after a delay of {@link #getDelayBeforeNewSnapshot()}
     *     a screenshot of the {@code component} is stored as the first image
     *     of this snapshot and the method returns normally.</li>
     *     <li>{@code generateSnapshotIfMissing == false}: the method throws
     *     a {@link GuiTestingException}</li>
     * </ul>
     *
     * <p>When the snapshot has images but the {@code component} does not match
     * any of these images, even after the duration defined by
     * {@link #timeout()}, the method throws a {@link GuiTestingException}.
     *
     * @param component    the {@link Component} to compare with the images of
     *                     the snapshot
     * @param snapshotName a Java identifier to identify the snapshot. The
     *                     snapshotName must be unique within the method calling
     *                     this method.
     * @return the screenshot image
     */
    @Timeoutable
    default BufferedImage waitUntilScreenshotMatchesSnapshot(
            Component component, String snapshotName)
            throws GuiTestingException {
        return waitUntilScreenshotMatchesSnapshot(component, null, snapshotName);
    }

    /**
     * Waits until the screenshot of the {@code component}
     * matches one of the images defined for
     * the snapshot  named {@link ScreenCaptureSupport#SNAPSHOT_NAME_DEFAULT},
     * and returns the image.
     *
     * <p>When the snapshot does not yet have images the behaviour depends on
     * the property {@code generateSnapshotIfMissing}
     * (see {@link #getGenerateSnapshotIfMissing()}):</p>
     * <ul>
     *     <li>{@code generateSnapshotIfMissing == true}: after a delay of {@link #getDelayBeforeNewSnapshot()}
     *     a screenshot of the {@code component} is stored as the first image
     *     of this snapshot and the method returns normally.</li>
     *     <li>{@code generateSnapshotIfMissing == false}: the method throws
     *     a {@link GuiTestingException}</li>
     * </ul>
     *
     * <p>When the snapshot has images but the {@code component} does not match
     * any of these images, even after the duration defined by
     * {@link #timeout()}, the method throws a {@link GuiTestingException}.
     *
     * @param component the {@link Component} to compare with the images of
     *                  the snapshot
     * @return the screenshot image
     */
    @Timeoutable
    default BufferedImage waitUntilScreenshotMatchesSnapshot(Component component)
            throws GuiTestingException {
        return waitUntilScreenshotMatchesSnapshot(component, SNAPSHOT_NAME_DEFAULT);
    }

    /**
     * The difference between two {@link Image}s ({@code imageA} and {@code imageB}).
     */
    interface ImageDifference {

        /**
         * Returns {@code true} when the images don't match, i.e. there are
         * differences between the images, {@code false} otherwise.
         *
         * @return {@code true} when the images don't match, i.e. there are
         * differences between the images, {@code false} otherwise
         */

        boolean imagesAreDifferent();

        /**
         * Returns the first image of the comparison.
         *
         * @return the first image of the comparison
         */
        BufferedImage getImageA();

        /**
         * Returns the second image of the comparison.
         *
         * @return the second image of the comparison
         */
        BufferedImage getImageB();

        /**
         * Returns an image marking the differences between imageA and imageB
         * with {@link Color#black} pixels on a (transparent) white canvas.
         *
         * <p>The images are compared pixel by pixel and all pixel that are not
         * similar or that only exist in one image are marked {@link Color#black}.</p>
         *
         * @return an image marking the differences between imageA and imageB
         * with {@link Color#black} pixels on a (transparent) white canvas
         */
        BufferedImage getDifferenceMask();
    }

}
