package org.talend.logging.audit;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ContextBuilderTest {

    @Test(expected = UnsupportedOperationException.class)
    public void contextBuilderCreateImutableContextPut() {
        ContextBuilder builder = ContextBuilder.create();

        final Context context = builder.build();

        context.put("aa", "bb");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void contextBuilderCreateImutableContextPutAll() {
        ContextBuilder builder = ContextBuilder.create();

        final Context context = builder.build();

        context.putAll(Collections.<String, String> emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void contextBuilderCreateImutableContextRemove() {
        ContextBuilder builder = ContextBuilder.create();

        final Context context = builder.build();

        context.remove("aa");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void contextBuilderCreateImutableContextClear() {
        ContextBuilder builder = ContextBuilder.create();

        final Context context = builder.build();

        context.clear();
    }

    @Test()
    public void contextBuilderCreateDifferentContext() {
        ContextBuilder builder = ContextBuilder.create();
        final Context context = builder.build();

        builder.with("test", "test");
        final Context contextWithOne = builder.build();

        assertEquals(0, context.size());
        assertEquals(1, contextWithOne.size());
    }
}