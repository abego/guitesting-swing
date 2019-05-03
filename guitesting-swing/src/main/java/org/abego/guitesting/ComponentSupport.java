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

package org.abego.guitesting;

import org.abego.commons.seq.Seq;

import java.awt.Component;
import java.util.function.Predicate;

import static org.abego.commons.seq.SeqUtil.newSeq;

public interface ComponentSupport {


    <T extends Component> Seq<T> allComponentsWith(Class<T> componentClass, Seq<Component> roots, Predicate<T> condition);

    <T extends Component> Seq<T> allComponentsWith(Class<T> componentClass, Predicate<T> condition);

    default <T extends Component> Seq<T> allComponentsWith(Class<T> componentClass, Component root, Predicate<T> condition) {
        return allComponentsWith(componentClass, newSeq(root), condition);
    }


    default <T extends Component> boolean hasComponentWith(Class<T> componentClass, Seq<Component> roots, Predicate<T> condition) {
        return !allComponentsWith(componentClass, roots, condition).isEmpty();
    }

    default <T extends Component> boolean hasComponentWith(Class<T> componentClass, Component root, Predicate<T> condition) {
        return !allComponentsWith(componentClass, root, condition).isEmpty();
    }

    default <T extends Component> boolean hasComponentWith(Class<T> componentClass, Predicate<T> condition) {
        return !allComponentsWith(componentClass, condition).isEmpty();
    }


    default <T extends Component> T componentWith(Class<T> componentClass, Seq<Component> roots, Predicate<T> condition) {
        return allComponentsWith(componentClass, roots, condition).singleItem();
    }

    default <T extends Component> T componentWith(Class<T> componentClass, Component root, Predicate<T> condition) {
        return allComponentsWith(componentClass, root, condition).singleItem();
    }

    default <T extends Component> T componentWith(Class<T> componentClass, Predicate<T> condition) {
        return allComponentsWith(componentClass, condition).singleItem();
    }


    default <T extends Component> T anyComponentWith(Class<T> componentClass, Seq<Component> roots, Predicate<T> condition) {
        return allComponentsWith(componentClass, roots, condition).anyItem();
    }

    default <T extends Component> T anyComponentWith(Class<T> componentClass, Component root, Predicate<T> condition) {
        return allComponentsWith(componentClass, root, condition).anyItem();
    }

    default <T extends Component> T anyComponentWith(Class<T> componentClass, Predicate<T> condition) {
        return allComponentsWith(componentClass, condition).anyItem();
    }
}
