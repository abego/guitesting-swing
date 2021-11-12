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

import org.abego.event.EventObserver;
import org.abego.event.PropertyChanged;
import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;

abstract class PropBase<T> {
    private final EventAPIForProp eventAPIForProp;
    private final @Nullable Object otherSource;
    private final @Nullable String otherPropertyName;

    protected PropBase(EventAPIForProp eventAPIForProp,
                       @Nullable Object otherSource,
                       @Nullable String otherPropertyName) {
        this.eventAPIForProp = eventAPIForProp;
        this.otherSource = otherSource;
        this.otherPropertyName = otherPropertyName;
    }

    EventObserver<PropertyChanged> addPropertyObserver(
            Object source,
            @Nullable String propertyName,
            Consumer<PropertyChanged> listener) {
        return eventAPIForProp.addPropertyObserver(source, propertyName, listener);
    }

    protected void removeObserver(EventObserver<PropertyChanged> o) {
        eventAPIForProp.removeObserver(o);
    }

    protected void postPropertyChanged() {
        eventAPIForProp.postPropertyChanged(this, PropService.VALUE_PROPERTY_NAME); //NON-NLS
        if (otherSource != null && otherPropertyName != null) {
            eventAPIForProp.postPropertyChanged(otherSource, otherPropertyName);
        }
    }


}
