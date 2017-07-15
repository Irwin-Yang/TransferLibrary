package com.irwin.event;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Irwin on 2015/12/28.
 * TODO Add priority for handling.
 */
public class EventHub {
    private static final String TAG = EventHub.class.getSimpleName();
    private static final long   MAX_HANDLE_INTERVAL = 500;
    private final HashMap<Integer, List<EventHandler>> mHandlerMap = new HashMap<>();

    private static final Handler POST_HANDLER = new Handler(Looper.getMainLooper());

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public void register(EventHandler handler, int... eventTypes) {
        if (eventTypes == null || eventTypes.length == 0) {
            throw new IllegalArgumentException("Event type needed");
        }
        synchronized (mHandlerMap) {
            for (int i = 0; i < eventTypes.length; i++) {
                final Integer type = (Integer) eventTypes[i];
                List<EventHandler> list = mHandlerMap.get(type);
                if (list == null) {
                    list = new ArrayList<>();
                    mHandlerMap.put(type, list);
                }
                if (!list.contains(handler)) {
                    list.add(handler);
                }
            }
        }
    }

    public void unregister(EventHandler handler, int... eventTypes) {
        synchronized (mHandlerMap) {
            if (eventTypes == null || eventTypes.length == 0) {
                Collection<List<EventHandler>> collection = mHandlerMap.values();
                Iterator<List<EventHandler>> collectionIterator = collection.iterator();
                List<EventHandler> list;
                EventHandler tempHandler;
                while (collectionIterator.hasNext()) {
                    list = collectionIterator.next();
                    Iterator<EventHandler> handlerIterator = list.iterator();
                    while (handlerIterator.hasNext()) {
                        tempHandler = handlerIterator.next();
                        if (handler == tempHandler) {
                            handlerIterator.remove();
                        }
                    }
                }
            } else {
                for (int i = 0; i < eventTypes.length; i++) {
                    final Integer type = (Integer) eventTypes[i];
                    final List<EventHandler> list = mHandlerMap.get(type);
                    if (list == null || list.size() == 0) {
                        continue;
                    }
//                    EventHandler tempHandler;
//                    Iterator<EventHandler> handlerIterator = list.iterator();
//                    while (handlerIterator.hasNext()) {
//                        tempHandler = handlerIterator.next();
//                        if (handler == tempHandler) {
//                            handlerIterator.remove();
//                        }
//                    }
                    list.remove(handler);
                }
            }
        }
    }

    public void unregister(int eventType) {
        final Integer type = (Integer) eventType;
        synchronized (mHandlerMap) {
            mHandlerMap.remove(type);
        }
    }

    public void dispatch(Event event) {
        if (event == null) {
            throw new NullPointerException("Event can not be null.");
        }
        mExecutor.execute(new EventDispatcher(event));
    }

    public void dispatchOrdered(Event event) {
        if (event == null) {
            throw new NullPointerException("Event can not be null.");
        }
        event.setOrdered();
        mExecutor.execute(new EventDispatcher(event));
    }

    private class EventDispatcher implements Runnable {
        final Event Event;

        public EventDispatcher(Event event) {
            Event = event;
        }

        @Override
        public void run() {
            final Iterator<EventHandler> handlerIterator;
            synchronized (mHandlerMap) {
                Integer type = (Integer) Event.Type;
                List<EventHandler> list = mHandlerMap.get(type);
                if (list == null || list.size() == 0) {
                    return;
                }
                handlerIterator = new ArrayList<EventHandler>(list).iterator();
            }
            //TODO Is operation below thread safe?
            if (Event.isOrdered()) {
                handleNext(handlerIterator);
            } else {
                while (handlerIterator.hasNext()) {
                    final EventHandler handler = handlerIterator.next();
                    POST_HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            handleEvent(handler);
                        }
                    });
                }
            }
        }

        void handleNext(final Iterator<EventHandler> handlerIterator) {
            if (handlerIterator.hasNext()) {
                final EventHandler handler = handlerIterator.next();
                POST_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        handleEvent(handler);
                        if (!Event.isCancel()) {
                            handleNext(handlerIterator);
                        }
                    }
                });
            }
        }

        void handleEvent(final EventHandler handler) {
            try {
                long start = System.currentTimeMillis();
                handler.handle(Event);
                long end = System.currentTimeMillis();
                if (end - start > MAX_HANDLE_INTERVAL) {
                    Log.w(TAG, "Handle " + Event + " cost too much time:" + (end - start) + "ms  Handler:" + handler);
                }
            } catch (Exception e) {
                Log.w(TAG, "Eventhandler exception:\r\n");
                e.printStackTrace();
            }
        }
    }

}
