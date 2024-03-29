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

import javax.swing.KeyStroke;

/**
 * Operations on the keyboard, like typing text or individual keys.
 */
public interface KeyboardSupport extends BasicKeyboardSupport {

    /**
     * Types the given {@code text}, character by character.
     *
     * @param text the text to type
     */
    void type(String text);

    /**
     * Types the given {@code keyStroke}.
     *
     * @param keyStroke the KeyStroke to type
     */
    void type(KeyStroke keyStroke);

    /**
     * Types the given Keystroke defined by {@code keyStrokeString}, in the
     * format as defined for {@link KeyStroke#getKeyStroke(String)}.
     * <p>
     * Throws an exception when {@code keyStrokeString} is not valid.
     *
     * @param keyStrokeString the KeyStroke to type, as defined for
     * {@link KeyStroke#getKeyStroke(String)}
     */
    void typeKey(String keyStrokeString);

    /**
     * Types the key with the given {@code keycode}.
     *
     * @param keycode the keycode of the key to type, as defined in {@link java.awt.event.KeyEvent}
     */
    void typeKeycode(int keycode);

    /**
     * Release all keys that are currently pressed (and not yet released).
     */
    void releaseAllKeys();
}
