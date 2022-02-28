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

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static javax.swing.SwingUtilities.invokeLater;

abstract class PropComputedBase<T> extends PropBase<T> {
    //TODO: currently the value is calculation with the first call to get.
    //  As the dependencies of the calculations are only known after the first
    //  calculation the PropComputed cannot trigger any PropertyChanged events
    //  until after the first calculation. This may not be what a user expects,
    //  E.g. a user defined a PropComputed and is not interested in the actual
    //  value but only that the valued changed. Therefore the user observes the
    //  PropComputed value (using EventService.addObserver). But he will not
    //  see any events as the PropComputed does not know about the dependencies
    //  of the calculation (without the "get" call).
    //
    //  This issue already led to problems with the "PseudoProp", as the
    //  "Counter" was not observed when the PseudoProp's value was never
    //  accessed. (6ff294cac716750adbb3c1db8d731b6a2d644fe8)
    //
    //  Changing to "calculate immediately" (in the background) seems to
    //  to solve the problem. However an initial attempt failed (the "compute"
    //  method was also removed, but used in client code (see
    //  ExpectedActualDifferenceImageWidget). Try again later...

    private final Function<DependencyCollector, T> function;
    private volatile boolean isComputing = true;
    private volatile boolean mustRecompute = false;
    private volatile ResultComputer currentResultComputer;

    /**
     * Holds a computed value and all observers currently registered to trigger
     * a re-computation when any aspect that influenced the computed value
     * changed.
     * <p>
     * Immedidately before re-computation the observers must be removed to
     * ensure they trigger no additional re-computations.
     */
    private class Result {
        final T value;
        final List<EventObserver<PropertyChanged>> observers;

        public Result(T value, List<EventObserver<PropertyChanged>> observers) {
            this.value = value;
            this.observers = observers;
        }

        public void stopNotifications() {
            for (EventObserver<PropertyChanged> o : observers) {
                removeObserver(o);
            }
        }
    }

    private class ResultComputer extends SwingWorker<Result, Object> {
        @Override
        protected Result doInBackground() {
            List<EventObserver<PropertyChanged>> observers = new ArrayList<>();
            DependencyCollector dependencyCollector =
                    newRecomputeTriggeringObserversForDependencies(observers);
            isComputing = true;
            mustRecompute = false;
            T v = function.apply(dependencyCollector);
            return new Result(v, observers);
        }

        @Override
        protected void done() {
            // We are done calculating, the new result is available, and using
            // getValue will return it, without blocking.
            //
            // Make this ResultComputer the current one, so clients will read
            // the latest value. Also post PropertyChanged events to tell
            // observers the value has changed (if it did change)
            //
            // Special detail: for the initial computation the "resultComputer"
            // is already "this", as it was assigned to it during
            // initialization. In that case calling "done()" changes nothing.
            //
            // It will not even post an PropertyChanged event because the code
            // compares the values of the same ResultComputer which will always
            // be equal. Not posting an event is intended, as up to this time
            // no client has received any value that may have changed. We are
            // just done computing the value and will pass it to clients after
            // "done".

            ResultComputer previousComputer = currentResultComputer;
            currentResultComputer = this;
            if (!hasEqualValue(previousComputer)) {
                postPropertyChanged();
            }
            if (mustRecompute) {
                recompute();
            } else {
                isComputing = false;
            }
        }

        public boolean hasValue() {
            return getState() == SwingWorker.StateValue.DONE;
        }

        public boolean hasEqualValue(@Nullable T value) {
            return Objects.equals(getValue(), value);
        }

        public boolean hasEqualValue(ResultComputer otherComputer) {
            return hasEqualValue(otherComputer.getValue());
        }

        public void ensureComputationIsStarted() {
            if (getState() == SwingWorker.StateValue.PENDING) {
                execute();
            }
        }

        public T getValue() {
            return getResult().value;
        }

        public Result getResult() {
            try {
                return get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    protected PropComputedBase(EventAPIForProp eventAPIForProp,
                               Function<DependencyCollector, T> function,
                               @Nullable Object otherSource,
                               @Nullable String otherPropertyName) {
        super(eventAPIForProp, otherSource, otherPropertyName);
        this.function = function;
        currentResultComputer = new ResultComputer();
    }

    /**
     * This "getter" _get is defined to allow code reuse between the Nullable
     * and non-Nullable variants of PropComputed (the concrete class' get method
     * defines the correct Nullable type and just calls the _get method)
     */
    protected @Nullable T _get() {
        currentResultComputer.ensureComputationIsStarted();

        return currentResultComputer.getValue();
    }

    /**
     * This "setter" {@code _set} is defined to allow code reuse between the
     * Nullable and non-Nullable variants of PropComputed.
     * <p>
     * The concrete class' {@code set} method defines the correct Nullable type
     * and just calls {@code _set}.
     */
    protected void _set(@Nullable T value) {
        // do nothing when value is equal to current value
        if (currentResultComputer.hasValue() && currentResultComputer.hasEqualValue(value)) {
            return;
        }

        // ignore value setters for computed properties, as the value is only
        // defined by the computation.
        // Instead, post a change event to make sure the objects depending on
        // this Prop use the currently computed value (especially that one that
        // may have tried to set a different value).
        postPropertyChanged();
    }

    private void dependenciesChanged() {
        // make sure to run the code in the EDT, in the same thread also
        // used be the SwingWorkers "done()" method, so the operations are
        // serialized.
        invokeLater(() -> {

            if (mustRecompute) {
                return;
            }
            mustRecompute = true;

            // when we are not computing at the moment start a new computation.
            // Otherwise at the end of the computation some other code will
            // check for the `mustRecompute` flag and recompute if required.
            if (!isComputing) {
                recompute();
            }
        });
    }

    private void recompute() {
        // we are about to start a new computation.
        // Make sure the "old" result computer does not trigger more events
        currentResultComputer.getResult().stopNotifications();

        new ResultComputer().execute();
    }

    private DependencyCollector newRecomputeTriggeringObserversForDependencies(List<EventObserver<PropertyChanged>> observers) {
        return prop -> observers.add(
                addPropertyObserver(prop, PropService.VALUE_PROPERTY_NAME,
                        e -> dependenciesChanged()));
    }

    public void compute() {
        invokeLater(this::recompute);
    }
}