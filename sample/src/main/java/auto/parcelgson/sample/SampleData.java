package auto.parcelgson.sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import auto.parcelgson.gson.AutoParcelGsonTypeAdapterFactory;
import model1.HeightBucket;
import model2.Address;
import model3.Person;

public class SampleData {


    static final Person ALICE = Person.builder().name("Alice").id(1L).heightType(HeightBucket.AVERAGE)
            .addresses(new HashMap<String, Address>() {{
                put("home", Address.create(new double[] { 0.3, 0.7 }, "Rome"));
            }}).friends(Collections.<Person>emptyList()).build();

    static final Person BOB = Person.builder().name("Bob").id(2L)
            .heightType(HeightBucket.TALL)
            .addresses(new HashMap<String, Address>() {{
                put("home", Address.create(new double[] { 3.2, 143.2 }, "Turin"));
                put("work", Address.create(new double[] { 5.9, 156.1 }, "Genoa"));
            }}).friends(Arrays.asList(ALICE)).build();

  public static Person getSampleData() {
//    return BOB;
    return new GsonBuilder().registerTypeAdapterFactory(new AutoParcelGsonTypeAdapterFactory()).create().fromJson(
        "{\"name\": \"Bob\"," +
            "\"id\": 2," +
            "\"height_type\": \"tall\"," +
            "\"addresses\": {" +
            "\"home\": {\"coordinates\": [3.2, 143.2], \"city_name\": \"Turin\"}," +
            "\"work\": {\"coordinates\": [5.9, 156.1], \"city_name\": \"Genoa\"}}," +
            "\"friends\": [{" +
            "\"name\": \"Alice\"," +
            "\"id\": 1," +
            "\"height_type\": \"average\"," +
            "\"addresses\": {" +
            "\"home\": {\"coordinates\": [0.3, 0.7], \"city_name\": \"Rome\"}}," +
            "\"friends\": []}]}", Person.class);
  }
}
