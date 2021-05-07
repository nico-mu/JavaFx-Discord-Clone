package de.uniks.stp.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class RouteMapTask extends DefaultTask {

    private final String CLASS_EXTENSION = ".class";
    private final String JAVA_EXTENSION = ".java";
    private String inputPackage;
    private String annotationClass;
    private String outputClass;
    private String superClass;
    private String outputFilePath;

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }


    public void setInputPackage(String inputPackage) {
        this.inputPackage = inputPackage;
    }


    public void setOutputClass(String outputClass) {
        this.outputClass = outputClass;
    }

    public void setAnnotationClass(String annotationClass) {
        this.annotationClass = annotationClass;
    }

    private ClassLoader getClassLoader(String buildDir) throws MalformedURLException {
        URL url = new URL("file://" + buildDir + File.separator);
        return  new URLClassLoader(new URL[]{url});
    }

    private String getClassNameFromFile(String fileName) {
        return fileName.substring(0, fileName.length() - CLASS_EXTENSION.length());
    }

    private String convertPackageNameToDir(String packageName) {
        return packageName.replace('.', '/');
    }

    private boolean checkControllerInterfaceImpl(Class<?> clazz) {
        Class<?>[] clazzInterfaces = clazz.getInterfaces();
        boolean inheritsControllerInterface = false;
        String controllerInterfaceName = superClass;

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
        String buildDir = main.getOutput().getClassesDirs().getAsPath();
        outputFilePath = this.getProject().getProjectDir() + "/src/" + main.getName() + "/java/" + convertPackageNameToDir(outputClass) + JAVA_EXTENSION;

        String inputDirectory = buildDir + File.separator + convertPackageNameToDir(inputPackage) + File.separator;
        List<String> fileNames = getFilesForFolder(new File(inputDirectory));

        if(fileNames.size() == 0) {
            System.err.println("No files found in " + inputDirectory);
            return;
        }

        try {
            HashMap<String, String> routeMap = new HashMap<>();
            ClassLoader customLoader = getClassLoader(buildDir);
            Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) customLoader.loadClass(this.annotationClass);


            for (String fileName : fileNames) {
                String convertedFileName = getClassNameFromFile(fileName);
                Class<?> clazz = customLoader.loadClass(inputPackage + "." + convertedFileName);
                boolean inheritsControllerInterface = checkControllerInterfaceImpl(clazz);

                if (inheritsControllerInterface && clazz.isAnnotationPresent(annotationClass)) {
                    routeMap.put(parseAnnotationValue(clazz.getAnnotation(annotationClass).toString()), clazz.getName());
                }
            }

            if(routeMap.isEmpty()) {
                System.err.println("No controllers with routes found in " + inputDirectory);
                return;
            }

            buildRouteMapFile(routeMap);

        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Error while creating route file");
            e.printStackTrace();
        }

    }

    public String buildFileContent(HashMap<String, String> routeMap) {
        StringBuilder beginning = new StringBuilder();
        StringBuilder file = new StringBuilder();


         beginning.append("package ").append(extractPackageName(outputClass))
             .append(";")
             .append(System.lineSeparator())
             .append(System.lineSeparator());

         beginning.append("import java.util.HashMap;")
             .append(System.lineSeparator());

         file.append("public class ").append(extractClassName(outputClass)).append(" {")
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
                .append(CLASS_EXTENSION)
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

    public String extractPackageName(String classNameWithPackage) {
        int index = classNameWithPackage.lastIndexOf(".");
        return classNameWithPackage.substring(0, index);
    }

    public String extractClassName(String classNameWithPackage) {
        int index = classNameWithPackage.lastIndexOf(".");
        return classNameWithPackage.substring(index + 1);
    }

    public void buildRouteMapFile(HashMap<String, String> routeMap) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFilePath);
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
}
