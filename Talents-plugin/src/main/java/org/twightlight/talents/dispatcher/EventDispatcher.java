package org.twightlight.talents.dispatcher;

import org.bukkit.event.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

/**
 * High-performance event dispatcher that routes Bukkit events to registered CustomListener methods.
 * Each dispatcher instance maintains its own independent caches.
 */
public class EventDispatcher {

    /**
     * Priority levels for event handlers.
     * Lower values execute first.
     */
    public enum Priority {
        LOWEST(0),
        LOW(1),
        NORMAL(2),
        HIGH(3),
        HIGHEST(4),
        MONITOR(5);  // Executes last, should not modify event state

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Annotation to specify handler priority.
     * If not present, defaults to NORMAL.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ListenerPriority {
        Priority value() default Priority.NORMAL;
    }

    /**
     * Cached handler method info to avoid reflection overhead during dispatch
     */
    private static final class HandlerMethod {
        final Method method;
        final Class<? extends Event> eventType;
        final Priority priority;

        HandlerMethod(Method method, Class<? extends Event> eventType, Priority priority) {
            this.method = method;
            this.eventType = eventType;
            this.priority = priority;
        }
    }

    /**
     * Holds a listener and its cached handler methods
     */
    private static final class RegisteredListener {
        final CustomListener listener;
        final HandlerMethod[] methods;

        RegisteredListener(CustomListener listener, HandlerMethod[] methods) {
            this.listener = listener;
            this.methods = methods;
        }
    }

    /**
     * A single dispatch target: listener + method to invoke
     */
    private static final class DispatchTarget {
        final CustomListener listener;
        final Method method;
        final Priority priority;

        DispatchTarget(CustomListener listener, Method method, Priority priority) {
            this.listener = listener;
            this.method = method;
            this.priority = priority;
        }
    }

    // Comparator for sorting dispatch targets by priority
    private static final Comparator<DispatchTarget> PRIORITY_COMPARATOR =
            Comparator.comparingInt(t -> t.priority.getValue());

    // Empty array constants to avoid allocation
    private static final HandlerMethod[] EMPTY_HANDLER_METHODS = new HandlerMethod[0];
    private static final DispatchTarget[] EMPTY_DISPATCH_TARGETS = new DispatchTarget[0];

    // Instance-level cache: listener class -> handler methods (per dispatcher)
    private final HashMap<Class<? extends CustomListener>, HandlerMethod[]> methodCache = new HashMap<>();

    // Registered listeners
    private final ArrayList<RegisteredListener> listeners = new ArrayList<>();

    // Lock for registration (dispatch is lock-free via copy-on-write)
    private final Object registrationLock = new Object();

    // Volatile snapshot for lock-free dispatch
    private volatile DispatchSnapshot snapshot = new DispatchSnapshot(
            new RegisteredListener[0],
            new HashMap<>()
    );

    /**
     * Immutable snapshot for lock-free dispatch
     */
    private static final class DispatchSnapshot {
        final RegisteredListener[] listeners;
        final HashMap<Class<? extends Event>, DispatchTarget[]> cache;

        DispatchSnapshot(RegisteredListener[] listeners, HashMap<Class<? extends Event>, DispatchTarget[]> cache) {
            this.listeners = listeners;
            this.cache = cache;
        }
    }

    /**
     * Register a CustomListener whose methods will receive events.
     *
     * @param listener The listener to register
     */
    public void register(CustomListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        Class<? extends CustomListener> clazz = listener.getClass();

        synchronized (registrationLock) {
            HandlerMethod[] methods = getOrCacheHandlerMethods(clazz);

            if (methods.length == 0) {
                return; // No handler methods, skip
            }

            listeners.add(new RegisteredListener(listener, methods));
            rebuildSnapshot();
        }

        try {
            listener.onRegister();
        } catch (Exception e) {
            handleCallbackException(listener, "onRegister", e);
        }
    }

