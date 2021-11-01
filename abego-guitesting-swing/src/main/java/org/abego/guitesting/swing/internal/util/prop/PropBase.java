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
import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;

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
//TODO: check if we can reuse some code of the different "Prop..." classes
abstract class PropBase<T> {
    //TODO: can we make this private?
    private final EventsForProp eventsForProp;
    private final @Nullable Object otherSource;
    private final @Nullable String otherPropertyName;

    protected PropBase(EventsForProp eventsForProp,
                       @Nullable Object otherSource,
                       @Nullable String otherPropertyName) {
        this.eventsForProp = eventsForProp;
        this.otherSource = otherSource;
        this.otherPropertyName = otherPropertyName;
    }

    /**
     * Runs a {@link Runnable} (the "code") "now" and whenever the Prop changes.
     */
    public void runDependingCode(Runnable code) {
        code.run();
        eventsForProp.addPropertyObserver(this, PropService.VALUE_PROPERTY_NAME, e -> code.run());
    }

    protected EventObserver<PropertyChanged> addPropertyObserver(
            Object source,
            // any property name,
            // no extra condition,
            // use defaultDispatcher,
            Consumer<PropertyChanged> listener) {
        return eventsForProp.addPropertyObserver(source, listener);
    }

    protected EventObserver<PropertyChanged> addPropertyObserver(
            Object source,
            @Nullable String propertyName,
            // no extra condition (just the property name),
            // use defaultDispatcher,
            Consumer<PropertyChanged> listener) {
        return eventsForProp.addPropertyObserver(source, propertyName, listener);
    }

    protected void removeObserver(EventObserver<?> observer) {
        eventsForProp.removeObserver(observer);
    }

    protected void postPropertyChanged() {
        eventsForProp.postPropertyChanged(this, PropService.VALUE_PROPERTY_NAME); //NON-NLS
        if (otherSource != null && otherPropertyName != null) {
            eventsForProp.postPropertyChanged(otherSource, otherPropertyName);
        }
    }
}
