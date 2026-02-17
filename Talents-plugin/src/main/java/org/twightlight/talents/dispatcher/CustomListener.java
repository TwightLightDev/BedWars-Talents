package org.twightlight.talents.dispatcher;

import org.bukkit.event.Event;

/**
 * Base class for all event listeners that can be registered with EventDispatcher.
 * Extend this class and add methods with a single Event parameter to handle events.
 */
public interface CustomListener {

    /**
     * Called when this listener is registered to a dispatcher.
     * Override for initialization logic.
     */
    void onRegister();

    /**
     * Called when this listener is unregistered from a dispatcher.
     * Override for cleanup logic.
     */
    void onUnregister();

    /**
     * Called when an exception occurs during event dispatch to this listener.
     * Override for custom error handling.
     *
     * @param event The event being dispatched
     * @param method The method that threw
     * @param exception The exception that occurred
     * @return true to suppress the exception, false to let dispatcher handle it
     */
    boolean onDispatchError(Event event, String method, Exception exception);
}