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

import org.eclipse.jdt.annotation.Nullable;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class PropBase<T> {
    //TODO: make this private?
    protected final EventAPIForProp eventAPIForProp;
    private final @Nullable Object otherSource;
    private final @Nullable String otherPropertyName;

    protected PropBase(EventAPIForProp eventAPIForProp,
                       @Nullable Object otherSource,
                       @Nullable String otherPropertyName) {
        this.eventAPIForProp = eventAPIForProp;
        this.otherSource = otherSource;
        this.otherPropertyName = otherPropertyName;
    }

    protected void postPropertyChanged() {
        eventAPIForProp.postPropertyChanged(this, PropService.VALUE_PROPERTY_NAME); //NON-NLS
        if (otherSource != null && otherPropertyName != null) {
            eventAPIForProp.postPropertyChanged(otherSource, otherPropertyName);
        }
    }
}
