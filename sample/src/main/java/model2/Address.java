package model2;

import auto.parcelgson.AutoParcelGson;
import android.os.Parcelable;

@AutoParcelGsonGson
public abstract class Address implements Parcelable {
  public abstract double[] coordinates();
  public abstract String cityName();

  public static Address create(double[] coordinates, String cityName) {
      return builder().coordinates(coordinates).cityName(cityName).build();
  }

  public static Builder builder() {
      return new AutoParcelGson_Address.Builder();
  }

  @AutoParcelGsonGson.Builder
  public interface Builder {
      public Builder coordinates(double[] x);
      public Builder cityName(String x);
      public Address build();
  }

  @AutoParcelGsonGson.Validate
  public void validate() {
      if (cityName().length() < 2) {
          throw new IllegalStateException("Not a city name");
      }
  }
}
