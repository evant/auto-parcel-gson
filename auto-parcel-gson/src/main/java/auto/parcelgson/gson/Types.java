/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package auto.parcelgson.gson;

import java.io.Serializable;
import java.lang.reflect.*;
import java.security.AccessControlException;
import java.util.*;

/**
 * Utilities for working with {@link Type}.
 *
 * @author Ben Yu
 */
final class Types {

  /**
   * Returns a type where {@code rawType} is parameterized by {@code arguments}.
   */
  static ParameterizedType newParameterizedType(Class<?> rawType, Type... arguments) {
    return new ParameterizedTypeImpl(
        ClassOwnership.JVM_BEHAVIOR.getOwnerType(rawType), rawType, arguments);
  }

  /**
   * Decides what owner type to use for constructing {@link ParameterizedType} from a raw class.
   */
  private enum ClassOwnership {
    OWNED_BY_ENCLOSING_CLASS {
      @Override
      Class<?> getOwnerType(Class<?> rawType) {
        return rawType.getEnclosingClass();
      }
    },
    LOCAL_CLASS_HAS_NO_OWNER {
      @Override
      Class<?> getOwnerType(Class<?> rawType) {
        if (rawType.isLocalClass()) {
          return null;
        } else {
          return rawType.getEnclosingClass();
        }
      }
    };

    abstract Class<?> getOwnerType(Class<?> rawType);

    static final ClassOwnership JVM_BEHAVIOR = detectJvmBehavior();

    private static ClassOwnership detectJvmBehavior() {
      class LocalClass<T> {
      }
      Class<?> subclass = new LocalClass<String>() {
      }.getClass();
      ParameterizedType parameterizedType = (ParameterizedType) subclass.getGenericSuperclass();
      for (ClassOwnership behavior : ClassOwnership.values()) {
        if (behavior.getOwnerType(LocalClass.class) == parameterizedType.getOwnerType()) {
          return behavior;
        }
      }
      throw new AssertionError();
    }
  }

  /**
   * Returns a new {@link TypeVariable} that belongs to {@code declaration} with {@code name} and
   * {@code bounds}.
   */
  static <D extends GenericDeclaration> TypeVariable<D> newArtificialTypeVariable(
      D declaration, String name, Type... bounds) {
    return newTypeVariableImpl(
        declaration, name, (bounds.length == 0) ? new Type[]{Object.class} : bounds);
  }

  /**
   * Returns human readable string representation of {@code type}. <ul> <li>For array type {@code
   * Foo[]}, {@code "com.mypackage.Foo[]"} are returned. <li>For any class, {@code
   * theClass.getName()} are returned. <li>For all other types, {@code type.toString()} are
   * returned. </ul>
   */
  static String toString(Type type) {
    return (type instanceof Class) ? ((Class<?>) type).getName() : type.toString();
  }

  private static final class GenericArrayTypeImpl implements GenericArrayType, Serializable {

    private final Type componentType;

    GenericArrayTypeImpl(Type componentType) {
      this.componentType = JavaVersion.CURRENT.usedInGenericType(componentType);
    }

    @Override
    public Type getGenericComponentType() {
      return componentType;
    }

    @Override
    public String toString() {
      return Types.toString(componentType) + "[]";
    }

    @Override
    public int hashCode() {
      return componentType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof GenericArrayType) {
        GenericArrayType that = (GenericArrayType) obj;
        return equal(getGenericComponentType(), that.getGenericComponentType());
      }
      return false;
    }

