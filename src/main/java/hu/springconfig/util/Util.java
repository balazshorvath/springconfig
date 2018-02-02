package hu.springconfig.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Util {

    public static boolean notNullAndNotEmpty(String str) {
        return str != null && !str.equals("");
    }

    public static boolean notNullAndNotEmpty(Collection collection) {
        return collection != null && !collection.isEmpty();
    }

    public static class ArrayListBuilder<T> {
        private Collection<T> collection;

        private ArrayListBuilder(Collection<T> collection) {
            this.collection = collection;
        }

        public static <E> ArrayListBuilder<E> newList() {
            return new ArrayListBuilder<>(new ArrayList<>());
        }

        public static <E> ArrayListBuilder<E> newSet() {
            return new ArrayListBuilder<>(new HashSet<>());
        }

        public ArrayListBuilder<T> add(T t) {
            this.collection.add(t);
            return this;
        }

        public Collection<T> get() {
            return collection;
        }

    }
}
