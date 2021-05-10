package de.uniks.stp.annotation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedAnnotationTypes({
    AnnotationReader.ANNOTATION_ROUTE
})
public class AnnotationReader extends AbstractProcessor {

    static final String ANNOTATION_ROUTE = "de.uniks.stp.annotation.Route";

    private Messager messager;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        try {
            //JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("de.uniks.stp.controller.RouteMap");
            //Writer writer = builderFile.openWriter();
            messager = processingEnv.getMessager();

            for (TypeElement annotation : annotations) {
                for (Element elem : env.getElementsAnnotatedWith(annotation)) {
                    PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(elem);

                    messager.printMessage(NOTE, elem.getSimpleName());
                    List<? extends AnnotationMirror> annotationMirrors = elem.getAnnotationMirrors();
                    for (AnnotationMirror annotationMirror : annotationMirrors) {

                        // Get the ExecutableElement:AnnotationValue pairs from the annotation element
                        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues
                            = annotationMirror.getElementValues();
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                            : elementValues.entrySet()) {
                            String key = entry.getKey().getSimpleName().toString();
                            Object value = entry.getValue().getValue();
                            if(key.equals("value")) {
                                messager.printMessage(NOTE, value.toString());
                                //writer.append(value.toString());
                            }
                        }
                    }
                }
            }
            //writer.close();
        }
        catch (Exception e) {
            messager.printMessage(ERROR, "InjectionProcessor encountered an error: '" + e.getMessage() + "'.");
        }
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private String getTypePathOfAnnotation(AnnotationMirror mirror) {
        return mirror.getAnnotationType().toString();
    }
}
