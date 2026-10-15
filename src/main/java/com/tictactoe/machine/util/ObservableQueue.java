package com.tictactoe.machine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ObservableQueue<T> {
    private final List<T> items = new ArrayList<>();
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public synchronized void add(T element) {
        items.add(element);
        notifyListeners(element);
    }

    public synchronized void addListener(Consumer<T> listener) {
        listeners.add(listener);
        // Dispatch existing logs to new listener immediately
        for (T item : items) {
            listener.accept(item);
        }
    }

    private void notifyListeners(T element) {
        for (Consumer<T> listener : listeners) {
            listener.accept(element);
        }
    }

    public synchronized List<T> getItems() {
        return new ArrayList<>(items);
    }

    public synchronized void clear() {
        items.clear();
    }
}
