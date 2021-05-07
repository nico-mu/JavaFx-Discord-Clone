package de.uniks.stp.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class RouteMapTask extends DefaultTask {
    private AnnotationReaderPluginExtension extension;

    private File buildDir;
    private String projectDir;


    private String getBuildControllerDirectory() {
        return buildDir
            + extension.getBuildPath()
            + File.separator
            + convertPackageNameToDir(extension.getControllerPackage())
            + File.separator;
    }

    private ClassLoader getControllerClassLoader() throws MalformedURLException {
        URL url = new URL("file://" + buildDir.getPath() + extension.getBuildPath() + File.separator);
        return  new URLClassLoader(new URL[]{url});
    }

    private String convertClassPath(String classPath) {
        return classPath.substring(0, classPath.length() - extension.getClassExtension().length());
    }

    private String convertPackageNameToDir(String packageName) {
        return packageName.replace('.', '/');
    }

    private boolean checkControllerInterfaceImpl(Class<?> clazz) {
        Class<?>[] clazzInterfaces = clazz.getInterfaces();
        boolean inheritsControllerInterface = false;
        String controllerInterfaceName = extension.getControllerPackage() + extension.getControllerSuperObjectName();

        for(Class<?> clazzInterface: clazzInterfaces) {
            if(clazzInterface.getName().equals(controllerInterfaceName)) {
                inheritsControllerInterface = true;
                break;
            }
        }
        return inheritsControllerInterface;
    }


    @TaskAction
    public void execute() {

        final SourceSetContainer sourceSets = this.getProject().getConvention().getPlugin(JavaPluginConvention.class)
            .getSourceSets();

        final SourceSet main = sourceSets.getByName("main");

        projectDir = this.getProject().getProjectDir().getPath()
            + File.separator
            + "src"
            + File.separator
            + main.getName()
            + extension.getJavaPath();
        buildDir = this.getProject().getBuildDir();

        File controllerFolder = new File(getBuildControllerDirectory());
        List<String> fileNames = getFilesForFolder(controllerFolder);

        if(fileNames.size() == 0) {
            System.err.println("No files found in " + getBuildControllerDirectory());
            return;
        }

        try {
            HashMap<String, String> routeMap = new HashMap<>();
            ClassLoader customLoader = getControllerClassLoader();
            Class<? extends Annotation> annotationClass =
                (Class<? extends Annotation>) customLoader.loadClass(extension.getRouteAnnotationClass());


            for (String fileName : fileNames) {
                String convertedFileName = convertClassPath(fileName);
                Class<?> clazz = customLoader.loadClass(extension.getControllerPackage() + "." + convertedFileName);
                boolean inheritsControllerInterface = checkControllerInterfaceImpl(clazz);

                if (inheritsControllerInterface && clazz.isAnnotationPresent(annotationClass)) {
                    routeMap.put(parseAnnotationValue(clazz.getAnnotation(annotationClass).toString()), clazz.getName());
                }
            }

            if(routeMap.isEmpty()) {
                System.err.println("No controllers with routes found in " + getBuildControllerDirectory());
                return;
            }

            buildRouteMapFile(routeMap);

        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Error while creating route file");
            e.printStackTrace();
        }

    }

    private String getRouteFilename() {
        return projectDir
            + File.separator
            + convertPackageNameToDir(extension.getRouteFilePackage())
            + File.separator
            + extension.getRouteClassName()
            + ".java";
    }

    public String buildFileContent(HashMap<String, String> routeMap) {
        StringBuilder beginning = new StringBuilder();
        StringBuilder file = new StringBuilder();

         beginning.append("package ").append(extension.getRouteFilePackage())
             .append(";")
             .append(System.lineSeparator())
             .append(System.lineSeparator());

         beginning.append("import java.util.HashMap;")
             .append(System.lineSeparator());

         file.append("public class ").append(extension.getRouteClassName()).append(" {")
             .append(System.lineSeparator())
             .append(" ".repeat(4)).append("public HashMap<String, Class<?>> getRoutes() {")
             .append(System.lineSeparator())
             .append(" ".repeat(8)).append("HashMap<String, Class<?>> routes = new HashMap<>();")
             .append(System.lineSeparator());


        for(String route : routeMap.keySet()) {
            String classNameWithPackage = routeMap.get(route);

            beginning.append("import ").append(classNameWithPackage).append(";")
                .append(System.lineSeparator());

            file.append(" ".repeat(8))
                .append("routes.put(")
                .append("\"").append(route).append("\"")
                .append(", ")
                .append(extractClassName(classNameWithPackage))
                .append(extension.getClassExtension())
                .append(");")
                .append(System.lineSeparator());
        }

        file.append(" ".repeat(8)).append("return routes;")
            .append(System.lineSeparator())
            .append(" ".repeat(4))
            .append("}")
            .append(System.lineSeparator())
            .append("}");

        return beginning.append(System.lineSeparator()).append(file).toString();
    }

    public String extractClassName(String classNameWithPackage) {
        int index = classNameWithPackage.lastIndexOf(".");
        return classNameWithPackage.substring(index + 1);
    }

    public void buildRouteMapFile(HashMap<String, String> routeMap) throws IOException {
        FileWriter fileWriter = new FileWriter(getRouteFilename());
        fileWriter.write(buildFileContent(routeMap));
        fileWriter.close();
    }

    public String parseAnnotationValue(String annotationStringRepresentation) {
        return annotationStringRepresentation.split("\"")[1];
    }

    public List<String> getFilesForFolder(File folder) {
        List<String> fileNames = new ArrayList<>();

        if(folder.canRead() && folder.isDirectory()) {
            File[] filesEntries = folder.listFiles();

            if(Objects.nonNull(filesEntries)) {
                for (final File fileEntry : filesEntries) {
                    if (fileEntry.isDirectory()) {
                        getFilesForFolder(fileEntry);
                    } else {
                        fileNames.add(fileEntry.getName());
                    }
                }
            }
        }
        return fileNames;
    }

    public void setExtension(AnnotationReaderPluginExtension extension) {
        this.extension = extension;
    }
}
