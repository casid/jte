package gg.jte.runtime;

public final class ClassInfo {
    public final String name;
    public final String className;
    public final String packageName;
    public final String fullName;
    public int[] lineInfo;

    public ClassInfo(String name, String parentPackage) {
        this.name = name;

        int endIndex = name.lastIndexOf('.');
        if (endIndex == -1) {
            endIndex = name.length();
        }

        int startIndex = name.lastIndexOf('/');
        if (startIndex == -1) {
            startIndex = 0;
        } else {
            startIndex += 1;
        }

        className = Constants.CLASS_PREFIX + name.substring(startIndex, endIndex).replace("-", "").replace(".", "") + Constants.CLASS_SUFFIX;
        if (startIndex == 0) {
            packageName = parentPackage;
        } else {
            packageName = parentPackage + "." + name.substring(0, startIndex - 1).replace('/', '.');
        }
        fullName = packageName + "." + className;
    }
}
