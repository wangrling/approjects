package com.android.approjects.permissions.logger;

import android.util.Log;

import com.android.approjects.permissions.logger.LogNode;

/**
 * Helper class which wraps Android's native Log utility in the Logger interface.  This way
 * normal DDMS output can be one of the many targets receiving and outputting logs simultaneously.
 */
public class LogWrapper implements LogNode {

    // For piping: The next node to receive Log data after this one has done its work.
    private LogNode mNext;

    /**
     * @return the next LogNode in the linked list.
     */
    public LogNode getNext() {
        return mNext;
    }

    /**
     * @param node  the LogNode data will be sent to..
     */
    public void setNext(LogNode node) {
        mNext = node;
    }

    @Override
    public void println(int priority, String tag, String msg, Throwable tr) {
        // There actually are log methods that don't take a msg parameter.  For now,
        // if that's the case, just convert null to the empty string and move on.
        String useMsg = msg;
        if (useMsg == null) {
            useMsg = "";
        }

        // If an exeption was provided, convert that exception to a usable string and attach
        // it to the end of the msg method.
        if (tr != null) {
            msg += "\n" + Log.getStackTraceString(tr);
        }

        // This is functionally identical to Log.x(tag, useMsg);
        Log.println(priority, tag, useMsg);

        // If this isn't the last node in the chain, move things along.
        if (mNext != null) {
            mNext.println(priority, tag, msg, tr);
        }
    }
}
