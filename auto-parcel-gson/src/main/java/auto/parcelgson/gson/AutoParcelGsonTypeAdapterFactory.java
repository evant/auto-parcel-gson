package auto.parcelgson.gson;

import auto.parcelgson.AutoParcelGson;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
      Class<?> autoValueClass = Class.forName(autoValueName);
      Type autoValueType = autoValueClass;
      // We need to copy over any parameterized types from the source class to the autovalue
      // one  because they are lost when doing {@code Class.forName()}.
      if (type.getType() instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type.getType();
        autoValueType = Types.newParameterizedType(autoValueClass, parameterizedType.getActualTypeArguments());
      }
      TypeAdapter<T> typeAdapter = (TypeAdapter<T>) gson.getDelegateAdapter(this, TypeToken.get(autoValueType));
      TYPE_MAP.put(type, typeAdapter);
      return typeAdapter;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load AutoParcelGson type " + autoValueName, e);
    }
  }
}
