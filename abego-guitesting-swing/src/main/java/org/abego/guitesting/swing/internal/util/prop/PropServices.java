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

import org.abego.event.EventService;
import org.abego.event.EventServices;

public class PropServices {
    PropServices() {
        throw new UnsupportedOperationException();
    }

    public static PropService getDefault() {
        return PropServiceDefault.getDefault();
    }

    /**
     * Create a new PropService, using the given {@link EventService}.
     * <p>
     * The {@link PropService} uses an {@link EventService} for its inner
     * working. With {@link PropServices#getDefault()} the default
     * {@link EventService} ({@link EventServices#getDefault()}) is used. When
     * you want to create a new PropService that uses a different EventService
     * use this {@link #newPropService(EventService)} method.
     * <p>
     * Make sure to use the same EventService olso with other objects working
     * on Prop instances created with the PropService. Especially be aware of
     * the fact that events never leave an EventService. E.g. an observer
     * created by the default EventService will not be informed when events
     * are posted by a different EventService.
     */
    public static PropService newPropService(EventService eventService) {
        return PropServiceDefault.newPropService(eventService);
    }

    public static Props newProps() {
        return getDefault().newProps();
    }

}