    /**
     * Unregister a listener.
     *
     * @param listener The listener to unregister
     * @return true if the listener was found and removed
     */
    public boolean unregister(CustomListener listener) {
        if (listener == null) {
            return false;
        }

        boolean removed;
        synchronized (registrationLock) {
            removed = listeners.removeIf(r -> r.listener == listener);
            if (removed) {
                rebuildSnapshot();
            }
        }

        if (removed) {
            try {
                listener.onUnregister();
            } catch (Exception e) {
                handleCallbackException(listener, "onUnregister", e);
            }
        }

        return removed;
    }

    /**
     * Check if a listener is registered.
     */
    public boolean isRegistered(CustomListener listener) {
        if (listener == null) {
            return false;
        }
        RegisteredListener[] snap = snapshot.listeners;
        for (int i = 0; i < snap.length; i++) {
            if (snap[i].listener == listener) {
                return true;
            }
        }
        return false;
    }

    /**
     * Dispatch an event to all registered listeners that accept it.
     * Lock-free, optimized for hot-path.
     * Handlers are invoked in priority order (LOWEST first, MONITOR last).
     *
     * @param event The event to dispatch
     */
    public void dispatch(Event event) {
        if (event == null) {
            return;
        }

        DispatchTarget[] targets = getTargets(event.getClass());

        for (int i = 0; i < targets.length; i++) {
            invokeTarget(targets[i], event);
        }
    }

    /**
     * Dispatch an event and return whether any handler was invoked.
     *
     * @param event The event to dispatch
     * @return true if at least one handler was invoked
     */
    public boolean dispatchWithResult(Event event) {
        if (event == null) {
            return false;
        }

        DispatchTarget[] targets = getTargets(event.getClass());

        if (targets.length == 0) {
            return false;
        }

        for (int i = 0; i < targets.length; i++) {
            invokeTarget(targets[i], event);
        }

        return true;
    }

    /**
     * Get the number of registered listeners.
     */
    public int getListenerCount() {
        return snapshot.listeners.length;
    }

    /**
     * Get all registered listeners (returns a copy).
     */
    public List<CustomListener> getListeners() {
        RegisteredListener[] snap = snapshot.listeners;
        List<CustomListener> result = new ArrayList<>(snap.length);
        for (int i = 0; i < snap.length; i++) {
            result.add(snap[i].listener);
        }
        return result;
    }

    /**
     * Clear all registered listeners.
     */
    public void clear() {
        List<CustomListener> toNotify;

        synchronized (registrationLock) {
            toNotify = new ArrayList<>(listeners.size());
            for (int i = 0, size = listeners.size(); i < size; i++) {
                toNotify.add(listeners.get(i).listener);
            }
            listeners.clear();
            methodCache.clear();
            snapshot = new DispatchSnapshot(new RegisteredListener[0], new HashMap<>());
        }

        for (int i = 0, size = toNotify.size(); i < size; i++) {
            CustomListener listener = toNotify.get(i);
            try {
                listener.onUnregister();
            } catch (Exception e) {
                handleCallbackException(listener, "onUnregister", e);
            }
        }
    }

    /**
     * Clear the dispatch cache. Call this if you need to force re-computation
     * of dispatch targets (rarely needed).
     */
    public void clearDispatchCache() {
        synchronized (registrationLock) {
            rebuildSnapshot();
        }
    }

    // ==================== Internal Methods ====================

    /**
     * Get dispatch targets for an event class.
     */
    private DispatchTarget[] getTargets(Class<? extends Event> eventClass) {
        DispatchSnapshot snap = this.snapshot; // Single volatile read
        DispatchTarget[] targets = snap.cache.get(eventClass);

        if (targets == null) {
            targets = computeAndCacheTargets(eventClass, snap);
        }

        return targets;
    }

