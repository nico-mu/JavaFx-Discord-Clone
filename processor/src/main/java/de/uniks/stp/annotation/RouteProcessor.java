package de.uniks.stp.annotation;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedAnnotationTypes(RouteProcessor.ANNOTATION_ROUTE)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class RouteProcessor extends AbstractProcessor {

    static final String ANNOTATION_ROUTE = "de.uniks.stp.annotation.Route";
    private static final String PACKAGE_NAME = "de.uniks.stp.router";
    private static final String CLASS_NAME = "RouteMap";
    private static final String CLASS_EXTENSION = ".class";

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment env) {
        try {
            final JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(PACKAGE_NAME + "." + CLASS_NAME);

            final Messager messager = processingEnv.getMessager();

            final Writer writer = builderFile.openWriter();

            writer.append("package ").append(PACKAGE_NAME).append(";")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("import java.util.HashMap;")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

            final StringBuilder javaClass = new StringBuilder()
                .append(System.lineSeparator())
                .append("class ").append(CLASS_NAME).append(" {")
                .append(System.lineSeparator())
                .append(" ".repeat(4)).append("HashMap<String, Class<?>> getRoutes() {")
                .append(System.lineSeparator())
                .append(" ".repeat(8)).append("final HashMap<String, Class<?>> routes = new HashMap<>();")
                .append(System.lineSeparator());


            for (final TypeElement annotation : annotations) {
                for (final Element elem : env.getElementsAnnotatedWith(annotation)) {
                    writer.append("import ").append(elem.toString()).append(";")
                        .append(System.lineSeparator());

                    messager.printMessage(NOTE, elem.getSimpleName());
                    final List<? extends AnnotationMirror> annotationMirrors = elem.getAnnotationMirrors();
                    for (final AnnotationMirror annotationMirror : annotationMirrors) {

                        // Get the ExecutableElement:AnnotationValue pairs from the annotation element
                        final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues
                            = annotationMirror.getElementValues();
                        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                            : elementValues.entrySet()) {
                            final String key = entry.getKey().getSimpleName().toString();
                            final Object value = entry.getValue().getValue();
                            if (key.equals("value")) {
                                messager.printMessage(NOTE, value.toString());
                                javaClass.append(" ".repeat(8))
                                    .append("routes.put(")
                                    .append("\"").append(value).append("\"")
                                    .append(", ")
                                    .append(elem.getSimpleName())
                                    .append(CLASS_EXTENSION)
                                    .append(");")
                                    .append(System.lineSeparator());
                            }
                        }
                    }
                }
            }
            writer.append(javaClass.toString());
            writer.append(" ".repeat(8)).append("return routes;")
                .append(System.lineSeparator())
                .append(" ".repeat(4))
                .append("}")
                .append(System.lineSeparator())
                .append("}");

            writer.close();
            messager.printMessage(NOTE, "Route annotation processing done.");
        } catch (Exception ignored) {
        }
        return true;
    }
}
