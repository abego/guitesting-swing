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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import static org.abego.guitesting.swing.internal.util.prop.Prop.newProp;

class PropBindable<T> extends PropBase<T> implements IPropBindable<T> {
    private IProp<T> prop;
    private EventObserver<PropertyChanged> observer;

    private PropBindable(@NonNull T initialValue,
                         @Nullable Object otherSource,
                         @Nullable String otherPropertyName) {
        super(initialValue,otherSource,otherPropertyName);
        prop = newProp(initialValue);
        observer = eventService.addPropertyObserver(
                prop, e -> postPropertyChanged());
    }

    public static <T> PropBindable<T> newPropBindable(
            @NonNull T initialValue,
            @Nullable Object otherSource,
            @Nullable String otherPropertyName) {
        return new PropBindable<>(initialValue, otherSource, otherPropertyName);
    }

    @Override
    public @NonNull T get() {
        return prop.get();
    }

    @Override
    public void set(@NonNull T value) {
        prop.set(value);
    }

    @Override
    public boolean hasValue() {
        return prop.hasValue();
    }

    public void bindTo(IProp<T> sourceOfTruth) {
        eventService.removeObserver(observer);

        @NonNull T oldValue = get();
        prop = sourceOfTruth;

        observer = eventService.addPropertyObserver(prop,
                e -> postPropertyChanged());
        if (!oldValue.equals(get())) {
            postPropertyChanged();
        }
    }
}
