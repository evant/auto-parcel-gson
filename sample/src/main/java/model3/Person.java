package model3;

import android.os.Parcelable;

import java.util.List;
import java.util.Map;

import auto.parcelgson.AutoParcelGson;
import auto.parcelgson.gson.AutoParcelGsonTypeAdapterFactory;
import auto.parcelgson.gson.annotations.JsonAdapter;
import auto.parcelgson.gson.annotations.SerializedName;
import model1.HeightBucket;
import model2.Address;

@AutoParcelGson
public abstract class Person implements Parcelable {
  public static Person create(String name, long id, HeightBucket heightType, Map<String, Address> addresses,
                              List<Person> friends) {
    return new AutoParcelGson_Person(name, id, heightType, addresses, friends);
  }

  public abstract String name();

  public abstract long id();

  @SerializedName("height_type")
  public abstract HeightBucket heightType();

  public abstract Map<String, Address> addresses();

  public abstract List<Person> friends();
}
