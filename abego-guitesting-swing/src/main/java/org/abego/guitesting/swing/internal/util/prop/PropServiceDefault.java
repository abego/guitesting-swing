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
import org.abego.event.EventService;
import org.abego.event.EventServices;
import org.abego.event.PropertyChanged;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Timer;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.abego.guitesting.swing.internal.util.prop.PropFieldImpl.newPropField;

class PropServiceDefault implements PropService {
    private static final PropService DEFAULT_INSTANCE = newPropService(EventServices.getDefault());
    private final EventService eventService;
    private final EventAPIForProp eventAPIForProp = new EventHandlingForPropImpl();
    private Prop<Long> pseudoPropRecheckCount = newProp(0L);
    private Duration pseudoPropRecheckPeriod = PSEUDO_PROP_RECHECK_PERIOD_DEFAULT;
    private Timer pseudoPropRecheckTimer = new Timer((int) pseudoPropRecheckPeriod.toMillis(), a -> {
        pseudoPropRecheckCount.set(pseudoPropRecheckCount.get() + 1);
    });

    private class EventHandlingForPropImpl implements EventAPIForProp {
        private final Set<EventObserver<?>> remainingObservers = new HashSet<>();
        private boolean isClosed;

        @Override
        public EventObserver<PropertyChanged> addPropertyObserver(Object source, @Nullable String propertyName, Consumer<PropertyChanged> listener) {
            checkNotClosed();

            EventObserver<PropertyChanged> observer = eventService.addPropertyObserver(source, propertyName, listener);
            remainingObservers.add(observer);
            return observer;
        }

        @Override
        public void removeObserver(EventObserver<?> observer) {
            checkNotClosed();

            remainingObservers.remove(observer);
            eventService.removeObserver(observer);
        }

        @Override
        public void postPropertyChanged(Object source, String propertyName) {
            checkNotClosed();

            eventService.postPropertyChanged(source, propertyName);
        }

        public void close() {
            isClosed = true;
            eventService.removeAllObservers(remainingObservers);
        }

        public PropService getPropService() {
            return PropServiceDefault.this;
        }

        private void checkNotClosed() {
            if (isClosed) {
                throw new IllegalStateException("Already closed"); //NON-NLS
            }
        }
    }

    private PropServiceDefault(EventService eventService) {this.eventService = eventService;}

    static PropService getDefault() {
        return DEFAULT_INSTANCE;
    }

    static PropService newPropService(EventService eventsForProp) {
        return new PropServiceDefault(eventsForProp);
    }

    @Override
    public <T> PropField<T> newProp(T value) {
        return newPropField(eventAPIForProp, value, null, null);
    }

    @Override
    public <T> PropField<T> newProp(
            T value, Object otherSource, String otherPropertyName) {
        return newPropField(eventAPIForProp, value, otherSource, otherPropertyName);
    }

    @Override
    public <T> PropFieldNullable<T> newPropNullable() {
        return PropFieldNullableImpl.newPropFieldNullable(eventAPIForProp, null);
    }

    @Override
    public <T> PropFieldNullable<T> newPropNullable(T value) {
        return PropFieldNullableImpl.newPropFieldNullable(eventAPIForProp, value);
    }

    @Override
    public <T> PropFieldNullable<T> newPropNullable(
            @Nullable T value, Object otherSource, String otherPropertyName) {
        return PropFieldNullableImpl.newPropFieldNullable(eventAPIForProp, value, otherSource, otherPropertyName);
    }

    @Override
    public <T> PropComputed<T> newPropComputed(Function<DependencyCollector, T> valueComputation) {
        return PropComputedImpl.newPropComputed(eventAPIForProp, valueComputation);
    }

    @Override
    public <T> PropComputed<T> newPropComputed(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName) {
        return PropComputedImpl.newPropComputed(eventAPIForProp, valueComputation, otherSource, otherPropertyName);
    }

    @Override
    public <T> PropComputedNullable<T> newPropComputedNullable(Function<DependencyCollector, T> valueComputation) {
        return PropComputedNullableImpl.newPropComputedNullable(eventAPIForProp, valueComputation);
    }

    @Override
    public <T> PropComputedNullable<T> newPropComputedNullable(
            Function<DependencyCollector, T> valueComputation, Object otherSource, String otherPropertyName) {
        return PropComputedNullableImpl.newPropComputedNullable(eventAPIForProp, valueComputation, otherSource, otherPropertyName);
    }

    @Override
    public <T> PropComputed<T> newPseudoProp(Supplier<T> valueComputation) {
        ensurePseudoPropTimerIsRunning();
        PropComputed<T> result = newPropComputed(dc -> {
            // The PseudeProp depends on the Prop `pseudoPropRecheckCount` that
            // changes periodically. With this *fake* dependency we tell the
            // PropComputed that the computed value may have changed and trigger
            // a re-computation, and possibly a Propery value change for the
            // PropComputed.
            dc.dependsOnProp(pseudoPropRecheckCount);
            return valueComputation.get();
        });
        // we must call `get` now to setup the dependency to
        // `pseudoPropRecheckCount` and get notified when the count changes.
        result.get();
        return result;
    }

    @Override
    public <T> PropComputedNullable<T> newPseudoPropNullable(Supplier<T> valueComputation) {
        ensurePseudoPropTimerIsRunning();
        PropComputedNullable<T> result = newPropComputedNullable(dc -> {
            // The PseudeProp depends on the Prop `pseudoPropRecheckCount` that
            // changes periodically. With this *fake* dependency we tell the
            // PropComputed that the computed value may have changed and trigger
            // a re-computation, and possibly a Propery value change for the
            // PropComputed.
            dc.dependsOnProp(pseudoPropRecheckCount);
            return valueComputation.get();
        });
        // we must call `get` now to setup the dependency to
        // `pseudoPropRecheckCount` and get notified when the count changes.
        result.get();
        return result;
    }

    @Override
    public Duration getPseudoPropRecheckPeriod() {
        return pseudoPropRecheckPeriod;
    }

    @Override
    public void setPseudoPropRecheckPeriod(Duration period) {
        if (!pseudoPropRecheckPeriod.equals(period)) {
            int t = Math.toIntExact(period.toMillis());
            pseudoPropRecheckTimer.setInitialDelay(t);
            pseudoPropRecheckTimer.setDelay(t);
        }
    }

    @Override
    public Bindings newBindings() {
        return new BindingsImpl(eventAPIForProp);
    }

    @Override
    public EventService getEventService() {
        return eventService;
    }

    @Override
    public void close() {
        eventAPIForProp.close();
    }

    private void ensurePseudoPropTimerIsRunning() {
        if (!pseudoPropRecheckTimer.isRunning()) {
            pseudoPropRecheckTimer.start();
        }
    }
}
