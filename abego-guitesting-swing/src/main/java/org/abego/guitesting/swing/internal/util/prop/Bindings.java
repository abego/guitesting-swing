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

package org.abego.guitesting.swing.internal.util.prop;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;

public interface Bindings {
    /**
     * Binds {@code prop} and {@code sourceOfTruth} to each other, i.e.
     * {@code prop} is set to the current value of {@code sourceOfTruth} and
     * each value is updated whenever the other value changes.
     * <p>
     * A {@link Prop} must only be bound to one source of truth.
     */
    <T> void bind(Prop<T> sourceOfTruth, Prop<T> prop);

    /**
     * Binds {@code prop} and {@code sourceOfTruth} to each other, i.e.
     * {@code prop} is set to the current value of {@code sourceOfTruth} and
     * each value is updated whenever the other value changes.
     * <p>
     * A {@link Prop} must only be bound to one source of truth.
     */
    <T> void bind(PropNullable<T> sourceOfTruth, PropNullable<T> prop);

    /**
     * Binds the {@code consumer} to the {@code prop}, i.e.the {@code consumer}'s
     * method {@link Consumer#accept(Object)} is called with the current value
     * of {@code prop} and called again with {@code prop}'s value whenever
     * the value changes.
     */
    <T> void bindTo(Consumer<T> consumer, Prop<T> prop);

    /**
     * Binds the {@code consumer} to the {@code prop}, i.e.the {@code consumer}'s
     * method {@link Consumer#accept(Object)} is called with the current value
     * of {@code prop} and called again with {@code prop}'s value whenever
     * the value changes.
     */
    <T> void bindTo(Consumer<@Nullable T> consumer, PropNullable<T> prop);

    /**
     * Binds the {@code consumer} to the {@code prop}, i.e.the {@code consumer}'s
     * method {@link Consumer#accept(Object)} is called with the current value
     * of {@code prop} and called again (from the EventDispatchThread)
     * with {@code prop}'s value whenever the value changes.
     */
    <T> void bindSwingCodeTo(Consumer<T> consumer, Prop<T> prop);

    /**
     * Binds the {@code consumer} to the {@code prop}, i.e.the {@code consumer}'s
     * method {@link Consumer#accept(Object)} is called with the current value
     * of {@code prop} and called again  (from the EventDispatchThread)
     * with {@code prop}'s value whenever the value changes.
     */
    <T> void bindSwingCodeTo(Consumer<@Nullable T> consumer, PropNullable<T> prop);

    /**
     * Binds the {@code code} to {@code props}, i.e. run the code now and
     * whenever {@link AnyProp} in {{@code props} changes its value.
     * <p>
     * The {@code code} may operate on Swing components. Therefore, all but the
     * initial run are executed in the Event Dispatch Thread. The initial run
     * is performed immediately, in the current thread. To be thread-safe
     * make sure to run #bindSwingCode from the Event Dispatch Thread
     * (e.g. via {@link javax.swing.SwingUtilities#invokeLater(Runnable)} or,
     * before the Swing components the {@code code} operates on are realized
     * (http://www.javapractices.com/topic/TopicAction.do?Id=153).
     * <p>
     * Typically this method is used to update Swing components that depend
     * on the values of the items in {@code props}. The first run of the
     * {@code code}, directly performed with the invocation of #bindSwingCode,
     * initializes the related aspects of the Swing components (e.g. their
     * "text"). After that the {@code code} is re-run when {@code prop}'s
     * value changed.<p>
     * <p>
     * After a change the {@code code} is executed, but there may be some delay
     * between the change and the execution of the {@code code}. Also multiple
     * changes may result in just one code execution, covering all changes that
     * occured since the last code run.
     */
    void bindSwingCode(Runnable code, AnyProp... props);

    /**
     * Returns {@code true} when the Bindings are currently running "update
     * code", {@code false} otherwise.
     * <p>
     * This function can be used to check if the currently executed code
     * is part of a "Binding update" or not. This can be useful when some
     * code may be executed by Bindings or from some other way. A typical
     * scenario is the following:
     * <p>
     * We want to bind the text of a JTextField `tf` to a String Prop `text`.
     * Changing `text` should update `tf`'s text. Therefore, we use the
     * standard "Bindings.bindSwingCode" API. To support the other
     * direction, i.e. the user's edits in `tf` update the Prop `text`, we
     * add a DocumentListener to `tf`, containing code like this:
     * <pre>
     * text.set(tf.getText());
     * </pre>
     * However this code is also triggered when `tf`'s text is changed
     * through the bind mechanism. This way we may introduce a cycle in
     * the update procedure.
     * <p>
     * To avoid this problem we could write the code in the DocumentListener
     * like this:
     * <pre>
     * if (!bindings.isUpdating()) {
     *     text.set(tf.getText());
     * }
     * </pre>
     * This way the textfield will update the Prop `text` only when it is not
     * currently in a update itself.
     */
    boolean isUpdating();

    void close();
}
