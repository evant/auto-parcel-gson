package model1;

import com.google.gson.annotations.SerializedName;

public enum HeightBucket {
  @SerializedName("short")
  SHORT,
  @SerializedName("average")
  AVERAGE,
  @SerializedName("tall")
  TALL
}
