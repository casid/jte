package gg.jte.internal;

final class ClassInfo {
    final String name;
    final String className;
    final String packageName;
    final String fullName;
    int[] lineInfo;

    ClassInfo(String name, String parentPackage) {
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

        className = Constants.CLASS_PREFIX + name.substring(startIndex, endIndex).replace("-", "") + Constants.CLASS_SUFFIX;
        if (startIndex == 0) {
            packageName = parentPackage;
        } else {
            packageName = parentPackage + "." + name.substring(0, startIndex - 1).replace('/', '.');
        }
        fullName = packageName + "." + className;
    }
}
