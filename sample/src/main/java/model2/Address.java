package model2;

import android.os.Parcelable;

import auto.parcelgson.AutoParcelGson;
import auto.parcelgson.gson.annotations.SerializedName;

@AutoParcelGson
public abstract class Address implements Parcelable {
  public abstract double[] coordinates();

  @SerializedName("city_name")
  public abstract String cityName();

  public static Address create(double[] coordinates, String cityName) {
    return builder().coordinates(coordinates).cityName(cityName).build();
  }

  public static Builder builder() {
    return new AutoParcelGson_Address.Builder();
  }

  @AutoParcelGson.Builder
  public interface Builder {
    public Builder coordinates(double[] x);

    public Builder cityName(String x);

    public Address build();
  }

  @AutoParcelGson.Validate
  public void validate() {
    if (cityName().length() < 2) {
      throw new IllegalStateException("Not a city name");
    }
  }
}
