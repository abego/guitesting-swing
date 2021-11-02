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
import org.abego.event.EventServices;
import org.abego.event.PropertyChanged;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

//TODO: review JavaDoc

/**
 * A {@link Var} emitting {@link PropertyChanged} events
 * when its value changed (via {@link EventServices} default).
 * <p>
 * The source of the PropertyChanged event will be the Prop object
 * and the property name "value". In addition, a second PropertyChanged event
 * may be generated with another source object and property name. The
 * "other source" typically is the object containing the Prop object and the
 * property name the name of the Prop within that container.
 */
abstract class PropComputedBase<T> extends PropBase<T> {
    private final Function<DependencyCollector, T> valueComputation;
    private @Nullable List<EventObserver<PropertyChanged>> observers;
    private @Nullable T value;
    private boolean mustComputeValue;

    protected PropComputedBase(EventsForProp eventsForProp,
                               Function<DependencyCollector, T> valueComputation,
                               @Nullable Object otherSource,
                               @Nullable String otherPropertyName) {
        super(eventsForProp, otherSource, otherPropertyName);
        this.valueComputation = valueComputation;
        this.mustComputeValue = true;
    }

    protected @Nullable T _get() {
        if (mustComputeValue) {
            // do the initial computation
            mustComputeValue = false;
            recompute();
        }
        return value;
    }

    protected void _set(@Nullable T value) {
        if (Objects.equals(value, this.value)) {
            return;
        }
        // ignore value setters for computed properties.
        // Instead, post a change event to make sure the objects depending on
        // this Prop use the currently computed value.
        postPropertyChanged();
    }

    private void recompute() {
        recomputeAndOnChangeDo(() -> {});
    }

    private void recomputeAndPostEvent() {
        recomputeAndOnChangeDo(this::postPropertyChanged);
    }

    private void recomputeAndOnChangeDo(Runnable onChangeCode) {

        if (observers != null) {
            for (EventObserver<PropertyChanged> o : observers) {
                removeObserver(o);
            }
            this.observers = null;
        }

        List<EventObserver<PropertyChanged>> observers = new ArrayList<>();
        DependencyCollector dependencyCollector = new DependencyCollector() {
            @Override
            public void dependsOnProperty(Object source, String propertyName) {
                observers.add(addPropertyObserver(
                        source, propertyName, e -> recomputeAndPostEvent()));
            }
        };

        T v = valueComputation.apply(dependencyCollector);
        if (!observers.isEmpty()) {
            this.observers = observers;
        }
        if (!Objects.equals(v, value)) {
            value = v;
            onChangeCode.run();
        }
    }
}
