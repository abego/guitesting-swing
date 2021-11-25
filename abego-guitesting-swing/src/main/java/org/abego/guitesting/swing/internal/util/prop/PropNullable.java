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

/**
 * As {@link Prop}, but supporting {@code nullable} values.
 */
public interface PropNullable<T> extends AnyProp {
    /**
     * Return the value of the Var.
     */
    @Nullable T get();

    /**
     * Sets the value of the Prop to the {@code value}.
     * <p>
     * When the Prop is not editable setting a value may be ignored silently,
     * or throw an Exception, depending on the implementation of the Prop.
     */
    void set(@Nullable T value);

    /**
     * Returns true when the Prop is editable, i.e. calling {@link #set(Object)}
     * will change the value, and false otherwise.
     **/
    default boolean isEditable() {
        return true;
    }

    default  @Nullable T get(DependencyCollector collector) {
        collector.dependsOnProp(this);
        return get();
    }
}
