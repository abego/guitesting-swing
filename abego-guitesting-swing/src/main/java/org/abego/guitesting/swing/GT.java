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

import org.abego.commons.blackboard.Blackboard;
import org.abego.commons.seq.Seq;
import org.abego.commons.test.AssertRetryingService;
import org.abego.commons.timeout.TimeoutService;
import org.abego.commons.timeout.Timeoutable;
import org.opentest4j.AssertionFailedError;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.awt.Component;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Predicate;

import static org.abego.commons.lang.ThrowableUtil.messageOrClassName;

/**
 * GT, the main interface for GUI testing.
 *
 * <p>
 * The GT interface provides access to most features of the GUITesting Swing
 * library. The interface covers many areas related to GUI testing,
 * like dealing with windows or components, using input devices like
 * keyboard or mouse, checking test results with GUI specific
 * "assert..." methods and many more.
 * <p>
 * A typical code snippet using GT may look like this:
 * <pre>
 * import static org.abego.guitesting.swing.GuiTesting.newGT;
 *
 * ...
 *
 * // A GT instance is the main thing we need when testing GUI code.
 * GT gt = newGT();
 *
 * // run some application code that opens a window
 * openSampleWindow();
 *
 * // In that window we are interested in a JTextField named "input"
 * JTextField input = gt.waitForComponentNamed(JTextField.class, "input");
 *
 * // we move the focus to that input field and type "Your name" ", please!"
 * gt.setFocusOwner(input);
 * gt.type("Your name");
 * gt.type(", please!");
 *
 * // Verify if the text field really contains the expected text.
 * gt.assertEqualsRetrying("Your name, please!", input::getText);
 *
 * // When we are done with our tests we can ask GT to cleanup
 * // (This will dispose open windows etc.)
 * gt.cleanup();
 * </pre>
 * <p>
 * <b>Feature Areas and "...Support" interfaces</b>
 * <p>
 * GT is a quite large API with many methods for different GUI testing areas.
 * To better manage this large feature set we have different
 * "...Support" interfaces for each area, like {@link KeyboardSupport} or
 * {@link WindowSupport}. GT gives access to these feature areas through
 * methods starting with an underscore, like {@link #_keyboard()} or
 * {@link #_window()}, e.g. with code like this:
 * <pre>
 * gt._keyboard().type("hello world");
 * </pre>
 * or
 * <pre>
 * gt._window().waitForWindowNamed("preferences");
 * </pre>
 * As GT also inherits from these feature area interfaces you may also access
 * the methods directly, e.g. with code like this:
 * <pre>
 * gt.type("hello world");
 * </pre>
 * or
 * <pre>
 * gt.waitForWindowNamed("preferences");
 * </pre>
 */
