package auto.parcelgson.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import auto.parcelgson.AutoParcelGson;

/**
 * Created by evan on 2/24/15.
 */
public class AutoParcelGsonTypeAdapterFactory implements TypeAdapterFactory {
  private static final ConcurrentMap<TypeToken<?>, TypeAdapter<?>> TYPE_MAP = new ConcurrentHashMap<TypeToken<?>, TypeAdapter<?>>();

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Class<? super T> rawType = type.getRawType();
    if (!rawType.isAnnotationPresent(AutoParcelGson.class)) {
      return null;
    }
    
    TypeAdapter<T> adapter = (TypeAdapter<T>) TYPE_MAP.get(type);
    if (adapter != null) {
      return adapter;
    }

    String packageName = rawType.getPackage().getName();
    String className = rawType.getName().substring(packageName.length() + 1).replace('$', '_');
    String autoValueName = packageName + ".AutoParcelGson_" + className;

    try {
      Class<?> autoValueType = Class.forName(autoValueName);
      TypeAdapter<T> typeAdapter = (TypeAdapter<T>) gson.getAdapter(autoValueType);
      TYPE_MAP.put(type, typeAdapter);
      return typeAdapter;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load AutoParcelGson type " + autoValueName, e);
    }
  }
}
