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
import sun.plugin.dom.exception.InvalidStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link VarNullable} emitting {@link org.abego.event.PropertyChanged} events
 * when its value changed (via {@link EventServices} default).
 * <p>
 * By default the source of the PropertyChanged event will be the Prop object
 * and the property name "value". In addition, a second PropertyChanged event
 * may be generated with another source object and property name. The
 * "other source" typically is the object containing the Prop object and the
 * property name the name of the Prob within its container.
 */
public class PropNullable<T> implements VarNullable<T> {
    //TODO: can we share some code between PropNullable and Prop?

    private final EventService eventService = EventServices.getDefault();
    private final @Nullable Object otherSource;
    private final @Nullable Function<DependencyCollector, T> valueComputation;
    private final @Nullable String otherPropertyName;
    private @Nullable T value;
    private @Nullable List<EventObserver<PropertyChanged>> observers;
    private boolean mustComputeValue;


    private PropNullable(@Nullable T value,
                         @Nullable Function<DependencyCollector, T> valueComputation,
                         @Nullable Object otherSource,
                         @Nullable String otherPropertyName) {
        if (value != null && valueComputation != null) {
            //noinspection DuplicateStringLiteralInspection
            throw new IllegalArgumentException("Must not specify both a value and a valueComputation"); //NON-NLS
        }
        this.value = value;
        this.valueComputation = valueComputation;
        this.otherSource = otherSource;
        this.otherPropertyName = otherPropertyName;
        this.mustComputeValue = valueComputation != null;
    }

    public static <T> PropNullable<T> newPropNullable() {
        return new PropNullable<>(null, null,null, null);
    }

    public static <T> PropNullable<T> newPropNullable(T value) {
        return new PropNullable<>(value, null,null, null);
    }

    public static <T> PropNullable<T> newPropNullable(
            @Nullable T value, Object otherSource, String otherPropertyName) {
        return new PropNullable<>(value, null, otherSource, otherPropertyName);
    }

    public static <T> PropNullable<T> newComputedPropNullable(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName) {
        return new PropNullable<>(null, valueComputation, otherSource, otherPropertyName);
    }

    public static <T> PropNullable<T> newComputedPropNullable(Function<DependencyCollector, T> valueComputation) {
        return new PropNullable<>(null, valueComputation, null, null);
    }

    @Override
    public @Nullable T get() {
        if (mustComputeValue) {
            // do the initial computation
            mustComputeValue = false;
            recompute();
        }
        return value;
    }

    @Override
    public void set(@Nullable T value) {
        if (Objects.equals(value, this.value)) {
            return;
        }
        if (valueComputation != null) {
            //noinspection DuplicateStringLiteralInspection
            throw new IllegalStateException("Cannot set value on computed property"); //NON-NLS
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

    private void recompute() {
        recomputeAndOnChangeDo(() -> {});
    }

    private void recomputeAndPostEvent() {
        recomputeAndOnChangeDo(this::postPropertyChanged);
    }

    private void recomputeAndOnChangeDo(Runnable onChangeCode) {
        @Nullable Function<DependencyCollector, T> computation = this.valueComputation;
        if (computation == null) {
            throw new InvalidStateException("Internal Error: no valueComputation defined");
        }

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
        @Nullable Function<DependencyCollector, T> comp = valueComputation;
        T v = computation.apply(dependencyCollector);
        mustComputeValue = true;
        if (!observers.isEmpty()) {
            this.observers = observers;
        }
        if (!Objects.equals(v, value)) {
            value = v;
            onChangeCode.run();
        }
    }

    private void postPropertyChanged() {
        //noinspection DuplicateStringLiteralInspection
        eventService.postPropertyChanged(this, "value"); //NON-NLS
        if (otherSource != null && otherPropertyName != null) {
            eventService.postPropertyChanged(otherSource, otherPropertyName);
        }
    }
}
