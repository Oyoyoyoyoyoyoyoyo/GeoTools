package org.geotools.tutorial.feature;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * GeoJSON转Shp文件
 */
public class GeoJSON2Shp {
    public static void main(String[] args) {
        Map map = new HashMap();
        GeometryJSON gjson = new GeometryJSON();
        String outputPath = null;
        try {
            //读取geojson文件
            File file = JFileDataStoreChooser.showOpenFile("geojson", null);
            if (file == null) {
                return;
            }
            // 输出文件为同路径下同文件名
            outputPath = file.getAbsolutePath().replace(".geojson", ".shp");
            FileReader fileReader = new FileReader(file);
            Reader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            sb.toString();
            System.out.println(sb.toString());
            //将geojson数据转json对象
            JSONObject json = JSONObject.parseObject(sb.toString());
            JSONArray features = (JSONArray) json.get("features");
            JSONObject feature0 = JSONObject.parseObject(features.get(0).toString());
            System.out.println("feature0.toString()--:" + feature0.toString());
            System.out.println("dataType-----------:" + feature0.get("properties").getClass().toString());
            JSONObject attrs = (JSONObject) feature0.get("properties");

            String strType = ((JSONObject) feature0.get("geometry")).getString("type").toString();
            Class<?> geoType = null;

            switch (strType) {
                case "Point":
                    geoType = Point.class;
                    break;
                case "MultiPoint":
                    geoType = MultiPoint.class;
                    break;
                case "LineString":
                    geoType = LineString.class;
                    break;
                case "MultiLineString":
                    geoType = MultiLineString.class;
                    break;
                case "Polygon":
                    geoType = Polygon.class;
                    break;
                case "MultiPolygon":
                    geoType = MultiPolygon.class;
                    break;
                default:
                    break;
            }

            //    创建shape文件对象
            File outputFile = new File(outputPath);
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", outputFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            ShapefileDataStore ds = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

            final SimpleFeatureType TYPE =
                    DataUtilities.createType(
                            "Location",
                            "the_geom:MultiPolygon:srid=4326,"
                                    + // 属性字段
                                    "code:String,"
                                    + // 属性字段
                                    "name:String"
                    );
            System.out.println("TYPE:" + TYPE);
            ds.createSchema(TYPE);
            //    设置编码
            Charset charset = Charset.forName("GBK");
            ds.setCharset(charset);
            //    设置writer
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
            for (int i = 0, len = features.size(); i < len; i++) {
                String strFeature = features.get(i).toString();
                Reader reader1 = new StringReader(strFeature);
                SimpleFeature feature = writer.next();
                feature.setAttribute("the_geom", gjson.readMultiPolygon(reader1));
                //feature.setAttribute("POIID", i);
                writer.write();
            }
            writer.close();
            ds.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
