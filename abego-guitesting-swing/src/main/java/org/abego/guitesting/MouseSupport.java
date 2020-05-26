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

package org.abego.guitesting;

import static org.abego.commons.swing.JTextComponentUtil.modelToView;
import static org.abego.commons.swing.RectangleUtil.center;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;

import javax.swing.text.JTextComponent;

public interface MouseSupport extends BasicMouseSupport {

    /**
     * Move mouse pointer to the given {@code position} (in screen coordinates).
     */
    default void mouseMove(Point position) {
        mouseMove(position.x, position.y);
    }

    /**
     * Use the buttons defined by <code>buttonsMask</code> to click
     * <code>clickCount</code> times at {@code (x,y)}, in screen coordinates.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the <code>clickCount</code> parameter instead.
     *
     * @param x          if &lt; 0 offset is taken from the right of the component
     * @param y          if &lt; 0 offset is taken from the bottom of the component
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    void click(int buttonsMask, int x, int y, int clickCount);

    default void click(int buttonsMask, Point pos, int clickCount) {
        click(buttonsMask, pos.x, pos.y, clickCount);
    }

    /**
     * Left click <code>clickCount</code> times at {@code (x,y)}, in screen coordinates.
     * <p>
     * See {@link #click(int, int, int, int)}
     */
    default void clickLeft(int x, int y, int clickCount) {
        click(InputEvent.BUTTON1_DOWN_MASK, x, y, clickCount);
    }

    default void clickLeft(Point pos, int clickCount) {
        clickLeft(pos.x, pos.y, clickCount);
    }

    /**
     * Left click at {@code (x,y)}, in screen coordinates.
     * <p>
     * See {@link #click(int, int, int, int)}
     */
    default void clickLeft(int x, int y) {
        click(InputEvent.BUTTON1_DOWN_MASK, x, y, 1);
    }

    default void clickLeft(Point pos) {
        clickLeft(pos.x, pos.y);
    }

    /**
     * Right click <code>clickCount</code> times at {@code (x,y)}, in screen coordinates.
     * <p>
     * See {@link #click(int, int, int, int)}
     */
    default void clickRight(int x, int y, int clickCount) {
        click(InputEvent.BUTTON3_DOWN_MASK, x, y, clickCount);
    }

    default void clickRight(Point pos, int clickCount) {
        clickRight(pos.x, pos.y, clickCount);
    }

    /**
     * Right click at {@code (x,y)}, in screen coordinates.
     * <p>
     * See {@link #click(int, int, int, int)}
     */
    default void clickRight(int x, int y) {
        click(InputEvent.BUTTON3_DOWN_MASK, x, y, 1);
    }

    default void clickRight(Point pos) {
        clickRight(pos.x, pos.y);
    }

    /**
     * Use the buttons defined by <code>buttonsMask</code> to click
     * <code>clickCount</code> times at {@code (x,y)}, relative to the given
     * <code>component</code>.
     * <p>
     * Calling this method more than once with the same coordinates will not
     * generate a "double click", use the <code>clickCount</code> parameter instead.
     *
     * @param x          if &lt; 0 offset is taken from the right of the component
     * @param y          if &lt; 0 offset is taken from the bottom of the component
     * @param clickCount [clickCount &gt; 0; default=1] the number of clicks
     */
    void click(int buttonsMask,
               Component component,
               int x,
               int y,
               int clickCount);

    default void click(int buttonsMask,
                       Component component, Point pos, int clickCount) {
        click(buttonsMask, component, pos.x, pos.y, clickCount);
    }

    /**
     * Left click <code>clickCount</code> times at {@code (x,y)}, relative to
     * the given <code>component</code>.
     * <p>
     * See {@link #click(int, Component, int, int, int)}
     */
    default void clickLeft(Component component, int x, int y, int clickCount) {
        click(InputEvent.BUTTON1_DOWN_MASK, component, x, y, clickCount);
    }

    default void clickLeft(Component component, Point pos, int clickCount) {
        clickLeft(component, pos.x, pos.y, clickCount);
    }

    /**
     * Left click at {@code (x,y)}, relative to the given <code>component</code>.
     * <p>
     * See {@link #click(int, Component, int, int, int)}
     */
    default void clickLeft(Component component, int x, int y) {
        click(InputEvent.BUTTON1_DOWN_MASK, component, x, y, 1);
    }

    default void clickLeft(Component component, Point pos) {
        clickLeft(component, pos.x, pos.y);
    }

    /**
     * Left click <code>clickCount</code> times into the center of the
     * given <code>component</code>.
     * <p>
     * See {@link #click(int, Component, int, int, int)}
     */
    default void clickLeft(Component component, int clickCount) {
        clickLeft(component, component.getWidth() / 2, component.getHeight() / 2, clickCount);
    }

    /**
     * Left click into the center of the given <code>component</code>.
     * <p>
     * See {@link #click(int, Component, int, int, int)}
     */
    default void clickLeft(Component component) {
        clickLeft(component, 1);
    }

