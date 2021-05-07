package de.uniks.stp.plugin;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public class AnnotationReaderPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        String taskName = "buildRouteMap";
        AnnotationReaderPluginExtension extension = project.getExtensions()
            .create("annotationReader", AnnotationReaderPluginExtension.class);

        final SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class)
            .getSourceSets();

        final SourceSet main = sourceSets.getByName("main");
        final SourceSet test = sourceSets.getByName("test");

        project.getTasks().register(taskName, RouteMapTask.class, (task) -> {
            task.mustRunAfter(main.getCompileJavaTaskName());
            task.dependsOn(main.getCompileJavaTaskName());
            task.setAnnotationClass(extension.getAnnotationClass());
            task.setInputPackage(extension.getInputPackage());
            task.setOutputClass(extension.getOutputClass());
            task.setSuperClass(extension.getSuperClass());
        });
    }
}
