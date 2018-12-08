package com.android.approjects.permissions.logger;

/**
 * Simple {@link LogNode} filter, removes everything except the message.
 * Useful for situations like on-screen log output where you don't want a lot of metadata displayed,
 * just easy-to-read message updates as they're happening.
 */
public class LogFilter implements LogNode {

    LogNode mNext;

    public LogFilter(LogNode next) {
        mNext = next;
    }

    public LogFilter() {

    }

    /**
     * Return the next LogNode in the chain.
     */
    public LogNode getNext() {
        return mNext;
    }

    /**
     * Sets the LogNode data will be sent to.
     */
    public void setNext(LogNode node) {
        mNext = node;
    }

    @Override
    public void println(int priority, String tag, String msg, Throwable tr) {
        if (mNext != null) {
            getNext().println(Log.NONE, null, msg, null);
        }
    }
}
