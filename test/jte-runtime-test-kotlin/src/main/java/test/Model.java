package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class Model {
    public String hello;
    public int x;
    public int[] array;
    public ModelType type;

    // For null iteration tests
    public boolean[] booleanArray;
    public byte[] byteArray;
    public short[] shortArray;
    public int[] intArray;
    public long[] longArray;
    public float[] floatArray;
    public double[] doubleArray;
    public List<String> list;
    public ArrayList<String> arrayList;
    public Set<String> set;
    public Collection<String> collection;
    public Iterable<String> iterable;

    public String getAnotherWorld() {
        return "Another World";
    }

    public void setX(int amount) {
        x = amount;
    }

    public boolean isCaseA() {
        return true;
    }

    public boolean isCaseB() {
        return false;
    }

    public String getThatThrows() {
        throw new NullPointerException("Oops");
    }
}
