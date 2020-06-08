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

import org.eclipse.jdt.annotation.Nullable;

import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;

import static org.abego.commons.swing.JTextComponentUtil.modelToView;
import static org.abego.commons.swing.RectangleUtil.center;

/**
 * Operations with the mouse, like moving it, clicking, dragging, or
 * the features of {@link BasicMouseSupport}.
 */
public interface MouseSupport extends BasicMouseSupport {

    /**
     * Moves the mouse pointer to the given {@code position}.
     *
     * @param position the location to move the mouse pointer to (in screen coordinates)
     */
    default void mouseMove(Point position) {
        mouseMove(position.x, position.y);
    }

    /**
     * Clicks {@code clickCount} times with the buttons defined by
     * {@code buttonsMask} at {@code (x,y)}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     * @param x           the x coordinate of the location to click at, in screen coordinates.
     *                    If &lt; 0 offset is taken from the right of the component
     * @param y           the y coordinate of the location to click at, in screen coordinates.
     *                    if &lt; 0 offset is taken from the bottom of the component
     * @param clickCount  [clickCount &gt; 0; default=1] the number of clicks
     */
    void click(int buttonsMask, int x, int y, int clickCount);

    /**
     * Clicks {@code clickCount} times with the buttons defined by
     * {@code buttonsMask} at {@code position}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     * @param position    the position to click at, in screen coordinates.
     * @param clickCount  [clickCount &gt; 0; default=1] the number of clicks
     */
    default void click(int buttonsMask, Point position, int clickCount) {
        click(buttonsMask, position.x, position.y, clickCount);
    }

    /**
     * Left clicks {@code clickCount} times at {@code (x,y)}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param x          the x coordinate of the location to click at, in screen coordinates.
     *                   If &lt; 0 offset is taken from the right of the component
     * @param y          the y coordinate of the location to click at, in screen coordinates.
     *                   if &lt; 0 offset is taken from the bottom of the component
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickLeft(int x, int y, int clickCount) {
        click(InputEvent.BUTTON1_DOWN_MASK, x, y, clickCount);
    }

    /**
     * Left clicks {@code clickCount} times at {@code position}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param position   the position to click at, in screen coordinates.
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickLeft(Point position, int clickCount) {
        clickLeft(position.x, position.y, clickCount);
    }

    /**
     * Left clicks at {@code (x,y)}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param x the x coordinate of the location to click at, in screen coordinates.
     *          If &lt; 0 offset is taken from the right of the component
     * @param y the y coordinate of the location to click at, in screen coordinates.
     *          if &lt; 0 offset is taken from the bottom of the component
     */
    default void clickLeft(int x, int y) {
        click(InputEvent.BUTTON1_DOWN_MASK, x, y, 1);
    }

    /**
     * Left clicks at {@code position}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param position the position to click at, in screen coordinates.
     */
    default void clickLeft(Point position) {
        clickLeft(position.x, position.y);
    }

    /**
     * Right clicks {@code clickCount} times at {@code (x,y)}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param x          the x coordinate of the location to click at, in screen coordinates.
     *                   If &lt; 0 offset is taken from the right of the component
     * @param y          the y coordinate of the location to click at, in screen coordinates.
     *                   if &lt; 0 offset is taken from the bottom of the component
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickRight(int x, int y, int clickCount) {
        click(InputEvent.BUTTON3_DOWN_MASK, x, y, clickCount);
    }

    /**
     * Right clicks {@code clickCount} times at {@code position}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param position   the position to click at, in screen coordinates.
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickRight(Point position, int clickCount) {
        clickRight(position.x, position.y, clickCount);
    }

    /**
     * Right clicks at {@code (x,y)}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param x the x coordinate of the location to click at, in screen coordinates.
     *          If &lt; 0 offset is taken from the right of the component
     * @param y the y coordinate of the location to click at, in screen coordinates.
     *          if &lt; 0 offset is taken from the bottom of the component
     */
    default void clickRight(int x, int y) {
        click(InputEvent.BUTTON3_DOWN_MASK, x, y, 1);
    }

    /**
     * Right clicks at {@code position}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param position the position to click at, in screen coordinates.
     */
    default void clickRight(Point position) {
        clickRight(position.x, position.y);
    }

