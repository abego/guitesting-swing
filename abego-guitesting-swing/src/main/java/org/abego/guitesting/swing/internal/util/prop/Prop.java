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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A {@link Var} emitting {@link org.abego.event.PropertyChanged} events
 * when its value changed (via {@link org.abego.event.EventServices} default).
 * <p>
 * The source of the PropertyChanged event will be the Prop object
 * and the property name "value". In addition a second PropertyChanged event
 * may be generated with another source object and property name. The
 * "other source" typically is the object containing the Prop object and the
 * property name the name of the Prop within that container.
 */
//TODO: check if we can reuse some code of the different "Prop..." classes
public class Prop<T> implements Var<T> {
    private final EventService eventService = EventServices.getDefault();
    private final @Nullable Object otherSource;
    private final @Nullable String otherPropertyName;
    private final @Nullable Function<DependencyCollector, T> valueComputation;
    private @Nullable T value;
    private @Nullable List<EventObserver<PropertyChanged>> observers;


    private Prop(@Nullable T value,
                 @Nullable Function<DependencyCollector, T> valueComputation,
                 @Nullable Object otherSource,
                 @Nullable String otherPropertyName) {
        if (value != null && valueComputation != null) {
            throw new IllegalArgumentException("Must not specify both a value and a valueComputation");
        }
        this.value = value;
        this.valueComputation = valueComputation;
        this.otherSource = otherSource;
        this.otherPropertyName = otherPropertyName;
    }

    public static <T> Prop<T> newProp() {
        return new Prop<T>(null, null, null, null);
    }

    public static <T> Prop<T> newProp(T value) {
        return new Prop<T>(value, null, null, null);
    }

    public static <T> Prop<T> newProp(
            T value, Object otherSource, String otherPropertyName) {
        return new Prop<T>(value, null, otherSource, otherPropertyName);
    }

    public static <T> Prop<T> newComputedProp(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName) {
        return new Prop<T>(null, valueComputation, otherSource, otherPropertyName);
    }

    public static <T> Prop<T> newComputedProp(Function<DependencyCollector, T> valueComputation) {
        return new Prop<>(null, valueComputation, null, null);
    }

    @Override
    public @NonNull T get() {
        @Nullable T v = value;
        if (v == null) {
            if (valueComputation != null) {
                v = recompute();
            }
            if (v == null) {
                throw new IllegalStateException("Var has no value"); //NON-NLS
            }
        }
        return v;
    }

    @Override
    public void set(@NonNull T value) {
        if (value.equals(this.value)) {
            return;
        }
        if (valueComputation != null) {
            throw new IllegalStateException("Cannot set value on computed property");
        }
        this.value = value;
        postPropertyChanged();
    }

    /**
     * Runs a {@link Runnable} (the "code") "now" and whenever the Prop changes.
     */
    public void runDependingCode(Runnable code) {
        code.run();
        eventService.addPropertyObserver(this, "value", e -> code.run());
    }

    @NonNull
    private T recompute() {
        return recomputeAndOnChangeDo(() -> {});
    }

    @NonNull
    private T recomputeAndPostEvent() {
        return recomputeAndOnChangeDo(this::postPropertyChanged);
    }

    @NonNull
    private T recomputeAndOnChangeDo(Runnable onChangeCode) {
        if (observers != null) {
            for (EventObserver<PropertyChanged> o : observers) {
                eventService.removeObserver(o);
            }
            this.observers = null;
        }

        List<EventObserver<PropertyChanged>> observers = new ArrayList<>();
        DependencyCollector dependencyCollector = new DependencyCollector() {
            @Override
            public void dependsOnProperty(Object source, String propertyName) {
                observers.add(eventService.addPropertyObserver(
                        source, propertyName, e -> recomputeAndPostEvent()));
            }
        };
        T v = valueComputation.apply(dependencyCollector);
        if (!observers.isEmpty()) {
            this.observers = observers;
        }
        if (!v.equals(value)) {
            value = v;
            onChangeCode.run();
        }
        return v;
    }

    private void postPropertyChanged() {
        eventService.postPropertyChanged(this, "value"); //NON-NLS
        if (otherSource != null && otherPropertyName != null) {
            eventService.postPropertyChanged(otherSource, otherPropertyName);
        }
    }

    @Override
    public boolean hasValue() {
        return value != null;
    }
}
