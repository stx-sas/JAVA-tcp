package stx.mc;

public class Position{
    public Position(Vector3d xyz, Vector3d ypr) {
        this.xyz = xyz;
        this.ypr = ypr;
    }
    public Vector3d xyz;
    public Vector3d ypr;

    @Override
    public String toString() {
        return "Position{" +
                "xyz=" + xyz +
                ", ypr=" + ypr +
                '}';
    }
}
