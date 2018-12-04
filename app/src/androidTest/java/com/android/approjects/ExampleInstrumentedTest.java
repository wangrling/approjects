package com.android.approjects;

import android.app.Instrumentation;
import android.content.Context;

import org.junit.Test;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context appContext = instrumentation.getTargetContext();

        assertEquals("com.android.approjects", appContext.getPackageName());
    }
}
