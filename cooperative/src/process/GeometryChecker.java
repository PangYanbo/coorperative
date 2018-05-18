package process;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Shape�t�@�C���`��Ƃ̏d������p�N���X
 * @author H.Kanasugi@CSIS. UT.
 * @since 2010/10/13
 */
public class GeometryChecker {
	/* ==============================================================
	 * static methods
	 * ============================================================== */
//	/**
//	 * �e�X�g�p
//	 * @param args
//	 */
//	public static void main(String[] args) throws Exception {
//		File   shpdir  = new File(args[0]);
//		double lon     = 90.398600;
//		double lat     = 23.813900;
//		
//		GeometryChecker inst = new GeometryChecker(shpdir);
////		System.out.println( inst.checkOverlap(lon, lat) );
//		System.out.println( inst.listOverlaps("AID",lon, lat) );
//	}
	
	/**
	 * Shape�t�@�C���Ɋ܂܂��t�B�[�`���̃��X�g�擾
	 * @param dir Shape�t�@�C���̊܂܂��t�H���_�p�X�D��������ꍇ�͑S�ēǂݏo��
	 * @return �t�B�[�`���̃��X�g
	 */
	public static List<SimpleFeature> extractFeatures(File dir) throws FileNotFoundException, IOException {
		return extractFeatures(dir,"UTF-8");
	}
	
	/**
	 * Shape�t�@�C���Ɋ܂܂��t�B�[�`���̃��X�g�擾
	 * @param dir Shape�t�@�C���̊܂܂��t�H���_�p�X�D��������ꍇ�͑S�ēǂݏo��
	 * @param charset Shape�t�@�C���̕����R�[�h
	 * @return �t�B�[�`���̃��X�g
	 */
	public static List<SimpleFeature> extractFeatures(File dir,String charset) throws FileNotFoundException, IOException {
		// error�@handle ///////////////////////////////////
		if( dir == null || !dir.exists() ) { 
			throw new FileNotFoundException(dir.getAbsolutePath()); 
		}
		
		// extract shape file from directory //////////////
		File shpfiles[] = dir.listFiles(new FileFilter(){
			public boolean accept(File f) { return StringUtils.endsWithIgnoreCase(f.getName(), ".shp"); }
		});
		
		// load shape files ///////////////////////////////
		List<SimpleFeature> list = new ArrayList<SimpleFeature>();
		
		for(File f : shpfiles) {
			// loading shape file =====================
			ShapefileDataStore shapefile = new ShapefileDataStore(f.toURI().toURL());
			shapefile.setStringCharset(Charset.forName(charset));
			FeatureSource<SimpleFeatureType, SimpleFeature>     feature = shapefile.getFeatureSource();
			FeatureCollection<SimpleFeatureType, SimpleFeature> col     = feature.getFeatures();
			for(FeatureIterator<SimpleFeature> itr = col.features();itr.hasNext();) {
				//System.out.println(itr.next());
				list.add(itr.next());
			
			}
		}
		
		return list;
	}
	

	/* ==============================================================
	 * instance fields
	 * ============================================================== */
	/** �t�B�[�`���̃��X�g	*/	private List<SimpleFeature> _features;
	/** �W�I���g�������p	*/	private GeometryFactory     _fac;
	

	/* ==============================================================
	 * constructors
	 * ============================================================== */
	/**
	 * ������
	 * @param shp_dir �V�F�C�v�t�@�C���̂���t�H���_�p�X
	 */
	public GeometryChecker(File shp_dir) {
		try {
			_features = extractFeatures(shp_dir);
			_fac      = new GeometryFactory();
		}
		catch(FileNotFoundException exp) { exp.printStackTrace(); _features = Arrays.asList(); }
		catch(IOException exp) {           exp.printStackTrace(); _features = Arrays.asList(); }
	}


	/* ==============================================================
	 * instance methods
	 * ============================================================== */
	/**
	 * �w�肵�����W���d�����邩�ǂ����̔���
	 * @param lon �o�x
	 * @param lat �ܓx
	 * @return ���茋��
	 */
	public boolean checkOverlap(double lon, double lat) {
		Point point = _fac.createPoint(new Coordinate(lon,lat));
		for(SimpleFeature sf:_features) {
			if( Geometry.class.cast(sf.getDefaultGeometry()).contains(point) ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * �w�肵���o�ܓx�ɏd�Ȃ�t�B�[�`����
	 * @param attrname ������
	 * @param lon�@�o�x
	 * @param lat�@�ܓx
	 * @return �ΏۂɂȂ鑮���l�̃��X�g
	 */
	public List<String> listOverlaps(String attrname,double lon,double lat) {
		List<String> list = new ArrayList<String>();
		Point point = _fac.createPoint(new Coordinate(lon,lat));
		for(SimpleFeature sf:_features) {
			if( Geometry.class.cast(sf.getDefaultGeometry()).contains(point) ) {
				list.add( String.valueOf(sf.getAttribute(attrname)) );
			}
		}
		return list;
	}
}
