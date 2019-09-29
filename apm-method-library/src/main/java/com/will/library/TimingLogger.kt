package com.will.library

import android.os.SystemClock
import android.util.Log

import java.util.ArrayList

/**
 * A utility class to help log timings splits throughout a method call.
 * Typical usage is:
 *
 * <pre>
 * TimingLogger timings = new TimingLogger(TAG, "methodA");
 * // ... do some work A ...
 * timings.addSplit("work A");
 * // ... do some work B ...
 * timings.addSplit("work B");
 * // ... do some work C ...
 * timings.addSplit("work C");
 * timings.dumpToLog();
</pre> *
 *
 *
 * The dumpToLog call would add the following to the log:
 *
 * <pre>
 * D/TAG     ( 3459): methodA: begin
 * D/TAG     ( 3459): methodA:      9 ms, work A
 * D/TAG     ( 3459): methodA:      1 ms, work B
 * D/TAG     ( 3459): methodA:      6 ms, work C
 * D/TAG     ( 3459): methodA: end, 16 ms
</pre> *
 */
class TimingLogger
/**
 * Create and initialize a TimingLogger object that will log using
 * the specific tag. If the Log.isLoggable is not enabled to at
 * least the Log.VERBOSE level for that tag at creation time then
 * the addSplit and dumpToLog call will do nothing.
 * @param tag the log tag to use while logging the timings
 * @param label a string to be displayed with each log
 */
    (tag: String, label: String) {

    /**
     * The Log tag to use for checking Log.isLoggable and for
     * logging the timings.
     */
    private var mTag: String? = null

    /** A label to be included in every log.  */
    private var mLabel: String? = null

    /** Used to track whether Log.isLoggable was enabled at reset time.  */
    private var mDisabled: Boolean = false

    /** Stores the time of each split.  */
    internal var mSplits: ArrayList<Long>? = null

    /** Stores the labels for each split.  */
    internal var mSplitLabels: ArrayList<String>? = null

    init {
        reset(tag, label)
    }

    /**
     * Clear and initialize a TimingLogger object that will log using
     * the specific tag. If the Log.isLoggable is not enabled to at
     * least the Log.VERBOSE level for that tag at creation time then
     * the addSplit and dumpToLog call will do nothing.
     * @param tag the log tag to use while logging the timings
     * @param label a string to be displayed with each log
     */
    fun reset(tag: String, label: String) {
        mTag = tag
        mLabel = label
        reset()
    }

    /**
     * Clear and initialize a TimingLogger object that will log using
     * the tag and label that was specified previously, either via
     * the constructor or a call to reset(tag, label). If the
     * Log.isLoggable is not enabled to at least the Log.VERBOSE
     * level for that tag at creation time then the addSplit and
     * dumpToLog call will do nothing.
     */
    fun reset() {
        //        mDisabled = !Log.isLoggable(mTag, Log.VERBOSE);
        mDisabled = false
        if (mDisabled) return
        if (mSplits == null) {
            mSplits = ArrayList()
            mSplitLabels = ArrayList()
        } else {
            mSplits!!.clear()
            mSplitLabels?.clear()
        }
        addSplit(null)
    }

    /**
     * Add a split for the current time, labeled with splitLabel. If
     * Log.isLoggable was not enabled to at least the Log.VERBOSE for
     * the specified tag at construction or reset() time then this
     * call does nothing.
     * @param splitLabel a label to associate with this split.
     */
    fun addSplit(splitLabel: String?) {
        if (mDisabled) return
        val now = SystemClock.elapsedRealtime()
        mSplits!!.add(now)
        splitLabel?.let { mSplitLabels?.add(it) }
    }

    /**
     * Dumps the timings to the log using Log.d(). If Log.isLoggable was
     * not enabled to at least the Log.VERBOSE for the specified tag at
     * construction or reset() time then this call does nothing.
     */
    fun dumpToLog() {
        if (mDisabled) return
        Log.d(mTag, mLabel!! + ": begin")
        val first = mSplits!![0]
        var now = first
        for (i in 1 until mSplits!!.size) {
            now = mSplits!![i]
            val splitLabel = mSplitLabels?.get(i)
            val prev = mSplits!![i - 1]

            Log.d(mTag, mLabel + ":      " + (now - prev) + " ms, " + splitLabel)
        }
        Log.d(mTag, mLabel + ": end, " + (now - first) + " ms")
    }
}