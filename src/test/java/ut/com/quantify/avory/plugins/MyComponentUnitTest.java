package ut.com.quantify.avory.plugins;

import com.quantify.avory.plugins.api.MyPluginComponent;
import com.quantify.avory.plugins.impl.MyPluginComponentImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest {
    @Test
    public void testMyName() {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent", component.getName());
    }
}