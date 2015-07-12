package model3;

import android.os.Parcelable;

import java.util.List;
import java.util.Map;

import auto.parcelgson.AutoParcelGson;
import auto.parcelgson.gson.annotations.SerializedName;
import model1.HeightBucket;
import model2.Address;

@AutoParcelGson
public abstract class Person implements Parcelable {
  public static Person create(String name, long id, HeightBucket heightType, Map<String, Address> addresses,
                              List<Person> friends) {
    return builder().name(name).id(id).heightType(heightType)
        .addresses(addresses).friends(friends).build();
  }

  public abstract String name();

  public abstract long id();

  @SerializedName("height_type")
  public abstract HeightBucket heightType();

  public abstract Map<String, Address> addresses();

  public abstract List<Person> friends();

  @AutoParcelGson.Builder
  public abstract static class Builder {
      public abstract Builder name(String s);
      public abstract Builder id(long n);
      public abstract Builder heightType(HeightBucket x);
      public abstract Builder addresses(Map<String, Address> x);
      public abstract Builder friends(List<Person> x);
      public abstract Person build();
  }

  public static Builder builder() {
      return new AutoParcelGson_Person.Builder();
  }

  public abstract Builder toBuilder();

}
