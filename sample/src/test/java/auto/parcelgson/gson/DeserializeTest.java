package auto.parcelgson.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import modeltest.Basic;
import modeltest.Generic;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class DeserializeTest {
    Gson gson;

    @Before
    public void setup() {
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new AutoParcelGsonTypeAdapterFactory())
                .create();
    }

    @Test
    public void deserializeBasic() {
        String json = "{\"foo\":\"bar\"}";
        Basic basic = gson.fromJson(json, Basic.class);

        assertNotNull(basic);
        assertEquals("bar", basic.foo());
    }

    @Test
    public void deserializeGeneric() {
        String json = "{\"foo\":{\"foo\":\"bar\"}}";
        Generic<Basic> generic = gson.fromJson(json, new TypeToken<Generic<Basic>>() {
        }.getType());

        assertNotNull(generic);
        assertNotNull(generic.foo());
        assertEquals("bar", generic.foo().foo());
    }

}
