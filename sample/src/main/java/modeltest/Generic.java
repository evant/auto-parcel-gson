package modeltest;

import auto.parcelgson.AutoParcelGson;

@AutoParcelGson
public abstract class Generic<T> {
    public abstract T foo();
}
