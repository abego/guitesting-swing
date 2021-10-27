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

package org.abego.guitesting.swing.internal.util;

import org.abego.commons.var.Var;
import org.abego.event.EventObserver;
import org.abego.event.EventService;
import org.abego.event.EventServices;
import org.abego.event.PropertyChanged;
import org.eclipse.jdt.annotation.NonNull;

import java.util.function.Consumer;

import static org.abego.guitesting.swing.internal.util.Prop.newProp;

public class PropBindable<T> implements Var<T> {
    private final Consumer<T> onSourceOfTruthValueChanged;
    private Prop<T> prop;
    private EventObserver<PropertyChanged> observer;

    private PropBindable(@NonNull T initialValue, Consumer<T> onSourceOfTruthValueChanged) {
        prop = newProp(initialValue);
        this.onSourceOfTruthValueChanged = onSourceOfTruthValueChanged;
        observer = EventServices.getDefault().addPropertyObserver(
                prop, e -> onValueChanged(get()));
    }

    public static <T> PropBindable<T> newPropBindable(
            @NonNull T initialValue, Consumer<T> onSourceOfTruthValueChanged) {
        return new PropBindable<T>(initialValue, onSourceOfTruthValueChanged);
    }

    @Override
    public @NonNull T get() {
        return prop.get();
    }

    @Override
    public void set(@NonNull T value) {
        prop.set(value);
    }

    public void bindTo(Prop<T> sourceOfTruth) {
        EventService eventService = EventServices.getDefault();
        if (observer != null) {
            eventService.removeObserver(observer);
        }
        @NonNull T oldValue = get();
        prop = sourceOfTruth;

        observer = eventService.addPropertyObserver(prop,
                e -> onValueChanged(get()));
        if (!oldValue.equals(get())) {
            onValueChanged(get());
        }
    }

    private void onValueChanged(T newValue) {
        onSourceOfTruthValueChanged.accept(newValue);
        // when the source of truth changed also "I" changed
        EventServices.getDefault().postPropertyChanged(this, "value"); //NON-NLS
    }

}