    private static final long serialVersionUID = 0;
  }

  private static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {

    private final Type ownerType;
    private final List<Type> argumentsList;
    private final Class<?> rawType;

    ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type[] typeArguments) {
      this.ownerType = ownerType;
      this.rawType = rawType;
      this.argumentsList = JavaVersion.CURRENT.usedInGenericType(typeArguments);
    }

    @Override
    public Type[] getActualTypeArguments() {
      return toArray(argumentsList);
    }

    @Override
    public Type getRawType() {
      return rawType;
    }

    @Override
    public Type getOwnerType() {
      return ownerType;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      if (ownerType != null) {
        builder.append(JavaVersion.CURRENT.typeName(ownerType)).append('.');
      }
      builder.append(rawType.getName())
          .append('<');

      Iterator<Type> itr = argumentsList.iterator();
      while (itr.hasNext()) {
        Type type = itr.next();
        builder.append(JavaVersion.CURRENT.typeName(type));
        if (itr.hasNext()) {
          builder.append(", ");
        }
      }
      return builder.append('>')
          .toString();
    }

    @Override
    public int hashCode() {
      return (ownerType == null ? 0 : ownerType.hashCode())
          ^ argumentsList.hashCode()
          ^ rawType.hashCode();
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ParameterizedType)) {
        return false;
      }
      ParameterizedType that = (ParameterizedType) other;
      return getRawType().equals(that.getRawType())
          && equal(getOwnerType(), that.getOwnerType())
          && Arrays.equals(getActualTypeArguments(), that.getActualTypeArguments());
    }

    private static final long serialVersionUID = 0;
  }

  private static <D extends GenericDeclaration> TypeVariable<D> newTypeVariableImpl(
      D genericDeclaration, String name, Type[] bounds) {
    TypeVariableImpl<D> typeVariableImpl =
        new TypeVariableImpl<D>(genericDeclaration, name, bounds);
    @SuppressWarnings("unchecked")
    TypeVariable<D> typeVariable =
        newProxy(
            TypeVariable.class, new TypeVariableInvocationHandler(typeVariableImpl));
    return typeVariable;
  }

  /**
   * Invocation handler to work around a compatibility problem between Java 7 and Java 8.
   * <p/>
   * <p>Java 8 introduced a new method {@code getAnnotatedBounds()} in the {@link TypeVariable}
   * interface, whose return type {@code AnnotatedType[]} is also new in Java 8. That means that
   * we cannot implement that interface in source code in a way that will compile on both Java 7
   * and Java 8. If we include the {@code getAnnotatedBounds()} method then its return type means
   * it won't compile on Java 7, while if we don't include the method then the compiler will
   * complain that an abstract method is unimplemented. So instead we use a dynamic proxy to get
   * an implementation. If the method being called on the {@code TypeVariable} instance has the
   * same name as one of the public methods of {@link TypeVariableImpl}, the proxy calls the same
   * method on its instance of {@code TypeVariableImpl}. Otherwise it throws {@link
   * UnsupportedOperationException}; this should only apply to {@code getAnnotatedBounds()}. This
   * does mean that users on Java 8 who obtain an instance of {@code TypeVariable} from {@link
   * TypeResolver#resolveType} will not be able to call {@code getAnnotatedBounds()} on it, but
   * that should hopefully be rare.
   * <p/>
   * <p>This workaround should be removed at a distant future time when we no longer support Java
   * versions earlier than 8.
   */
  private static final class TypeVariableInvocationHandler implements InvocationHandler {
    private static final Map<String, Method> typeVariableMethods;

    static {
      Map<String, Method> builder = new LinkedHashMap<String, Method>();
      for (Method method : TypeVariableImpl.class.getMethods()) {
        if (method.getDeclaringClass().equals(TypeVariableImpl.class)) {
          try {
            method.setAccessible(true);
          } catch (AccessControlException e) {
            // OK: the method is accessible to us anyway. The setAccessible call is only for
            // unusual execution environments where that might not be true.
          }
          builder.put(method.getName(), method);
        }
      }
      typeVariableMethods = builder;
    }

    private final TypeVariableImpl<?> typeVariableImpl;

    TypeVariableInvocationHandler(TypeVariableImpl<?> typeVariableImpl) {
      this.typeVariableImpl = typeVariableImpl;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      Method typeVariableMethod = typeVariableMethods.get(methodName);
      if (typeVariableMethod == null) {
        throw new UnsupportedOperationException(methodName);
      } else {
        try {
          return typeVariableMethod.invoke(typeVariableImpl, args);
        } catch (InvocationTargetException e) {
          throw e.getCause();
        }
      }
    }
  }

  private static final class TypeVariableImpl<D extends GenericDeclaration> {

    private final D genericDeclaration;
    private final String name;
    private final List<Type> bounds;

    TypeVariableImpl(D genericDeclaration, String name, Type[] bounds) {
      this.genericDeclaration = genericDeclaration;
      this.name = name;
      this.bounds = new ArrayList<Type>(Arrays.asList(bounds));
    }

    public Type[] getBounds() {
      return toArray(bounds);
    }

    public D getGenericDeclaration() {
      return genericDeclaration;
    }

    public String getName() {
      return name;
    }

    public String getTypeName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public int hashCode() {
      return genericDeclaration.hashCode() ^ name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (NativeTypeVariableEquals.NATIVE_TYPE_VARIABLE_ONLY) {
        // equal only to our TypeVariable implementation with identical bounds
        if (obj != null
            && Proxy.isProxyClass(obj.getClass())
            && Proxy.getInvocationHandler(obj) instanceof TypeVariableInvocationHandler) {
          TypeVariableInvocationHandler typeVariableInvocationHandler =
              (TypeVariableInvocationHandler) Proxy.getInvocationHandler(obj);
          TypeVariableImpl<?> that = typeVariableInvocationHandler.typeVariableImpl;
          return name.equals(that.getName())
              && genericDeclaration.equals(that.getGenericDeclaration())
              && bounds.equals(that.bounds);
        }
        return false;
      } else {
        // equal to any TypeVariable implementation regardless of bounds
        if (obj instanceof TypeVariable) {
          TypeVariable<?> that = (TypeVariable<?>) obj;
          return name.equals(that.getName())
              && genericDeclaration.equals(that.getGenericDeclaration());
        }
        return false;
      }
    }
  }

  private static Type[] toArray(Collection<Type> types) {
    return types.toArray(new Type[types.size()]);
  }

  /**
   * Returns the {@code Class} object of arrays with {@code componentType}.
   */
  static Class<?> getArrayClass(Class<?> componentType) {
    // TODO(user): This is not the most efficient way to handle generic
    // arrays, but is there another way to extract the array class in a
    // non-hacky way (i.e. using String value class names- "[L...")?
    return Array.newInstance(componentType, 0).getClass();
  }

  // TODO(benyu): Once we are on Java 8, delete this abstraction
  enum JavaVersion {
    JAVA6 {
      @Override
      GenericArrayType newArrayType(Type componentType) {
        return new GenericArrayTypeImpl(componentType);
      }

      @Override
      Type usedInGenericType(Type type) {
        if (type instanceof Class) {
          Class<?> cls = (Class<?>) type;
          if (cls.isArray()) {
            return new GenericArrayTypeImpl(cls.getComponentType());
          }
        }
        return type;
      }
    },
    JAVA7 {
      @Override
      Type newArrayType(Type componentType) {
        if (componentType instanceof Class) {
          return getArrayClass((Class<?>) componentType);
        } else {
          return new GenericArrayTypeImpl(componentType);
        }
      }

      @Override
      Type usedInGenericType(Type type) {
        return type;
      }
    },
    JAVA8 {
      @Override
      Type newArrayType(Type componentType) {
        return JAVA7.newArrayType(componentType);
      }

      @Override
      Type usedInGenericType(Type type) {
        return JAVA7.usedInGenericType(type);
      }

      @Override
      String typeName(Type type) {
        try {
          Method getTypeName = Type.class.getMethod("getTypeName");
          return (String) getTypeName.invoke(type);
        } catch (NoSuchMethodException e) {
          throw new AssertionError("Type.getTypeName should be available in Java 8");
        } catch (InvocationTargetException e) {
          throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    };

    static final JavaVersion CURRENT;

    static {
      if (AnnotatedElement.class.isAssignableFrom(TypeVariable.class)) {
        CURRENT = JAVA8;
      } else if (new TypeCapture<int[]>() {
      }.capture() instanceof Class) {
        CURRENT = JAVA7;
      } else {
        CURRENT = JAVA6;
      }
    }

    abstract Type newArrayType(Type componentType);

    abstract Type usedInGenericType(Type type);

    String typeName(Type type) {
      return Types.toString(type);
    }

    final List<Type> usedInGenericType(Type[] types) {
      List<Type> builder = new ArrayList<Type>(types.length);
      for (Type type : types) {
        builder.add(usedInGenericType(type));
      }
      return builder;
    }
  }

  /**
   * Per https://code.google.com/p/guava-libraries/issues/detail?id=1635, In JDK 1.7.0_51-b13,
   * TypeVariableImpl.equals() is changed to no longer be equal to custom TypeVariable
   * implementations. As a result, we need to make sure our TypeVariable implementation respects
   * symmetry. Moreover, we don't want to reconstruct a native type variable <A> using our
   * implementation unless some of its bounds have changed in resolution. This avoids creating
   * unequal TypeVariable implementation unnecessarily. When the bounds do change, however, it's
   * fine for the synthetic TypeVariable to be unequal to any native TypeVariable anyway.
   */
  static final class NativeTypeVariableEquals<X> {
    static final boolean NATIVE_TYPE_VARIABLE_ONLY =
        !NativeTypeVariableEquals.class.getTypeParameters()[0]
            .equals(newArtificialTypeVariable(NativeTypeVariableEquals.class, "X"));
  }

  private static boolean equal(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }

  private static <T> T newProxy(Class<T> interfaceType, InvocationHandler handler) {
    Object object =
        Proxy.newProxyInstance(
            interfaceType.getClassLoader(), new Class<?>[]{interfaceType}, handler);
    return interfaceType.cast(object);
  }

  static abstract class TypeCapture<T> {

    /**
     * Returns the captured type.
     */
    final Type capture() {
      Type superclass = getClass().getGenericSuperclass();
      if (!(superclass instanceof ParameterizedType)) {
        throw new RuntimeException();
      }
      return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }
  }

  private Types() {
  }
}
