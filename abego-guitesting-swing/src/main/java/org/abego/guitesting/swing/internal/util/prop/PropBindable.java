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

import org.abego.commons.var.Var;
import org.abego.event.EventObserver;
import org.abego.event.EventService;
import org.abego.event.EventServices;
import org.abego.event.PropertyChanged;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;

import static org.abego.guitesting.swing.internal.util.prop.Prop.newProp;

public class PropBindable<T> implements Var<T> {
    private final EventService eventService = EventServices.getDefault();
    private final @Nullable Object otherSource;
    private final @Nullable String otherPropertyName;
    private Prop<T> prop;
    private EventObserver<PropertyChanged> observer;

    private PropBindable(@NonNull T initialValue,
                         @Nullable Object otherSource,
                         @Nullable String otherPropertyName) {
        prop = newProp(initialValue);
        this.otherSource = otherSource;
        this.otherPropertyName = otherPropertyName;
        observer = eventService.addPropertyObserver(
                prop, e -> onValueChanged(get()));
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

    /**
     * Runs a {@link Runnable} (the "code") "now" and whenever the Prop changes.
     */
    public void runDependingCode(Runnable code) {
        code.run();
        eventService.addPropertyObserver(this, "value", e -> code.run());
    }

    public void bindTo(Prop<T> sourceOfTruth) {
        eventService.removeObserver(observer);

        @NonNull T oldValue = get();
        prop = sourceOfTruth;

        observer = eventService.addPropertyObserver(prop,
                e -> onValueChanged(get()));
        if (!oldValue.equals(get())) {
            onValueChanged(get());
        }
    }

    private void onValueChanged(T newValue) {
        // when the source of truth changed also this object's value changed
        eventService.postPropertyChanged(this, "value"); //NON-NLS
        if (otherSource != null && otherPropertyName != null) {
            eventService.postPropertyChanged(otherSource, otherPropertyName);
        }
    }

}
