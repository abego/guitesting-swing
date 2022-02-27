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

import org.abego.event.EventService;
import org.abego.event.PropertyChanged;
import org.eclipse.jdt.annotation.NonNull;

/**
 * An object holding a value and emitting {@link PropertyChanged} events
 * when its value changed (via a {@link EventService} defined by this Prop's
 * {@link PropService}).
 * <p>
 * The source of the PropertyChanged event will be the Prop object
 * and the property name "value". In addition, a second PropertyChanged event
 * may be generated with another source object and property name. The
 * "other source" typically is the object containing the Prop object and the
 * property name the name of the Prop within that container.
 */
public interface Prop<T> extends AnyProp {

    /**
     * Return the value of the Prop.
     */
    @NonNull T get();

    /**
     * Sets the value of the Prop to the {@code value}.
     * <p>
     * When the Prop is not editable setting a value may be ignored silently,
     * or throw an Exception, depending on the implementation of the Prop.
     */
    void set(@NonNull T value);

    /**
     * Returns true when the Prop has a value, and false otherwise
     **/
    default boolean hasValue() {
        return true;
    }

    /**
     * Returns true when the Prop is editable, i.e. calling {@link #set(Object)}
     * will change the value, and false otherwise.
     **/
    default boolean isEditable() {
        return true;
    }

    default T get(DependencyCollector collector) {
        collector.dependsOnProp(this);
        return get();
    }
}
