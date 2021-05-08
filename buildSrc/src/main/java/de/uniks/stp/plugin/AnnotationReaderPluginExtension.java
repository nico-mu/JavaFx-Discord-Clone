package de.uniks.stp.plugin;

public class AnnotationReaderPluginExtension {
    //new
    private String inputPackage = "de.uniks.stp.controller";
    private String annotationClass = "de.uniks.stp.router.Route";
    private String outputClass = "de.uniks.stp.router.RouteMapping";
    private String superClass = "de.uniks.stp.controller.ControllerInterface";


    public String getInputPackage() {
        return inputPackage;
    }

    public void setInputPackage(String inputPackage) {
        this.inputPackage = inputPackage;
    }

    public String getOutputClass() {
        return outputClass;
    }

    public void setOutputClass(String outputClass) {
        this.outputClass = outputClass;
    }

    public String getAnnotationClass() {
        return annotationClass;
    }

    public void setAnnotationClass(String annotationClass) {
        this.annotationClass = annotationClass;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }
}
