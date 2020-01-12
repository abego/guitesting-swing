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

import org.abego.commons.seq.Seq;

import java.awt.Component;
import java.awt.Window;
import java.util.function.Predicate;

import static org.abego.guitesting.ComponentSupport.hasComponentNamePredicate;

public interface WindowSupport {
    /**
     * Return all Windows, including invisible ones, of the given <code>windowClass</code>.
     */
    <T extends Window> Seq<T> allWindowsIncludingInvisibleOnes(Class<T> windowClass);

    /**
     * Return all Windows, including invisible ones.
     */
    default Seq<Window> allWindowsIncludingInvisibleOnes() {
        return allWindowsIncludingInvisibleOnes(Window.class);
    }

    /**
     * Return all Windows of the given <code>windowClass</code>.
     *
     * <p>Invisible Windows are not considered.</p>
     */
    default <T extends Window> Seq<T> allWindows(Class<T> windowClass) {
        return allWindowsIncludingInvisibleOnes(windowClass).filter(Component::isVisible);
    }

    /**
     * Return all Windows.
     *
     * <p>Invisible Windows are not considered.</p>
     */
    default Seq<Window> allWindows() {
        return allWindows(Window.class);
    }

    /**
     * Return all Windows of the given <code>windowClass</code> that match the
     * <code>condition</code>.
     *
     * <p>Invisible Windows are not considered.</p>
     */
    default <T extends Window> Seq<T> allWindowsWith(Class<T> windowClass, Predicate<T> condition) {
        return allWindows(windowClass).filter(condition);
    }

    /**
     * Return all Windows that match the <code>condition</code>.
     *
     * <p>Invisible Windows are not considered.</p>
     */
    default Seq<Window> allWindowsWith(Predicate<Window> condition) {
        return allWindowsWith(Window.class, condition);
    }

    /**
     * Return <code>true</code> when a window of the given <code>windowClass</code>
     * exists that matches the <code>condition</code> or <code>false</code> otherwise.
     */
    default <T extends Window> boolean hasWindowWith(Class<T> windowClass, Predicate<T> condition) {
        return !allWindowsWith(windowClass, condition).isEmpty();
    }

    /**
     * Return <code>true</code> when a window exists that matches the
     * <code>condition</code> or <code>false</code> otherwise.
     */
    default boolean hasWindowWith(Predicate<Window> condition) {
        return !allWindowsWith(condition).isEmpty();
    }

    /**
     * Return <code>true</code> when a window of the given <code>windowClass</code>
     * exists that has the expected <code>name</code>.
     */
    default <T extends Window> boolean hasWindowNamed(Class<T> windowClass, String name) {
        return hasWindowWith(windowClass, hasComponentNamePredicate(name));
    }

    /**
     * Return <code>true</code> when a window exists that has the expected
     * <code>name</code>.
     */
    default boolean hasWindowNamed(String name) {
        return hasWindowNamed(Window.class, name);
    }


    /**
     * Return the window of the given <code>windowClass</code> that matches the <code>condition</code>.
     *
     * <p>Throw a NoSuchElementException when no window or more than one window
     * matches the condition.</p>
     */
    default <T extends Window> T windowWith(Class<T> windowClass, Predicate<T> condition) {
        return allWindowsWith(windowClass, condition).singleItem();
    }

    /**
     * Return the window that matches the <code>condition</code>.
     *
     * <p>Throw a NoSuchElementException when no window or more than one window
     * matches the condition.</p>
     */
    default Window windowWith(Predicate<Window> condition) {
        return windowWith(Window.class, condition);
    }

    /**
     * Return the window of the given <code>windowClass</code> that has the
     * expected <code>name</code>.
     *
     * <p>Throw a NoSuchElementException when no window or more than one window
     * matches the condition.</p>
     */
    default <T extends Window> T windowNamed(Class<T> windowClass, String name) {
        return windowWith(windowClass, hasComponentNamePredicate(name));
    }

    /**
     * Return the window that has the expected <code>name</code>.
     *
     * <p>Throw a NoSuchElementException when no window or more than one window
     * matches the condition.</p>
     */
    default Window windowNamed(String name) {
        return windowWith(hasComponentNamePredicate(name));
    }

    /**
     * Return a Window of the given <code>windowClass</code> that matches the
     * <code>condition</code>.
     *
     * <p>When multiple Windows match the condition return one of them.</p>
     *
     * <p>Invisible Windows are not considered.</p>
     *
     * <p>Throw a NoSuchElementException when no window matches the condition.</p>
     */
    default <T extends Window> T anyWindowWith(Class<T> windowClass, Predicate<T> condition) {
        return allWindows(windowClass).filter(condition).anyItem();
    }

    /**
     * Return a Window that matches the <code>condition</code>.
     *
     * <p>When multiple Windows match the condition return one of them.</p>
     *
     * <p>Invisible Windows are not considered.</p>
     *
     * <p>Throw a NoSuchElementException when no window matches the condition.</p>
     */
    default Window anyWindowWith(Predicate<Window> condition) {
        return anyWindowWith(Window.class, condition);
    }

    /**
     * Close the <code>window</code>.
     */
    default void close(Window window) {
        window.dispose();
    }

}