    /**
     * Clicks {@code clickCount} times with the buttons defined by
     * {@code buttonsMask} at {@code (x,y)}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     * @param component   the {@link Component} used as reference point
     * @param x           the x coordinate of the location to click at, relative to the given {@code component}.
     *                    If &lt; 0 offset is taken from the right of the component
     * @param y           the y coordinate of the location to click at, relative to the given {@code component}.
     *                    if &lt; 0 offset is taken from the bottom of the component
     * @param clickCount  [clickCount &gt; 0; default=1] the number of clicks
     */
    void click(int buttonsMask, Component component, int x, int y, int clickCount);

    /**
     * Clicks {@code clickCount} times with the buttons defined by
     * {@code buttonsMask} at {@code position}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     * @param component   the {@link Component} used as reference point
     * @param position    the position to click at, relative to the given {@code component}.
     * @param clickCount  [clickCount &gt; 0; default=1] the number of clicks
     */
    default void click(int buttonsMask, Component component, Point position, int clickCount) {
        click(buttonsMask, component, position.x, position.y, clickCount);
    }

    /**
     * Left clicks {@code clickCount} times at {@code (x,y)}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param component  the {@link Component} used as reference point
     * @param x          the x coordinate of the location to click at, relative to the given {@code component}.
     *                   If &lt; 0 offset is taken from the right of the component
     * @param y          the y coordinate of the location to click at, relative to the given {@code component}.
     *                   if &lt; 0 offset is taken from the bottom of the component
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickLeft(Component component, int x, int y, int clickCount) {
        click(InputEvent.BUTTON1_DOWN_MASK, component, x, y, clickCount);
    }

    /**
     * Left clicks {@code clickCount} times at {@code position}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param component  the {@link Component} used as reference point
     * @param position   the position to click at, relative to the given {@code component}.
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickLeft(Component component, Point position, int clickCount) {
        clickLeft(component, position.x, position.y, clickCount);
    }

    /**
     * Left clicks at {@code (x,y)}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param component the {@link Component} used as reference point
     * @param x         the x coordinate of the location to click at, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the right of the component
     * @param y         the y coordinate of the location to click at, relative to the given {@code component}.
     *                  if &lt; 0 offset is taken from the bottom of the component
     */
    default void clickLeft(Component component, int x, int y) {
        click(InputEvent.BUTTON1_DOWN_MASK, component, x, y, 1);
    }

    /**
     * Left clicks at {@code position}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param component the {@link Component} used as reference point
     * @param position  the position to click at, relative to the given {@code component}.
     */
    default void clickLeft(Component component, Point position) {
        clickLeft(component, position.x, position.y);
    }

    /**
     * Left clicks {@code clickCount} times into the center of the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param component  the {@link Component} to click into
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickLeft(Component component, int clickCount) {
        click(InputEvent.BUTTON1_DOWN_MASK, component, component.getWidth() / 2, component.getHeight() / 2, clickCount);
    }

    /**
     * Left clicks into the center of the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param component the {@link Component} to click into
     */
    default void clickLeft(Component component) {
        click(InputEvent.BUTTON1_DOWN_MASK, component, component.getWidth() / 2, component.getHeight() / 2, 1);
    }

    /**
     * Right clicks {@code clickCount} times at {@code (x,y)}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param component  the {@link Component} used as reference point
     * @param x          the x coordinate of the location to click at, relative to the given {@code component}.
     *                   If &lt; 0 offset is taken from the right of the component
     * @param y          the y coordinate of the location to click at, relative to the given {@code component}.
     *                   if &lt; 0 offset is taken from the bottom of the component
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickRight(Component component, int x, int y, int clickCount) {
        click(InputEvent.BUTTON3_DOWN_MASK, component, x, y, clickCount);
    }

    /**
     * Right clicks {@code clickCount} times at {@code position}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param component  the {@link Component} used as reference point
     * @param position   the position to click at, relative to the given {@code component}.
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickRight(Component component, Point position, int clickCount) {
        clickRight(component, position.x, position.y, clickCount);
    }

    /**
     * Right clicks at {@code (x,y)}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param component the {@link Component} used as reference point
     * @param x         the x coordinate of the location to click at, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the right of the component
     * @param y         the y coordinate of the location to click at, relative to the given {@code component}.
     *                  if &lt; 0 offset is taken from the bottom of the component
     */
    default void clickRight(Component component, int x, int y) {
        click(InputEvent.BUTTON3_DOWN_MASK, component, x, y, 1);
    }

    /**
     * Right clicks at {@code position}, relative to the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param component the {@link Component} used as reference point
     * @param position  the position to click at, relative to the given {@code component}.
     */
    default void clickRight(Component component, Point position) {
        clickRight(component, position.x, position.y);
    }

