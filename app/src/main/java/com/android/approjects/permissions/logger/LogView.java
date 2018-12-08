package com.android.approjects.permissions.logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

/** Simple TextView which is used to output log data received through the LogNode interface.
 */

@SuppressLint("AppCompatCustomView")
public class LogView extends TextView implements LogNode {

    LogNode mNext;

    public LogNode getNext() {
        return mNext;
    }

    public void setNext(LogNode next) {
        mNext = next;
    }

    public LogView(Context context) {
        super(context);
    }

    public LogView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LogView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public void println(int priority, String tag, String msg, Throwable tr) {
        String priorityStr = null;

        // For the purposes of this View, we want to print the priority as readable text.
        switch(priority) {
            case android.util.Log.VERBOSE:
                priorityStr = "VERBOSE";
                break;
            case android.util.Log.DEBUG:
                priorityStr = "DEBUG";
                break;
            case android.util.Log.INFO:
                priorityStr = "INFO";
                break;
            case android.util.Log.WARN:
                priorityStr = "WARN";
                break;
            case android.util.Log.ERROR:
                priorityStr = "ERROR";
                break;
            case android.util.Log.ASSERT:
                priorityStr = "ASSERT";
                break;
            default:
                break;
        }

        // Handily, the Log class has a facility for converting a stack trace into a usable string.
        String exceptionStr = null;
        if (tr != null) {
            exceptionStr = android.util.Log.getStackTraceString(tr);
        }

        // Take the priority, tag, message, and exception, and concatenate as necessary
        // into one usable line of text.
        final StringBuilder outputBuilder = new StringBuilder();

        String delimiter = "\t";
        appendIfNotNull(outputBuilder, priorityStr, delimiter);
        appendIfNotNull(outputBuilder, tag, delimiter);
        appendIfNotNull(outputBuilder, msg, delimiter);
        appendIfNotNull(outputBuilder, exceptionStr, delimiter);

        // In case this was originally called from an AsyncTask or some other off-UI thread,
        // make sure the update occurs within the UI thread.
        ((Activity) getContext()).runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                // Display the text we just generated within the LogView.
                appendToLog(outputBuilder.toString());
            }
        }));

        if (mNext != null) {
            mNext.println(priority, tag, msg, tr);
        }
    }

    /** Takes a string and adds to it, with a separator, if the bit to be added isn't null. Since
     * the logger takes so many arguments that might be null, this method helps cut out some of the
     * agonizing tedium of writing the same 3 lines over and over.
     * @param source StringBuilder containing the text to append to.
     * @param addStr The String to append
     * @param delimiter The String to separate the source and appended strings. A tab or comma,
     *                  for instance.
     * @return The fully concatenated String as a StringBuilder
     */
    private StringBuilder appendIfNotNull(StringBuilder source, String addStr, String delimiter) {
        if (addStr != null) {
            if (addStr.length() == 0) {
                delimiter = "";
            }

            return source.append(addStr).append(delimiter);
        }
        return source;
    }
    /**
     * Outputs the string as a new line of log data in the LogView.
     */
    public void appendToLog(String s) {
        append("\n" + s);
    }
}
