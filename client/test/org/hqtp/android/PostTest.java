package org.hqtp.android;

import java.util.Calendar;
import java.util.Date;

import org.hqtp.android.util.HQTPTestRunner;
import org.hqtp.android.util.RoboGuiceTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(HQTPTestRunner.class)
public class PostTest extends RoboGuiceTest {
    private Date testDate;

    @Before
    public void setUp() {
        Calendar cal = Calendar.getInstance();
        // The month field is zero-origin.
        cal.set(2012, 9 - 1, 30, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        testDate = cal.getTime();
    }

    @Test
    public void shouldConvertDateProperly() throws Exception {
        assertThat(Post.dateToVirtualTimestamp(testDate), equalTo(134893080000000000L));
    }

    @Test
    public void shouldConvertVirtualTSProperly() throws Exception {
        assertThat(Post.virtualTimestampToDate(134893080000000000L), equalTo(testDate));
    }
}