    /**
     * Right clicks {@code clickCount} times into the center of the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the {@code clickCount} parameter instead.
     *
     * @param component  the {@link Component} to click into
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    default void clickRight(Component component, int clickCount) {
        click(InputEvent.BUTTON3_DOWN_MASK, component, component.getWidth() / 2, component.getHeight() / 2, clickCount);
    }

    /**
     * Right clicks into the center of the given {@code component}.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the method with the extra {@code clickCount} parameter instead.
     *
     * @param component the {@link Component} to click into
     */
    default void clickRight(Component component) {
        click(InputEvent.BUTTON3_DOWN_MASK, component, component.getWidth() / 2, component.getHeight() / 2, 1);
    }

    /**
     * Left clicks before the index-th character of the textComponent.
     * <p>
     * A negative index refers to a position from the end of the text,
     * i.e. -1 behind the last character, -2 behind the second last character etc.
     *
     * @param textComponent the {@link JTextComponent} to click into
     * @param index         the index of the character in the textComponent to click in front of.
     */
    default void clickCharacterAtIndex(JTextComponent textComponent, int index) {
        // negative index refers to an index from the end.
        if (index < 0) {
            index = textComponent.getText().length() + index + 1;
        }
        @Nullable Rectangle r = modelToView(textComponent, index);
        if (r == null)
            return;
        clickLeft(textComponent, center(r));
    }

    /**
     * Drags the mouse with the buttons defined by {@code buttonsMask}
     * from {@code (x1,y1)} to {@code (x2,y2)}, in screen coordinates.
     * <p>
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     * @param x1          the x coordinate of the start location of the drag operation, in screen coordinates.
     *                    If &lt; 0 offset is taken from the right of the screen
     * @param y1          the y coordinate of the start location of the drag operation, in screen coordinates.
     *                    If &lt; 0 offset is taken from the bottom of the screen
     * @param x2          the x coordinate of the end location of the drag operation, in screen coordinates.
     *                    If &lt; 0 offset is taken from the right of the screen
     * @param y2          the y coordinate of the end location of the drag operation, in screen coordinates.
     *                    If &lt; 0 offset is taken from the bottom of the screen
     */
    void drag(int buttonsMask, int x1, int y1, int x2, int y2);

    /**
     * Drags the mouse with the buttons defined by {@code buttonsMask}
     * from {@code from} to {@code to}, in screen coordinates.
     * <p>
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     * @param from        the start location of the drag operation, in screen coordinates.
     * @param to          the end location of the drag operation, in screen coordinates.
     */
    default void drag(int buttonsMask, Point from, Point to) {
        drag(buttonsMask, from.x, from.y, to.x, to.y);
    }

    /**
     * Drags the mouse with the buttons defined by {@code buttonsMask}
     * from {@code (x1,y1)} to {@code (x2,y2)}, relative to the given {@code component}.
     * <p>
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     * @param component   the {@link Component} used as reference point
     * @param x1          the x coordinate of the start location of the drag operation, relative to the given {@code component}.
     *                    If &lt; 0 offset is taken from the right of the screen
     * @param y1          the y coordinate of the start location of the drag operation, relative to the given {@code component}.
     *                    If &lt; 0 offset is taken from the bottom of the screen
     * @param x2          the x coordinate of the end location of the drag operation, relative to the given {@code component}.
     *                    If &lt; 0 offset is taken from the right of the screen
     * @param y2          the y coordinate of the end location of the drag operation, relative to the given {@code component}.
     *                    If &lt; 0 offset is taken from the bottom of the screen
     */
    void drag(int buttonsMask, Component component, int x1, int y1, int x2, int y2);

    /**
     * Drags the mouse with the buttons defined by {@code buttonsMask}
     * from {@code from} to {@code to}, relative to the given {@code component}.
     * <p>
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     * @param component   the {@link Component} used as reference point
     * @param from        the start location of the drag operation, relative to the given {@code component}.
     * @param to          the end location of the drag operation, relative to the given {@code component}.
     */
    default void drag(int buttonsMask, Component component, Point from, Point to) {
        drag(buttonsMask, component, from.x, from.y, to.x, to.y);
    }

    /**
     * Left drags the mouse
     * from {@code (x1,y1)} to {@code (x2,y2)}, in screen coordinates.
     * <p>
     *
     * @param x1 the x coordinate of the start location of the drag operation, in screen coordinates.
     *           If &lt; 0 offset is taken from the right of the screen
     * @param y1 the y coordinate of the start location of the drag operation, in screen coordinates.
     *           If &lt; 0 offset is taken from the bottom of the screen
     * @param x2 the x coordinate of the end location of the drag operation, in screen coordinates.
     *           If &lt; 0 offset is taken from the right of the screen
     * @param y2 the y coordinate of the end location of the drag operation, in screen coordinates.
     *           If &lt; 0 offset is taken from the bottom of the screen
     */
    default void dragLeft(int x1, int y1, int x2, int y2) {
        drag(InputEvent.BUTTON1_DOWN_MASK, x1, y1, x2, y2);
    }

