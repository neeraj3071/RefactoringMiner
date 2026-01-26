package TestFeatures;

public class Constructors {
    private int _id;
    private String _name;
    private boolean _isActive;
    public Constructors() {
        _id = 0;
        _name = "Default";
        _isActive = false;
    }
    public Constructors(int id) {
        _id = id;
        _name = "Default";
        _isActive = false;
    }
    public Constructors(int id, String name) {
        _id = id;
        _name = name;
        _isActive = false;
    }
    public Constructors(int id, String name, boolean isActive) {
        _id = id;
        _name = name;
        _isActive = isActive;
    }
    public Constructors(String name) {
        _name = name;
    }
    public Constructors CreateNamed(String name) {
        return new Constructors(name);
    }
}
