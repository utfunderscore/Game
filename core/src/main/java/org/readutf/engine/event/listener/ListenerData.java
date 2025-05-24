package org.readutf.engine.event.listener;

public record ListenerData<T>(Class<T> type, TypedGameListener<T> listener) {}