    /**
     * Left drags the mouse
     * from {@code from} to {@code to}, in screen coordinates.
     * <p>
     *
     * @param from the start location of the drag operation, in screen coordinates.
     * @param to   the end location of the drag operation, in screen coordinates.
     */
    default void dragLeft(Point from, Point to) {
        dragLeft(from.x, from.y, to.x, to.y);
    }

    /**
     * Right drags the mouse
     * from {@code (x1,y1)} to {@code (x2,y2)}, in screen coordinates.
     * <p>
     *
     * @param x1 the x coordinate of the start location of the drag operation, in screen coordinates.
     *           If &lt; 0 offset is taken from the right of the screen
     * @param y1 the y coordinate of the start location of the drag operation, in screen coordinates.
     *           If &lt; 0 offset is taken from the bottom of the screen
     * @param x2 the x coordinate of the end location of the drag operation, in screen coordinates.
     *           If &lt; 0 offset is taken from the right of the screen
     * @param y2 the y coordinate of the end location of the drag operation, in screen coordinates.
     *           If &lt; 0 offset is taken from the bottom of the screen
     */
    default void dragRight(int x1, int y1, int x2, int y2) {
        drag(InputEvent.BUTTON3_DOWN_MASK, x1, y1, x2, y2);
    }

    /**
     * Right drags the mouse
     * from {@code from} to {@code to}, in screen coordinates.
     * <p>
     *
     * @param from the start location of the drag operation, in screen coordinates.
     * @param to   the end location of the drag operation, in screen coordinates.
     */
    default void dragRight(Point from, Point to) {
        dragRight(from.x, from.y, to.x, to.y);
    }

    /**
     * Left drags the mouse
     * from {@code (x1,y1)} to {@code (x2,y2)}, relative to the given {@code component}.
     * <p>
     *
     * @param component the {@link Component} used as reference point
     * @param x1        the x coordinate of the start location of the drag operation, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the right of the screen
     * @param y1        the y coordinate of the start location of the drag operation, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the bottom of the screen
     * @param x2        the x coordinate of the end location of the drag operation, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the right of the screen
     * @param y2        the y coordinate of the end location of the drag operation, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the bottom of the screen
     */
    default void dragLeft(Component component, int x1, int y1, int x2, int y2) {
        drag(InputEvent.BUTTON1_DOWN_MASK, component, x1, y1, x2, y2);
    }

    /**
     * Left drags the mouse
     * from {@code from} to {@code to}, relative to the given {@code component}.
     * <p>
     *
     * @param component the {@link Component} used as reference point
     * @param from      the start location of the drag operation, relative to the given {@code component}.
     * @param to        the end location of the drag operation, relative to the given {@code component}.
     */
    default void dragLeft(Component component, Point from, Point to) {
        dragLeft(component, from.x, from.y, to.x, to.y);
    }

    /**
     * Right drags the mouse
     * from {@code (x1,y1)} to {@code (x2,y2)}, relative to the given {@code component}.
     * <p>
     *
     * @param component the {@link Component} used as reference point
     * @param x1        the x coordinate of the start location of the drag operation, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the right of the screen
     * @param y1        the y coordinate of the start location of the drag operation, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the bottom of the screen
     * @param x2        the x coordinate of the end location of the drag operation, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the right of the screen
     * @param y2        the y coordinate of the end location of the drag operation, relative to the given {@code component}.
     *                  If &lt; 0 offset is taken from the bottom of the screen
     */
    default void dragRight(Component component, int x1, int y1, int x2, int y2) {
        drag(InputEvent.BUTTON3_DOWN_MASK, component, x1, y1, x2, y2);
    }

    /**
     * Right drags the mouse
     * from {@code from} to {@code to}, relative to the given {@code component}.
     * <p>
     *
     * @param component the {@link Component} used as reference point
     * @param from      the start location of the drag operation, relative to the given {@code component}.
     * @param to        the end location of the drag operation, relative to the given {@code component}.
     */
    default void dragRight(Component component, Point from, Point to) {
        dragRight(component, from.x, from.y, to.x, to.y);
    }

}