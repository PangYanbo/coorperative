package SigSpatial;

import jp.ac.ut.csis.pflow.geom.Mesh;

public class Feature2D implements Feature {
//    private double x;
//    private double y;

	private String mesh;
	
    public Feature2D(String mesh) {
        this.mesh = mesh;
    }
    
    public double groundDist(Feature f) {
        Feature2D f2d = (Feature2D)f;
        Mesh m1 = new Mesh(mesh);
        Mesh m2 = new Mesh(f2d.mesh);
//        double deltaX = x - f2d.x;
//        double deltaY = y - f2d.y;
        //return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        return m1.getCenter().distance(m2.getCenter());
    }
}
