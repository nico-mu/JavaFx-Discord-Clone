package de.uniks.stp.plugin;

public class AnnotationReaderPluginExtension {

    private String controllerPackage = "de.uniks.stp.controller";
    private String routeAnnotationClass = "de.uniks.stp.annotation.Route";
    private String buildPath = "/classes/java/main";
    private String classExtension = ".class";
    private String ControllerSuperObjectName = ".ControllerInterface";
    private String javaPath = "/java";
    private String routeClassName = "RouteMapping";
    private String routeFilePackage = "de.uniks.stp.controller";

    public String getControllerPackage() {
        return controllerPackage;
    }

    public void setControllerPackage(String controllerPackage) {
        this.controllerPackage = controllerPackage;
    }

    public String getRouteAnnotationClass() {
        return routeAnnotationClass;
    }

    public void setRouteAnnotationClass(String routeAnnotationClass) {
        this.routeAnnotationClass = routeAnnotationClass;
    }

    public String getBuildPath() {
        return buildPath;
    }

    public void setBuildPath(String buildPath) {
        this.buildPath = buildPath;
    }

    public String getClassExtension() {
        return classExtension;
    }

    public void setClassExtension(String classExtension) {
        this.classExtension = classExtension;
    }

    public String getControllerSuperObjectName() {
        return ControllerSuperObjectName;
    }

    public void setControllerSuperObjectName(String controllerSuperObjectName) {
        ControllerSuperObjectName = controllerSuperObjectName;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public String getRouteClassName() {
        return routeClassName;
    }

    public void setRouteClassName(String routeClassName) {
        this.routeClassName = routeClassName;
    }

    public String getRouteFilePackage() {
        return routeFilePackage;
    }

    public void setRouteFilePackage(String routeFilePackage) {
        this.routeFilePackage = routeFilePackage;
    }
}