    /**
     * Left click before the index-th character of the textComponent.
     * <p>
     * When negative index refers to a position from the end of the text,
     * i.e. -1 behind the last character, -2 behind the second last character etc.
     */
    default void clickLeftAtIndex(JTextComponent textComponent, int index) {
    	// negative index refers to an index from the end.
    	if (index < 0) {
    		index = textComponent.getText().length()+index+1;
    	}
    	Rectangle r = modelToView(textComponent, index);
    	if (r == null) 
    		return;
    	clickLeft(textComponent, center(r));
    }
    
    /**
     * Right click <code>clickCount</code> times at {@code (x,y)}, relative to
     * the given <code>component</code>.
     * <p>
     * See {@link #click(int, Component, int, int, int)}
     */
    default void clickRight(Component component, int x, int y, int clickCount) {
        click(InputEvent.BUTTON3_DOWN_MASK, component, x, y, clickCount);
    }

    default void clickRight(Component component, Point pos, int clickCount) {
        clickRight(component, pos.x, pos.y, clickCount);
    }

    /**
     * Right click at {@code (x,y)}, relative to the given <code>component</code>.
     * <p>
     * See {@link #click(int, Component, int, int, int)}
     */
    default void clickRight(Component component, int x, int y) {
        clickRight(component, x, y, 1);
    }

    default void clickRight(Component component, Point pos) {
        clickRight(component, pos.x, pos.y);
    }

    /**
     * Right click <code>clickCount</code> times into the center of the
     * given <code>component</code>.
     * <p>
     * See {@link #click(int, Component, int, int, int)}
     */
    default void clickRight(Component component, int clickCount) {
        clickRight(component, component.getWidth() / 2, component.getHeight() / 2, clickCount);
    }

    /**
     * Right click into the center of the given <code>component</code>.
     * <p>
     * See {@link #click(int, Component, int, int, int)}
     */
    default void clickRight(Component component) {
        clickRight(component, 1);
    }

    /**
     * Use the buttons defined by <code>buttonsMask</code> to drag the mouse
     * from {@code (x1,y1)} to {@code (x2,y2)}, in screen coordinates.
     * <p>
     *
     * @param x1 if &lt; 0 offset is taken from the right of the screen
     * @param y1 if &lt; 0 offset is taken from the bottom of the screen
     * @param x2 if &lt; 0 offset is taken from the right of the screen
     * @param y2 if &lt; 0 offset is taken from the bottom of the screen
     */
    void drag(int buttonsMask, int x1, int y1, int x2, int y2);

    default void drag(int buttonsMask, Point from, Point to) {
        drag(buttonsMask, from.x, from.y, to.x, to.y);
    }

    /**
     * Use the buttons defined by <code>buttonsMask</code> to drag the mouse
     * from {@code (x1,y1)} to {@code (x2,y2)}, relative to the given
     * <code>component</code>
     * <p>
     *
     * @param x1 if &lt; 0 offset is taken from the right of the component
     * @param y1 if &lt; 0 offset is taken from the bottom of the component
     * @param x2 if &lt; 0 offset is taken from the right of the component
     * @param y2 if &lt; 0 offset is taken from the bottom of the component
     */
    void drag(int buttonsMask, Component component, int x1, int y1, int x2, int y2);

    default void drag(int buttonsMask, Component component, Point from, Point to) {
        drag(buttonsMask, component, from.x, from.y, to.x, to.y);
    }

    /**
     * Left drag the mouse from {@code (x1,y1)} to {@code (x2,y2)}, in screen coordinates.
     * <p>
     * See {@link #drag(int, int, int, int, int)}
     */
    default void dragLeft(int x1, int y1, int x2, int y2) {
        drag(InputEvent.BUTTON1_DOWN_MASK, x1, y1, x2, y2);
    }

    default void dragLeft(Point from, Point to) {
        dragLeft(from.x, from.y, to.x, to.y);
    }

    /**
     * Right drag the mouse from {@code (x1,y1)} to {@code (x2,y2)}, in screen coordinates.
     * <p>
     * See {@link #drag(int, int, int, int, int)}
     */
    default void dragRight(int x1, int y1, int x2, int y2) {
        drag(InputEvent.BUTTON3_DOWN_MASK, x1, y1, x2, y2);
    }

    default void dragRight(Point from, Point to) {
        dragRight(from.x, from.y, to.x, to.y);
    }

    /**
     * Left drag the mouse from {@code (x1,y1)} to {@code (x2,y2)}, relative to
     * the given <code>component</code>
     * <p>
     * See {@link #drag(int, Component, int, int, int, int)}
     */
    default void dragLeft(Component component, int x1, int y1, int x2, int y2) {
        drag(InputEvent.BUTTON1_DOWN_MASK, component, x1, y1, x2, y2);
    }

    default void dragLeft(Component component, Point from, Point to) {
        dragLeft(component, from.x, from.y, to.x, to.y);
    }

    /**
     * Right drag the mouse from {@code (x1,y1)} to {@code (x2,y2)}, relative to
     * the given <code>component</code>
     * <p>
     * See {@link #drag(int, Component, int, int, int, int)}
     */
    default void dragRight(Component component, int x1, int y1, int x2, int y2) {
        drag(InputEvent.BUTTON3_DOWN_MASK, component, x1, y1, x2, y2);
    }

    default void dragRight(Component component, Point from, Point to) {
        dragRight(component, from.x, from.y, to.x, to.y);
    }

}
