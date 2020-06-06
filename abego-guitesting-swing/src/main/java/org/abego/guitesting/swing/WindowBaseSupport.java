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

import java.awt.Component;
import java.awt.Window;
import java.util.function.Predicate;

import static org.abego.guitesting.swing.ComponentBaseSupport.hasComponentNamePredicate;

/**
 * Basic operations dealing with {@link Window}s, like detecting and
 * collecting Windows.
 */
public interface WindowBaseSupport {
    /**
     * Returns all Windows, including invisible ones, of the given
     * {@code windowClass}.
     *
     * @param windowClass the type of {@link Window}s to return
     * @param <T>         the type of {@link Window}s to return
     * @return all Windows, including invisible ones, of the given {@code windowClass}
     */
    <T extends Window> Seq<T> allWindowsIncludingInvisibleOnes(Class<T> windowClass);

    /**
     * Returns all Windows, including invisible ones.
     *
     * @return all Windows, including invisible ones
     */
    default Seq<Window> allWindowsIncludingInvisibleOnes() {
        return allWindowsIncludingInvisibleOnes(Window.class);
    }

    /**
     * Returns all visible Windows of the given {@code windowClass}.
     *
     * @param windowClass the type of {@link Window}s to return
     * @param <T>         the type of {@link Window}s to return
     * @return all visible Windows of the given {@code windowClass}.
     */
    default <T extends Window> Seq<T> allWindows(Class<T> windowClass) {
        return allWindowsIncludingInvisibleOnes(windowClass).filter(Component::isVisible);
    }

    /**
     * Returns all visible Windows.
     *
     * @return all visible Windows
     */
    default Seq<Window> allWindows() {
        return allWindows(Window.class);
    }

    /**
     * Returns all visible Windows of the given {@code windowClass} that match the
     * {@code condition}.
     *
     * @param windowClass the type of {@link Window}s to return
     * @param <T>         the type of {@link Window}s to return
     * @param condition   the condition of the window to check
     * @return all visible Windows of the given {@code windowClass} that match the
     * {@code condition}
     */
    default <T extends Window> Seq<T> allWindowsWith(Class<T> windowClass, Predicate<T> condition) {
        return allWindows(windowClass).filter(condition);
    }

    /**
     * Returns all visible Windows that match the {@code condition}.
     *
     * @param condition the condition of the window to check
     * @return all Windows  that match the {@code condition}
     */
    default Seq<Window> allWindowsWith(Predicate<Window> condition) {
        return allWindowsWith(Window.class, condition);
    }

    /**
     * Returns {@code true} when a window of the given {@code windowClass}
     * exists that matches the {@code condition}; returns {@code false} otherwise.
     *
     * @param windowClass the type of {@link Window}s to check
     * @param <T>         the type of {@link Window}s to check
     * @param condition   the condition of the window to check
     * @return {@code true} when a window of the given {@code windowClass}
     * exists that matches the {@code condition}; {@code false} otherwise
     */
    default <T extends Window> boolean hasWindowWith(Class<T> windowClass, Predicate<T> condition) {
        return !allWindowsWith(windowClass, condition).isEmpty();
    }

    /**
     * Returns {@code true} when a window exists that matches the
     * {@code condition} or {@code false} otherwise.
     *
     * @param condition the condition of the window to check
     * @return {@code true} when a window exists that matches the
     * {@code condition} or {@code false} otherwise
     */
    default boolean hasWindowWith(Predicate<Window> condition) {
        return !allWindowsWith(condition).isEmpty();
    }

    /**
     * Returns {@code true} when a window of the given {@code windowClass}
     * exists that has the expected {@code name}; returns {@code false} otherwise.
     *
     * @param windowClass the type of {@link Window}s to check
     * @param <T>         the type of {@link Window}s to check
     * @param name        the expected name of a window
     * @return {@code true} when a window of the given {@code windowClass}
     * exists that has the expected {@code name}; {@code false} otherwise
     */
    default <T extends Window> boolean hasWindowNamed(Class<T> windowClass, String name) {
        return hasWindowWith(windowClass, hasComponentNamePredicate(name));
    }

    /**
     * Returns {@code true} when a window exists that has the expected
     * {@code name}.
     *
     * @param name the expected name of a window
     * @return {@code true} when a window exists that has the expected
     * {@code name}
     */
    default boolean hasWindowNamed(String name) {
        return hasWindowNamed(Window.class, name);
    }

    /**
     * Returns the window of the given {@code windowClass} that matches the {@code condition}.
     *
     * <p>Throws a NoSuchElementException when no window or more than one window
     * matches the condition.</p>
     *
     * @param windowClass the type of {@link Window}s to return
     * @param <T>         the type of {@link Window}s to return
     * @param condition   the condition of the window to return
     * @return the window of the given {@code windowClass} that matches the {@code condition}
     */
    default <T extends Window> T windowWith(Class<T> windowClass, Predicate<T> condition) {
        return allWindowsWith(windowClass, condition).singleItem();
    }

    /**
     * Returns the window that matches the {@code condition}.
     *
     * <p>Throws a NoSuchElementException when no window or more than one window
     * matches the condition.</p>
     *
     * @param condition the condition of the window to return
     * @return the window that matches the {@code condition}
     */
    default Window windowWith(Predicate<Window> condition) {
        return windowWith(Window.class, condition);
    }

    /**
     * Returns the window of the given {@code windowClass} that has the
     * expected {@code name}.
     *
     * <p>Throws a NoSuchElementException when no window or more than one window
     * matches the condition.</p>
     *
     * @param windowClass the type of {@link Window}s to return
     * @param <T>         the type of {@link Window}s to return
     * @param name        the name of the window to return
     * @return the window of the given {@code windowClass} that has the expected {@code name}
     */
    default <T extends Window> T windowNamed(Class<T> windowClass, String name) {
        return windowWith(windowClass, hasComponentNamePredicate(name));
    }

    /**
     * Returns the window that has the expected {@code name}.
     *
     * <p>Throws a NoSuchElementException when no window or more than one window
     * matches the condition.</p>
     *
     * @param name the name of the window to return
     * @return the window that has the expected {@code name}
     */
    default Window windowNamed(String name) {
        return windowWith(hasComponentNamePredicate(name));
    }

    /**
     * Returns a Window of the given {@code windowClass} that matches the
     * {@code condition}.
     *
     * <p>When multiple Windows match the condition return one of them.</p>
     *
     * <p>Invisible Windows are not considered.</p>
     *
     * <p>Throws a NoSuchElementException when no window matches the condition.</p>
     *
     * @param windowClass the type of {@link Window}s to return
     * @param <T>         the type of {@link Window}s to return
     * @param condition   the condition of the window to return
     * @return a Window of the given {@code windowClass} that matches the
     * {@code condition}
     */
    default <T extends Window> T anyWindowWith(Class<T> windowClass, Predicate<T> condition) {
        return allWindows(windowClass).filter(condition).anyItem();
    }

    /**
     * Returns a Window that matches the {@code condition}.
     *
     * <p>When multiple Windows match the condition return one of them.</p>
     *
     * <p>Invisible Windows are not considered.</p>
     *
     * <p>Throw a NoSuchElementException when no window matches the condition.</p>
     *
     * @param condition the condition of the window to return
     * @return a Window that matches the {@code condition}
     */
    default Window anyWindowWith(Predicate<Window> condition) {
        return anyWindowWith(Window.class, condition);
    }

    /**
     * Closes the {@code window}.
     *
     * @param window the {@link Window} to close
     */
    default void close(Window window) {
        window.dispose();
    }

}
