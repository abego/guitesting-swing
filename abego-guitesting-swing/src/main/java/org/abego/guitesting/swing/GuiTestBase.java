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


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A base class for JUnit 5 GUI tests.
 * <p>
 * It holds a protected final field {@code gt} with a {@link GT} instance.
 * Subclasses typically use {@code gt} in their test methods like in this
 * example:
 * <pre>
 * &#64;Test
 * void testSample() {
 *
 *     // run some application code that opens a window
 *     openSampleWindow();
 *
 *     // In that window we are interested in a JTextField named "input"
 *     JTextField input = gt.waitForComponentNamed(JTextField.class, "input");
 *
 *     // Move the focus to that input field and type "Your name" ", please!"
 *     gt.setFocusOwner(input);
 *     gt.type("Your name");
 *     gt.type(", please!");
 *
 *     // Verify if the text field really contains the expected text.
 *     gt.assertEqualsRetrying("Your name, please!", input::getText);
 * }
 * </pre>
 * For every test a new {@link GT} instance is used. The instance is also
 * observes the  GuiTesting related system properties (see
 * {@link GT#readSystemProperties()}).
 * <p>
 * After each test {@link GT#cleanup()} is performed, e.g. to all remaining
 * windows are closes/disposed.
 */
@ExtendWith(DumpComponentsOnFailure.class)
public class GuiTestBase {
    protected final GT gt = GuiTesting.newGT();

    @BeforeEach
    void setup() {
        gt.readSystemProperties();
    }

    @AfterEach
    void teardown() {
        gt.cleanup();
    }
}
