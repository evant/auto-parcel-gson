Android AutoParcel
============

Port of Google AutoValue for Android with Parcelable generation goodies.

Why AutoValue?
--------

Because it's awesome.
I can't explain it better than [that](https://github.com/google/auto/tree/master/value).

Ok then why a fork for Android?
--------

Because AutoValue is not extensible [yet](https://github.com/google/auto/pull/87). This fork adds automatic Parcelable implementation for your POJOS. It's easy as just adding `implements Parcelable`.

Fine, how do I use it?
--------

```java
@AutoParcelGson
abstract class SomeModel implements Parcelable {
  abstract String name();
  abstract List<SomeSubModel> subModels();
  abstract Map<String, OtherSubModel> modelsMap();

  static SomeModel create(String name, List<SomeSubModel> subModels, Map<String, OtherSubModel> modelsMap) {
    return new AutoParcelGson_SomeModel(name, subModels, modelsMap);
  }
}
```

That's that simple. And you get `Parcelable`, `hashCode`, `equals` and `toString` implementations for free.
As your models evolve you don't need to worry about keeping all the boilerplate in sync with the new implementation, it's already taken care of.

Sounds great, where can I download it?
--------

The easy way is to use Gradle.

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.android.tools.build:gradle:+'
    classpath 'com.neenbedankt.gradle.plugins:android-apt:+'
  }
}

apply plugin: 'android'
apply plugin: 'android-apt'

dependencies {
  compile 'com.github.frankiesardo:auto-parcel:+'
  apt 'com.github.frankiesardo:auto-parcel-processor:+'
}
```

I recommend using the `android-apt` plugin so that Android Studio picks up the generated files.
Check out the sample project for a working example.

License
-------

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
