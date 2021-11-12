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

class BindingsImpl implements Bindings {
    private final EventAPIForProp eventAPIForProp;
    private final Set<BindingImpl<?>> allBindings = new HashSet<>();

    private class BindingImpl<T> implements Binding<T> {
        private final Object prop;
        private final EventObserver<PropertyChanged> sourceOfTruthObserver;
        private final EventObserver<PropertyChanged> propObserver;

        public BindingImpl(Prop<T> prop,
                           Prop<T> sourceOfTruth,
                           Runnable updatePropCode,
                           Runnable updateSourceOfTruthCode) {
            this.prop = prop;
            this.sourceOfTruthObserver = addPropertyObserver(sourceOfTruth,
                    e -> updatePropCode.run());
            this.propObserver = addPropertyObserver(prop,
                    e -> updateSourceOfTruthCode.run());
            prop.set(sourceOfTruth.get());
        }

        public BindingImpl(PropNullable<T> prop,
                           PropNullable<T> sourceOfTruth,
                           Runnable updatePropCode,
                           Runnable updateSourceOfTruthCode) {
            this.prop = prop;
            this.sourceOfTruthObserver = addPropertyObserver(sourceOfTruth,
                    e -> updatePropCode.run());
            this.propObserver = addPropertyObserver(prop,
                    e -> updateSourceOfTruthCode.run());
            prop.set(sourceOfTruth.get());
        }

        public void unbind() {
            if (allBindings.remove(this)) {
                removeObserver(sourceOfTruthObserver);
                removeObserver(propObserver);
            } else {
                //TODO: throw or Log or nothing?
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

        protected void sourceOfTruthChanged() {
            eventAPIForProp.postPropertyChanged(prop, PropService.VALUE_PROPERTY_NAME); //NON-NLS
        }
    }

    public BindingsImpl(EventAPIForProp eventAPIForProp) {
        this.eventAPIForProp = eventAPIForProp;
    }

    @Override
    public <T> Binding<T> bind(Prop<T> sourceOfTruth, Prop<T> prop) {
        //TODO can we check if the prop is already bound to a source of truth?

        BindingImpl<T> binding = new BindingImpl<>(prop, sourceOfTruth,
                () -> prop.set(sourceOfTruth.get()),
                () -> sourceOfTruth.set(prop.get()));
        allBindings.add(binding);
        return binding;
    }

    @Override
    public <T> Binding<T> bind(PropNullable<T> sourceOfTruth, PropNullable<T> prop) {
        //TODO can we check if the prop is already bound to a source of truth?

        BindingImpl<T> binding = new BindingImpl<>(prop, sourceOfTruth,
                () -> prop.set(sourceOfTruth.get()),
                () -> sourceOfTruth.set(prop.get()));
        allBindings.add(binding);
        return binding;
    }

    @Override
    public void bindSwingCode(Prop<?> prop, Runnable code) {
        runDependingSwingCode_helper(prop, code);
    }

    @Override
    public void bindSwingCode(PropNullable<?> prop, Runnable code) {
        runDependingSwingCode_helper(prop, code);
    }

    @Override
    public void close() {
        // unbind all Bindings.
        // iterate on copy as we will change allBindings while iterating
        for(BindingImpl<?> b: allBindings.toArray(new BindingImpl[0])){
            b.unbind();
        }
    }

    private void runDependingSwingCode_helper(Object prop, Runnable code) {
        // the initial run, in the current thread.
        code.run();

        // some more logic to cover the "run once, even after multiple changes"
        // feature.
        AtomicBoolean runPending = new AtomicBoolean(false);
        eventAPIForProp.addPropertyObserver(prop, PropService.VALUE_PROPERTY_NAME,
                e -> {
                    // only schedule a new run when there is not yet one pending
                    if (!runPending.getAndSet(true)) {
                        SwingUtilities.invokeLater(() -> {
                            // only run the code when it is still necessary
                            if (runPending.getAndSet(false)) {
                                code.run();
                            }
                        });
                    }
                });
    }
}
