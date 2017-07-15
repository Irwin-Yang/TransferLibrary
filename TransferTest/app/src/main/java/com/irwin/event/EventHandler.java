package com.irwin.event;

/**
 * Created by Irwin on 2015/12/28.
 */
public interface EventHandler {
    /**
     * Handle event.
     *
     * @param event
     * @return Code reserved.
     */
    public int handle(Event event);
}
