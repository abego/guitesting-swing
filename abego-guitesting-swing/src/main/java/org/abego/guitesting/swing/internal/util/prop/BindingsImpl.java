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

import javax.swing.SwingUtilities;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class BindingsImpl implements Bindings {

    private static final Logger LOGGER = Logger.getLogger(BindingsImpl.class.getName());
    private static final ThreadLocal<Integer> updateCount = ThreadLocal.withInitial(() -> 0);

    private final EventAPIForProp eventAPIForProp;
    private final Set<Binding<?>> allBindings = new HashSet<>();
    private final Set<EventObserver<?>> observers = new HashSet<>();

    private class Binding<T> {
        private final EventObserver<PropertyChanged> sourceOfTruthObserver;
        private final @Nullable EventObserver<PropertyChanged> propObserver;

        private Binding(Prop<T> prop,
                        Prop<T> sourceOfTruth,
                        Runnable updatePropCode,
                        Runnable updateSourceOfTruthCode) {
            this.sourceOfTruthObserver = addPropertyObserver(sourceOfTruth,
                    e -> runUpdate(updatePropCode));
            this.propObserver = addPropertyObserver(prop,
                    e -> runUpdate(updateSourceOfTruthCode));
            runUpdate(updatePropCode);
        }

        private Binding(PropNullable<T> prop,
                        PropNullable<T> sourceOfTruth,
                        Runnable updatePropCode,
                        Runnable updateSourceOfTruthCode) {
            this.sourceOfTruthObserver = addPropertyObserver(sourceOfTruth,
                    e -> runUpdate(updatePropCode));
            this.propObserver = addPropertyObserver(prop,
                    e -> runUpdate(updateSourceOfTruthCode));
            runUpdate(updatePropCode);
        }

        private Binding(AnyProp sourceOfTruth, Runnable updateCode) {
            this.sourceOfTruthObserver = addPropertyObserver(sourceOfTruth,
                    e -> runUpdate(updateCode));
            this.propObserver = null;
            runUpdate(updateCode);
        }

        public void unbind() {
            if (allBindings.remove(this)) {
                removeObserver(sourceOfTruthObserver);
                if (propObserver != null) {
                    removeObserver(propObserver);
                }
            } else {
                LOGGER.log(Level.FINE, "Binding not(/no longer) bound"); //NON-NLS
            }
        }

        protected EventObserver<PropertyChanged> addPropertyObserver(
                Object source,
                // any property name,
                // no extra condition,
                // use defaultDispatcher,
                Consumer<PropertyChanged> listener) {
            return eventAPIForProp.addPropertyObserver(source, listener);
        }

        protected void removeObserver(EventObserver<?> observer) {
            eventAPIForProp.removeObserver(observer);
        }
    }

    private <T> void addBinding(Prop<T> prop,
                                Prop<T> sourceOfTruth,
                                Runnable updatePropCode,
                                Runnable updateSourceOfTruthCode) {
        allBindings.add(
                new Binding<>(prop, sourceOfTruth, updatePropCode, updateSourceOfTruthCode));
    }

    private <T> void addBinding(PropNullable<T> prop,
                                PropNullable<T> sourceOfTruth,
                                Runnable updatePropCode,
                                Runnable updateSourceOfTruthCode) {
        allBindings.add(
                new Binding<>(prop, sourceOfTruth, updatePropCode, updateSourceOfTruthCode));
    }

    private <T> void addBinding(AnyProp sourceOfTruth, Runnable updateCode) {
        allBindings.add(
                new Binding<T>(sourceOfTruth, updateCode));
    }

    public BindingsImpl(EventAPIForProp eventAPIForProp) {
        this.eventAPIForProp = eventAPIForProp;
    }

    @Override
    public <T> void bind(Prop<T> sourceOfTruth, Prop<T> prop) {

        addBinding(prop, sourceOfTruth,
                () -> prop.set(sourceOfTruth.get()),
                () -> sourceOfTruth.set(prop.get()));
    }

    @Override
    public <T> void bind(PropNullable<T> sourceOfTruth, PropNullable<T> prop) {

        addBinding(prop, sourceOfTruth,
                () -> prop.set(sourceOfTruth.get()),
                () -> sourceOfTruth.set(prop.get()));
    }

    @Override
    public <T> void bindTo(Consumer<T> consumer, Prop<T> prop) {
        addBinding(prop, () -> consumer.accept(prop.get()));
    }

    @Override
    public <T> void bindTo(Consumer<@Nullable T> consumer, PropNullable<T> prop) {
        addBinding(prop, () -> consumer.accept(prop.get()));
    }

    @Override
    public <T> void bindSwingCodeTo(Consumer<T> consumer, Prop<T> prop) {
        bindSwingCode(() -> consumer.accept(prop.get()), prop);
    }

    @Override
    public <T> void bindSwingCodeTo(Consumer<@Nullable T> consumer, PropNullable<T> prop) {
        bindSwingCode(() -> consumer.accept(prop.get()), prop);
    }

    @Override
    public void bindSwingCode(Runnable code, AnyProp... props) {
        // the initial run, in the current thread.
        runUpdate(code);

        Consumer<PropertyChanged> onPropChanged = newInEDTUpdater(code);

        for (AnyProp prop : props) {
            observers.add(eventAPIForProp.addPropertyObserver
                    (prop, PropService.VALUE_PROPERTY_NAME, onPropChanged));
        }
    }

    private void runUpdate(Runnable runnable) {
        updateCount.set(updateCount.get() + 1);
        try {
            runnable.run();
        } finally {
            updateCount.set(updateCount.get() - 1);
        }
    }

    public boolean isUpdating() {
        return updateCount.get() > 0;
    }

    private Consumer<PropertyChanged> newInEDTUpdater(Runnable code) {
        // some more logic to cover the "run once, even after multiple changes"
        // feature.
        AtomicBoolean runPending = new AtomicBoolean(false);
        return e -> {
            // only schedule a new run when there is not yet one pending
            if (!runPending.getAndSet(true)) {
                SwingUtilities.invokeLater(() -> {
                    // only run the code when it is still necessary
                    if (runPending.getAndSet(false)) {
                        runUpdate(code);
                    }
                });
            }
        };
    }

    @Override
    public void close() {
        // unbind all Bindings.

        // iterate on copy as we will change allBindings while iterating
        for (Binding<?> b : allBindings.toArray(new Binding[0])) {
            b.unbind();
        }
        // remove the bindSwingCode observers
        for (EventObserver<?> o : observers) {
            eventAPIForProp.removeObserver(o);
        }
    }

}
