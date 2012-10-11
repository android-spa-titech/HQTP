package org.hqtp.android;

import static org.junit.Assert.*;

import org.hqtp.android.util.HQTPTestRunner;
import org.hqtp.android.util.RoboGuiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(HQTPTestRunner.class)
public class ImageLoaderTest extends RoboGuiceTest {
    @Inject
    ImageLoader loader;

    @Test
    public void loaderShouldDisplayImage() {
        fail("Not yet implemented"); // TODO
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ImageLoader.class).to(ImageLoaderImpl.class);
        }
    }

    @Before
    public void setUp() {
        setUpRoboGuice(new TestModule());
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
