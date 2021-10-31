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

import org.abego.commons.var.VarNullable;
import org.abego.event.EventObserver;
import org.abego.event.EventService;
import org.abego.event.EventServices;
import org.abego.event.PropertyChanged;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

import static org.abego.guitesting.swing.internal.util.prop.PropNullable.newPropNullable;

public class PropNullableBindable<T> implements VarNullable<T> {
    private final EventService eventService = EventServices.getDefault();
    private final Consumer<T> onSourceOfTruthValueChanged;
    private final @Nullable Object otherSource;
    private final @Nullable String otherPropertyName;
    private PropNullable<T> prop;
    private EventObserver<PropertyChanged> observer;

    private PropNullableBindable(@Nullable T initialValue,
                                 @Nullable Object otherSource,
                                 @Nullable String otherPropertyName,
                                 //TODO: avoid callback, use events
                                 Consumer<T> onSourceOfTruthValueChanged) {
        prop = newPropNullable(initialValue);
        this.onSourceOfTruthValueChanged = onSourceOfTruthValueChanged;
        this.otherSource = otherSource;
        this.otherPropertyName = otherPropertyName;
        observer = eventService.addPropertyObserver(
                prop, e -> onValueChanged(get()));
    }

    public static <T> PropNullableBindable<T> newPropNullableBindable(
            @Nullable T initialValue, Consumer<T> onSourceOfTruthValueChanged) {
        return new PropNullableBindable<>(initialValue, null, null,
                onSourceOfTruthValueChanged);
    }

    public static <T> PropNullableBindable<T> newPropNullableBindable(
            @Nullable T initialValue,
            @Nullable Object otherSource,
            @Nullable String otherPropertyName,
            Consumer<T> onSourceOfTruthValueChanged) {
        return new PropNullableBindable<>(initialValue, otherSource, otherPropertyName,
                onSourceOfTruthValueChanged);
    }

    public static <T> PropNullableBindable<T> newPropNullableBindable(
            @Nullable T initialValue,
            @Nullable Object otherSource,
            @Nullable String otherPropertyName) {
        return new PropNullableBindable<>(initialValue, otherSource, otherPropertyName,
                e->{});
    }

    @Override
    public @Nullable T get() {
        return prop.get();
    }

    @Override
    public void set(@Nullable T value) {
        prop.set(value);
    }

    public void bindTo(PropNullable<T> sourceOfTruth) {
        eventService.removeObserver(observer);

        @Nullable T oldValue = get();
        prop = sourceOfTruth;

        observer = eventService.addPropertyObserver(prop,
                e -> onValueChanged(get()));
        if (!Objects.equals(oldValue, get())) {
            onValueChanged(get());
        }
    }

    /**
     * Runs a {@link Runnable} (the "code") "now" and whenever the Prop changes.
     */
    public void runDependingCode(Runnable code) {
        code.run();
        eventService.addPropertyObserver(this, "value", e -> code.run());
    }

    private void onValueChanged(T newValue) {
        onSourceOfTruthValueChanged.accept(newValue);
        // when the source of truth changed also this object's value changed
        eventService.postPropertyChanged(this, "value"); //NON-NLS
        if (otherSource != null && otherPropertyName != null) {
            eventService.postPropertyChanged(otherSource, otherPropertyName);
        }
    }

}
