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

    private final EventAPIForProp eventAPIForProp;
    private final Set<Binding<?>> allBindings = new HashSet<>();
    private final Set<EventObserver<?>> observers = new HashSet<>();

    private class Binding<T> {
        private final EventObserver<PropertyChanged> sourceOfTruthObserver;
        private final EventObserver<PropertyChanged> propObserver;

        public Binding(Prop<T> prop,
                       Prop<T> sourceOfTruth,
                       Runnable updatePropCode,
                       Runnable updateSourceOfTruthCode) {
            this.sourceOfTruthObserver = addPropertyObserver(sourceOfTruth,
                    e -> updatePropCode.run());
            this.propObserver = addPropertyObserver(prop,
                    e -> updateSourceOfTruthCode.run());
            updatePropCode.run();
        }

        public Binding(PropNullable<T> prop,
                       PropNullable<T> sourceOfTruth,
                       Runnable updatePropCode,
                       Runnable updateSourceOfTruthCode) {
            this.sourceOfTruthObserver = addPropertyObserver(sourceOfTruth,
                    e -> updatePropCode.run());
            this.propObserver = addPropertyObserver(prop,
                    e -> updateSourceOfTruthCode.run());
            updatePropCode.run();
        }

        public void unbind() {
            if (allBindings.remove(this)) {
                removeObserver(sourceOfTruthObserver);
                removeObserver(propObserver);
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

        protected EventObserver<PropertyChanged> addPropertyObserver(
                Object source,
                @Nullable String propertyName,
                // no extra condition (just the property name),
                // use defaultDispatcher,
                Consumer<PropertyChanged> listener) {
            return eventAPIForProp.addPropertyObserver(source, propertyName, listener);
        }

        protected void removeObserver(EventObserver<?> observer) {
            eventAPIForProp.removeObserver(observer);
        }
    }

    public BindingsImpl(EventAPIForProp eventAPIForProp) {
        this.eventAPIForProp = eventAPIForProp;
    }

    @Override
    public <T> void bind(Prop<T> sourceOfTruth, Prop<T> prop) {

        Binding<T> binding = new Binding<>(prop, sourceOfTruth,
                () -> prop.set(sourceOfTruth.get()),
                () -> sourceOfTruth.set(prop.get()));
        allBindings.add(binding);
    }

    @Override
    public <T> void bind(PropNullable<T> sourceOfTruth, PropNullable<T> prop) {

        Binding<T> binding = new Binding<>(prop, sourceOfTruth,
                () -> prop.set(sourceOfTruth.get()),
                () -> sourceOfTruth.set(prop.get()));
        allBindings.add(binding);
    }

    @Override
    public void bindSwingCode(Runnable code, AnyProp... props) {
        // the initial run, in the current thread.
        code.run();

        // some more logic to cover the "run once, even after multiple changes"
        // feature.
        AtomicBoolean runPending = new AtomicBoolean(false);
        Consumer<PropertyChanged> onPropChanged = e -> {
            // only schedule a new run when there is not yet one pending
            if (!runPending.getAndSet(true)) {
                SwingUtilities.invokeLater(() -> {
                    // only run the code when it is still necessary
                    if (runPending.getAndSet(false)) {
                        code.run();
                    }
                });
            }
        };

        for (AnyProp prop : props) {
            EventObserver<PropertyChanged> observer =
                    eventAPIForProp.addPropertyObserver
                            (prop, PropService.VALUE_PROPERTY_NAME, onPropChanged);
        }
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
