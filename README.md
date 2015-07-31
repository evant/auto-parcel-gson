Android AutoParcelGson
============

Port of Google AutoValue for Android with Parcelable generation goodies _and_ [gson](https://code.google.com/p/google-gson/).

Why AutoValue?
--------

Because it's awesome.
I can't explain it better than [that](https://github.com/google/auto/tree/master/value).

Ok then why a fork for Android?
--------

Because AutoValue is not extensible [yet](https://github.com/google/auto/pull/87). This fork adds automatic Parcelable implementation for your POJOS. It's easy as just adding `implements Parcelable`.

Ok then why fork the fork?
--------

Same reason as the first fork, AutoValue is not extensible. This fork adds support for gson as well.


Fine, how do I use it?
--------

```java
import auto.parcelgson.gson.AutoParcelGsonTypeAdapterFactory;
import auto.parcelgson.AutoParcelGson;
import auto.parcelgson.gson.annotations.SerializedName;

@AutoParcelGson
abstract class SomeModel implements Parcelable {
  abstract String name();
  @SerializedName("sub_models");
  abstract List<SomeSubModel> subModels();
  @SerializedName("models_map");
  abstract Map<String, OtherSubModel> modelsMap();

  static SomeModel create(String name, List<SomeSubModel> subModels, Map<String, OtherSubModel> modelsMap) {
    return new AutoParcelGson_SomeModel(name, subModels, modelsMap);
  }
}

...

Gson gson = new GsonBuilder().registerTypeAdapterFactory(new AutoParcelGsonTypeAdapterFactory()).create();
SomeModel = gson.fromJson(jsonModel, SomeModel.class);
```

That's that simple. And you get `Parcelable`, `hashCode`, `equals` and `toString` implementations for free.
As your models evolve you don't need to worry about keeping all the boilerplate in sync with the new implementation, it's already taken care of.

All gson annotations are mirrored in `auto.parcelgson.gson.annotations` which work on methods instead of fields. They will be copied to the fields of the generated class. The provided `AutoParcelGsonTypeAdapterFactory` will take care of finding the right generated class to construct from the given `@AutoParcelGson` abstract class.

Can you make the work with jackson/my random json parser?
--------

Sorry nope, this is already a pretty brittle solution due to the lack of extensibility in autovalue. Feel free to make your own fork!

Sounds great, where can I download it?
--------

The easy way is to use Gradle.

```groovy
buildscript {
  repositories {
    mavenCentral()
    jcenter()
  }
  dependencies {
    classpath 'com.android.tools.build:gradle:+'
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
  }
}

apply plugin: 'com.android.application'
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
  compile 'me.tatarka:auto-parcel-gson:0.1'
  apt 'me.tatarka:auto-parcel-gson-processor:0.1'
}

repositories {
  mavenCentral()
  jcenter()
}
```

I recommend using the [`android-apt`](https://bitbucket.org/hvisser/android-apt) plugin so that Android Studio picks up the generated files.
Check out the sample project for a working example.

Proguard
--------
The Gson TypeAdapter uses reflection to map your abstract classes to the AutoParcelGson implementations. If you are using proguard you need to keep the classes.
```
 -keep class **.AutoParcelGson_*
 -keepnames @auto.parcelgson.AutoParcelGson class *
 ```

License
-------

    Copyright 2015 Evan Tatarka
    Copyright 2014 Frankie Sardo
    Copyright 2013 Google, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
