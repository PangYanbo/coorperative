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
 * Shapeファイル形状との重複判定用クラス
 * @author H.Kanasugi@CSIS. UT.
 * @since 2010/10/13
 */
public class GeometryChecker {
	/* ==============================================================
	 * static methods
	 * ============================================================== */
//	/**
//	 * テスト用
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
	 * Shapeファイルに含まれるフィーチャのリスト取得
	 * @param dir Shapeファイルの含まれるフォルダパス．複数ある場合は全て読み出し
	 * @return フィーチャのリスト
	 */
	public static List<SimpleFeature> extractFeatures(File dir) throws FileNotFoundException, IOException {
		return extractFeatures(dir,"UTF-8");
	}
	
	/**
	 * Shapeファイルに含まれるフィーチャのリスト取得
	 * @param dir Shapeファイルの含まれるフォルダパス．複数ある場合は全て読み出し
	 * @param charset Shapeファイルの文字コード
	 * @return フィーチャのリスト
	 */
	public static List<SimpleFeature> extractFeatures(File dir,String charset) throws FileNotFoundException, IOException {
		// error　handle ///////////////////////////////////
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
	/** フィーチャのリスト	*/	private List<SimpleFeature> _features;
	/** ジオメトリ生成用	*/	private GeometryFactory     _fac;
	

	/* ==============================================================
	 * constructors
	 * ============================================================== */
	/**
	 * 初期化
	 * @param shp_dir シェイプファイルのあるフォルダパス
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
	 * 指定した座標が重複するかどうかの判定
	 * @param lon 経度
	 * @param lat 緯度
	 * @return 判定結果
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
	 * 指定した経緯度に重なるフィーチャの
	 * @param attrname 属性名
	 * @param lon　経度
	 * @param lat　緯度
	 * @return 対象になる属性値のリスト
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