    /**
     * Invoke a dispatch target with error handling.
     */
    private void invokeTarget(DispatchTarget target, Event event) {
        try {
            target.method.invoke(target.listener, event);
        } catch (Exception e) {
            boolean suppressed = false;
            try {
                suppressed = target.listener.onDispatchError(event, target.method.getName(), e);
            } catch (Exception ignored) {
            }

            if (!suppressed) {
                handleDispatchException(target.listener, target.method, event, e);
            }
        }
    }

    /**
     * Get or compute handler methods for a listener class.
     * Called under registrationLock.
     */
    private HandlerMethod[] getOrCacheHandlerMethods(Class<? extends CustomListener> clazz) {
        HandlerMethod[] cached = methodCache.get(clazz);
        if (cached != null) {
            return cached;
        }

        List<HandlerMethod> methodList = null;

        for (Method method : clazz.getMethods()) {
            if (method.getDeclaringClass() == CustomListener.class) {
                continue;
            }

            Class<?>[] params = method.getParameterTypes();

            if (params.length != 1) {
                continue;
            }

            Class<?> paramType = params[0];
            if (!Event.class.isAssignableFrom(paramType)) {
                continue;
            }

            if (methodList == null) {
                methodList = new ArrayList<>();
            }

            method.setAccessible(true);

            // Extract priority from annotation
            Priority priority = Priority.NORMAL;
            ListenerPriority priorityAnnotation = method.getAnnotation(ListenerPriority.class);
            if (priorityAnnotation != null) {
                priority = priorityAnnotation.value();
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) paramType;
            methodList.add(new HandlerMethod(method, eventType, priority));
        }

        HandlerMethod[] methods = (methodList == null)
                ? EMPTY_HANDLER_METHODS
                : methodList.toArray(new HandlerMethod[0]);

        methodCache.put(clazz, methods);
        return methods;
    }

    /**
     * Rebuild the immutable snapshot after registration changes.
     * Called under registrationLock.
     */
    private void rebuildSnapshot() {
        RegisteredListener[] listenersArray = listeners.toArray(new RegisteredListener[0]);
        this.snapshot = new DispatchSnapshot(listenersArray, new HashMap<>());
    }

    /**
     * Compute and cache dispatch targets for an event class.
     * Targets are sorted by priority (LOWEST first, MONITOR last).
     */
    private DispatchTarget[] computeAndCacheTargets(Class<? extends Event> eventClass, DispatchSnapshot snap) {
        List<DispatchTarget> targetList = null;

        RegisteredListener[] registeredListeners = snap.listeners;

        for (int i = 0; i < registeredListeners.length; i++) {
            RegisteredListener registered = registeredListeners[i];
            HandlerMethod[] methods = registered.methods;

            for (int j = 0; j < methods.length; j++) {
                HandlerMethod hm = methods[j];

                if (hm.eventType.isAssignableFrom(eventClass)) {
                    if (targetList == null) {
                        targetList = new ArrayList<>();
                    }
                    targetList.add(new DispatchTarget(registered.listener, hm.method, hm.priority));
                }
            }
        }

        DispatchTarget[] targets;
        if (targetList == null) {
            targets = EMPTY_DISPATCH_TARGETS;
        } else {
            // Sort by priority
            targetList.sort(PRIORITY_COMPARATOR);
            targets = targetList.toArray(new DispatchTarget[0]);
        }

        // Cache in snapshot (benign race - same result)
        snap.cache.put(eventClass, targets);

        return targets;
    }

    /**
     * Handle exceptions during dispatch. Override for custom logging.
     */
    protected void handleDispatchException(CustomListener listener, Method method, Event event, Exception e) {
        System.err.println("[EventDispatcher] Exception dispatching " + event.getClass().getSimpleName() +
                " to " + listener.getClass().getSimpleName() + "." + method.getName());
        e.printStackTrace();
    }

    /**
     * Handle exceptions in listener callbacks. Override for custom logging.
     */
    protected void handleCallbackException(CustomListener listener, String callback, Exception e) {
        System.err.println("[EventDispatcher] Exception in " + listener.getClass().getSimpleName() +
                "." + callback + "()");
        e.printStackTrace();
    }
}