public interface GT extends
        AssertRetryingService,
        ComponentSupport,
        DebugSupport,
        DialogAndFrameSupport,
        EDTSupport,
        FocusSupport,
        KeyboardSupport,
        MouseSupport,
        PollingSupport,
        RobotAPI,
        ScreenCaptureSupport,
        TimeoutService,
        WaitForIdleSupport,
        WaitSupport,
        WindowSupport {

    /**
     * see {@link #readSystemProperties()}.
     */
    String SYSTEM_PROPERTY_TIMEOUT_MILLIS = "abego-guitesting-swing.timeoutmillis"; //NON-NLS

    // ======================================================================
    // Blackboard
    // ======================================================================

    /**
     * Returns the default {@link Blackboard}.
     *
     * @return the default {@link Blackboard}
     */
    Blackboard<Object> blackboard();

    // ======================================================================
    // "waitFor..." Window Support
    // ======================================================================

    @Override
    @Timeoutable
    default <T extends Window> T waitForWindowWith(Class<T> windowClass, Predicate<T> condition) {
        Seq<T> windows = poll(
                () -> allWindowsWith(windowClass, condition),
                w -> !w.isEmpty());
        return windows.singleItem();
    }

    @Override
    @Timeoutable
    default Window waitForWindowWith(Predicate<Window> condition) {
        return waitForWindowWith(Window.class, condition);
    }

    @Override
    @Timeoutable
    default <T extends Window> T waitForWindowNamed(Class<T> windowClass, String name) {
        return waitForWindowWith(
                windowClass, w -> Objects.equals(w.getName(), name));
    }

    @Override
    @Timeoutable
    default Window waitForWindowNamed(String name) {
        return waitForWindowNamed(Window.class, name);
    }

    // ======================================================================
    // "waitFor..." Component Support
    // ======================================================================

    @Override
    @Timeoutable
    default <T extends Component> T waitForComponent(Class<T> componentClass) {
        return waitForComponentWith(componentClass, c -> true);
    }

    @Override
    @Timeoutable
    default <T extends Component> T waitForComponentWith(Class<T> componentClass,
                                                         Predicate<T> condition) {
        Seq<T> seq = poll(
                () -> allComponentsWith(componentClass, condition),
                a -> !a.isEmpty());
        return seq.singleItem();
    }

    @Override
    @Timeoutable
    default <T extends Component> T waitForComponentNamed(Class<T> componentClass, String name) {
        try {
            return waitForComponentWith(
                    componentClass,
                    c -> Objects.equals(c.getName(), name));
        } catch (Exception e) {
            throw new AssertionFailedError(String.format(
                    "Error when looking for %s named '%s': %s", //NON-NLS
                    componentClass.getName(),
                    name,
                    messageOrClassName(e)),
                    e);
        }
    }


    /**
     * Waits until the screenshot of the Popup Menu associated with
     * the {@code menu} matches one of the images defined for
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
     * @param menu         the {@link Component} to compare with the images of
     *                     the snapshot
     * @param snapshotName the name of the snapshot.
     * @return the screenshot image
     */
    @Timeoutable
    BufferedImage waitUntilPopupMenuScreenshotMatchesSnapshot(
            JMenu menu, String snapshotName);

    @Timeoutable
    default BufferedImage waitUntilPopupMenuScreenshotMatchesSnapshot(
            JMenu menu) {
        return waitUntilPopupMenuScreenshotMatchesSnapshot(
                menu, getSnapshotName(null));
    }

    /**
     * Waits until the screenshots of all components related to the given
     * {@code menubar} matches one of the images defined for
     * the snapshot with the given {@code snapshotName}.
     *
     * <p>Screenshots for the menubar, the menubar with visible accelerators,
     * each popup menu of each menu in the menubar as well as their sub menus
     * (recursively) are considered.</p>
     *
     * @param menubar      the {@link JMenuBar} to check
     * @param snapshotName the name of the snapshot.
     */
    @Timeoutable
    void waitUntilAllMenuRelatedScreenshotsMatchSnapshot(
            JMenuBar menubar, String snapshotName);

    /**
     * Waits until the screenshots of all components related to the given
     * {@code menubar} matches one of the images defined for the default
     * snapshot.
     *
     * <p>For details see
     * {@link #waitUntilAllMenuRelatedScreenshotsMatchSnapshot(JMenuBar, String)}</p>
     *
     * @param menubar the {@link JMenuBar} to check
     */
    @Timeoutable
    default void waitUntilAllMenuRelatedScreenshotsMatchSnapshot(
            JMenuBar menubar) {
        waitUntilAllMenuRelatedScreenshotsMatchSnapshot(
                menubar, getSnapshotName(null));
    }

    SnapshotReviewService newSnapshotReviewService();

    // ======================================================================
    // Reset
    // ======================================================================

    /**
     * Reset this GT instance.
     * <p>
     * I.e.:
     * <ul>
     * <li>Reset timeout to the {@link #initialTimeout()}.</li>
     * <li>Clear the blackboard (see {@link Blackboard#clear()}).</li>
     * <li>"Release" all keys (in case a key was pressed and
     * not yet released).</li>
     * </ul>
     */
    void reset();

    /**
     * Reset this GT instance and dispose all windows.
     *
     * <p>
     * See also {@link #reset()}
     */
    void cleanup();

    /**
     * Reads the system properties and applies them to this GT.
     *
     * <p>The following properties are supported:
     * <ul>
     *     <li>{@link #SYSTEM_PROPERTY_TIMEOUT_MILLIS}: time duration in milliseconds, used for
     *     initialTimeout and timeout.</li>
     * </ul>
     */
    void readSystemProperties();

    // ======================================================================
    // Feature group access
    // ======================================================================

    /**
     * Provides access to "assert...Retrying" methods.
     *
     * @return this object as AssertRetryingService
     */
    default AssertRetryingService _assert() {
        return this;
    }

    /**
     * Provides access to component related methods
     *
     * @return this object as ComponentSupport.
     */
    default ComponentSupport _component() {
        return this;
    }

    /**
     * Provides access to methods for GUI test debugging.
     *
     * @return this object as DebugSupport
     */
    default DebugSupport _debug() {
        return this;
    }

    /**
     * Provides access to Dialog related methods.
     *
     * @return this object as DialogAndFrameSupport
     */
    default DialogAndFrameSupport _dialog() {
        return this;
    }

    /**
     * Provides access to Event Dispatch Thread (EDT) related methods.
     *
     * @return this object as EDTSupport
     */
    @SuppressWarnings("GrazieInspection")
    default EDTSupport _edt() {
        return this;
    }

    /**
     * Provides access to (keyboard) focus related methods.
     *
     * @return this object as FocusSupport
     */
    default FocusSupport _focus() {
        return this;
    }

    /**
     * Provides access to {@link javax.swing.JFrame} related methods.
     *
     * @return this object as DialogAndFrameSupport
     */
    default DialogAndFrameSupport _frame() {
        return this;
    }

    /**
     * Provides access to keyboard related methods.
     *
     * @return this object as KeyboardSupport
     */
    default KeyboardSupport _keyboard() {
        return this;
    }

    /**
     * Provides access to mouse related methods.
     *
     * @return this object as MouseSupport
     */
    default MouseSupport _mouse() {
        return this;
    }

    /**
     * Provides access to "polling" methods.
     *
     * @return this object as PollingSupport
     */
    default PollingSupport _poll() {
        return this;
    }

    /**
     * Provides access to a {@link java.awt.Robot}-like API .
     *
     * @return this object as RobotAPI
     */
    default RobotAPI _robotAPI() {
        return this;
    }

    /**
     * Provides access to screen capture related methods.
     *
     * @return this object as ScreenCaptureSupport
     */
    default ScreenCaptureSupport _screenCapture() {
        return this;
    }

    /**
     * Provides access to timeout related methods.
     *
     * @return this object as TimeoutService
     */
    default TimeoutService _timeout() {
        return this;
    }

    /**
     * Provides access to "idle" related methods.
     *
     * @return this object as WaitForIdleSupport
     */
    default WaitForIdleSupport _idle() {
        return this;
    }

    /**
     * Provides access to methods related to "waiting".
     *
     * @return this object as WaitSupport
     */
    default WaitSupport _wait() {
        return this;
    }

    /**
     * Provides access to {@link Window} related methods.
     *
     * @return this object as WindowSupport
     */
    default WindowSupport _window() {
        return this;
    }
}
