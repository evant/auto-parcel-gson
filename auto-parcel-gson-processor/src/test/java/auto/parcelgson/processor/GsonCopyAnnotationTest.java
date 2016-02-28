package auto.parcelgson.processor;

import com.google.common.collect.ImmutableList;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.testing.compile.JavaFileObjects;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Tests to ensure gson annotations are copied to their equivalent field annotations on the
 * generated classes
 *
 * @author evan
 */
public class GsonCopyAnnotationTest extends TestCase {
  private static final String GSON_COPY_ANNOTATION_TEST = "auto.parcelgson.processor.GsonCopyAnnotationTest";
  private static final String JSON_ADAPTER = GSON_COPY_ANNOTATION_TEST + ".TestJsonAdapter";

  private JavaFileObject sourceCodeForField(List<String> imports, List<String> annotations) {
    ImmutableList<String> list = ImmutableList.<String>builder()
        .add(
            "package foo.bar;",
            "",
            "import auto.parcelgson.AutoParcelGson;"
        )
        .addAll(imports)
        .add(
            "",
            "@AutoParcelGson",
            "public abstract class Baz {"
        )
        .addAll(annotations)
        .add(
            "  public abstract int buh();",
            "",
            "  public static Baz create(int buh) {",
            "    return new AutoParcelGson_Baz(buh);",
            "  }",
            "}"
        )
        .build();

    String[] lines = list.toArray(new String[list.size()]);
    return JavaFileObjects.forSourceLines("foo.bar.Baz", lines);
  }

  private JavaFileObject sourceCodeForClass(List<String> imports, List<String> annotations) {
    ImmutableList<String> list = ImmutableList.<String>builder()
        .add(
            "package foo.bar;",
            "",
            "import auto.parcelgson.AutoParcelGson;"
        )
        .addAll(imports)
        .add("")
        .addAll(annotations)
        .add(
            "@AutoParcelGson",
            "public abstract class Baz {"
        )
        .add(
            "  public abstract int buh();",
            "",
            "  public static Baz create(int buh) {",
            "    return new AutoParcelGson_Baz(buh);",
            "  }",
            "}"
        )
        .build();

    String[] lines = list.toArray(new String[list.size()]);
    return JavaFileObjects.forSourceLines("foo.bar.Baz", lines);
  }

  private JavaFileObject expectedCodeForField(List<String> annotations) {
    ImmutableList<String> list = ImmutableList.<String>builder()
        .add(
            "package foo.bar;",
            "",
            "final class AutoParcelGson_Baz extends Baz {"
        )
        .addAll(annotations)
        .add(
            "  private final int buh;",
            "",
            "  AutoParcelGson_Baz(int buh) {",
            "    this.buh = buh;",
            "  }",
            ""
        )
        .add(
            "  @Override public int buh() {",
            "    return buh;",
            "  }",
            "",
            "  @Override public String toString() {",
            "    return \"Baz{\"",
            "        + \"buh=\" + buh",
            "        + \"}\";",
            "  }",
            "",
            "  @Override public boolean equals(Object o) {",
            "    if (o == this) {",
            "      return true;",
            "    }",
            "    if (o instanceof Baz) {",
            "      Baz that = (Baz) o;",
            "      return (this.buh == that.buh());",
            "    }",
            "    return false;",
            "  }",
            "",
            "  @Override public int hashCode() {",
            "    int h = 1;",
            "    h *= 1000003;",
            "    h ^= this.buh;",
            "    return h;",
            "  }",
            "}"
        )
        .build();

    String[] lines = list.toArray(new String[list.size()]);
    return JavaFileObjects.forSourceLines("foo.bar.AutoParcelGson_Baz", lines);
  }

  private JavaFileObject expectedCodeForClass(List<String> annotations) {
    ImmutableList<String> list = ImmutableList.<String>builder()
        .add(
            "package foo.bar;",
            "")
        .addAll(annotations)
        .add(
            "final class AutoParcelGson_Baz extends Baz {",
            "  private final int buh;",
            "",
            "  AutoParcelGson_Baz(int buh) {",
            "    this.buh = buh;",
            "  }",
            ""
        )
        .add(
            "  @Override public int buh() {",
            "    return buh;",
            "  }",
            "",
            "  @Override public String toString() {",
            "    return \"Baz{\"",
            "        + \"buh=\" + buh",
            "        + \"}\";",
            "  }",
            "",
            "  @Override public boolean equals(Object o) {",
            "    if (o == this) {",
            "      return true;",
            "    }",
            "    if (o instanceof Baz) {",
            "      Baz that = (Baz) o;",
            "      return (this.buh == that.buh());",
            "    }",
            "    return false;",
            "  }",
            "",
            "  @Override public int hashCode() {",
            "    int h = 1;",
            "    h *= 1000003;",
            "    h ^= this.buh;",
            "    return h;",
            "  }",
            "}"
        )
        .build();

    String[] lines = list.toArray(new String[list.size()]);
    return JavaFileObjects.forSourceLines("foo.bar.AutoParcelGson_Baz", lines);
  }

  public static class TestJsonAdapter extends TypeAdapter {
    @Override
    public void write(JsonWriter out, Object value) throws IOException {

    }

