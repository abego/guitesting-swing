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

/**
 * Easily write GUI Tests in Java (for Swing).
 * <p>
 * This module provides a bunch of features useful when testing desktop Swing
 * applications. Most functionality is accessible through the
 * {@link org.abego.guitesting.swing.GT} interface.
 * <p>
 * <b>Example</b>
 * <p>
 * A typical code snippet using {@link org.abego.guitesting.swing.GT} may look
 * like this:
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
 * // we move the focus to that input field and type "Your name" ", please!
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
 * <b>Unit Testing</b>
 * <p>
 * When writing JUnit tests you may want to subclass from
 * {@link org.abego.guitesting.swing.GuiTestBase}.
 * <p>
 * <b>Sample Code</b>
 * <p>
 * For sample code how to use the GUITesting Swing module have a look at the
 * test code of this module, in the {@code src/test/java} folder.
 */

@NonNullByDefault
package org.abego.guitesting.swing;

import org.eclipse.jdt.annotation.NonNullByDefault;