    @Override
    public Object read(JsonReader in) throws IOException {
      return null;
    }
  }

  private void assertGeneratedMatchesForField(
      List<String> imports,
      List<String> annotations,
      List<String> expectedAnnotations) {

    JavaFileObject javaFileObject = sourceCodeForField(imports, annotations);
    JavaFileObject expectedOutput = expectedCodeForField(expectedAnnotations);

    assert_().about(javaSource())
        .that(javaFileObject)
        .processedWith(new AutoParcelProcessor())
        .compilesWithoutError()
        .and().generatesSources(expectedOutput);
  }

  private void assertGeneratedMatchesForClass(
      List<String> imports,
      List<String> annotations,
      List<String> expectedAnnotations) {

    JavaFileObject javaFileObject = sourceCodeForClass(imports, annotations);
    JavaFileObject expectedOutput = expectedCodeForClass(expectedAnnotations);

    assert_().about(javaSource())
        .that(javaFileObject)
        .processedWith(new AutoParcelProcessor())
        .compilesWithoutError()
        .and().generatesSources(expectedOutput);
  }

  public void testExposeAnnotation() {
    assertGeneratedMatchesForField(
        ImmutableList.of("import auto.parcelgson.gson.annotations.Expose;"),
        ImmutableList.of("@Expose"),
        ImmutableList.of("@com.google.gson.annotations.Expose"));
  }

  public void testSerializedNameAnnotation() {
    assertGeneratedMatchesForField(
        ImmutableList.of("import auto.parcelgson.gson.annotations.SerializedName;"),
        ImmutableList.of("@SerializedName(\"foo\")"),
        ImmutableList.of("@com.google.gson.annotations.SerializedName(value=\"foo\")"));
  }

  public void testGsonSerializeNameAnnotation() {
    assertGeneratedMatchesForField(
        ImmutableList.of("import com.google.gson.annotations.SerializedName;"),
        ImmutableList.of("@SerializedName(\"foo\")"),
        ImmutableList.of("@com.google.gson.annotations.SerializedName(value=\"foo\")"));
  }

  public void testJsonAdapterAnnotation() {
    assertGeneratedMatchesForField(
        ImmutableList.of("import auto.parcelgson.gson.annotations.JsonAdapter;"),
        ImmutableList.of("@JsonAdapter(" + JSON_ADAPTER + ".class)"),
        ImmutableList.of("@com.google.gson.annotations.JsonAdapter(value = " + JSON_ADAPTER + ".class)"));
  }

  public void testSinceAnnotation() {
    assertGeneratedMatchesForField(
        ImmutableList.of("import auto.parcelgson.gson.annotations.Since;"),
        ImmutableList.of("@Since(1)"),
        ImmutableList.of("@com.google.gson.annotations.Since(value = 1.0)"));
  }

  public void testUntilAnnotation() {
    assertGeneratedMatchesForField(
        ImmutableList.of("import auto.parcelgson.gson.annotations.Until;"),
        ImmutableList.of("@Until(1)"),
        ImmutableList.of("@com.google.gson.annotations.Until(value = 1.0)"));
  }

  public void testJsonAdapterClassAnnotation() {
    assertGeneratedMatchesForClass(
        ImmutableList.of("import auto.parcelgson.gson.annotations.JsonAdapter;"),
        ImmutableList.of("@JsonAdapter(" + JSON_ADAPTER + ".class)"),
        ImmutableList.of("@com.google.gson.annotations.JsonAdapter(value = " + JSON_ADAPTER + ".class)"));
  }

  public void testOriginalJsonAdapterClassAnnotation() {
    assertGeneratedMatchesForClass(
        ImmutableList.of("import com.google.gson.annotations.JsonAdapter;"),
        ImmutableList.of("@JsonAdapter(" + JSON_ADAPTER + ".class)"),
        ImmutableList.of("@com.google.gson.annotations.JsonAdapter(value = " + JSON_ADAPTER + ".class)"));
  }

  public void testSinceClassAnnotation() {
    assertGeneratedMatchesForClass(
        ImmutableList.of("import auto.parcelgson.gson.annotations.Since;"),
        ImmutableList.of("@Since(1)"),
        ImmutableList.of("@com.google.gson.annotations.Since(value = 1.0)"));
  }

  public void testOriginalSinceClassAnnotation() {
    assertGeneratedMatchesForClass(
        ImmutableList.of("import com.google.gson.annotations.Since;"),
        ImmutableList.of("@Since(1)"),
        ImmutableList.of("@com.google.gson.annotations.Since(value = 1.0)"));
  }

  public void testUntilClassAnnotation() {
    assertGeneratedMatchesForClass(
        ImmutableList.of("import auto.parcelgson.gson.annotations.Until;"),
        ImmutableList.of("@Until(1)"),
        ImmutableList.of("@com.google.gson.annotations.Until(value = 1.0)"));
  }

  public void testOriginalUntilClassAnnotation() {
    assertGeneratedMatchesForClass(
        ImmutableList.of("import com.google.gson.annotations.Until;", 
           ""),
        ImmutableList.of("@Until(1)"),
        ImmutableList.of("@com.google.gson.annotations.Until(value = 1.0)"));
  }
}
