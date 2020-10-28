package org.geotools.tutorial.feature;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * GeoJSON转Shp文件（当前支持WGS84）
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
            //将geojson数据转json对象
            JSONObject json = JSONObject.parseObject(sb.toString());
            JSONArray features = (JSONArray) json.get("features");
            JSONObject feature0 = JSONObject.parseObject(features.get(0).toString());
            //数据字段
            JSONObject attrs = (JSONObject) feature0.get("properties");
            //数据矢量类型
            String strType = ((JSONObject) feature0.get("geometry")).getString("type").toString();

            //    创建shape文件对象
            File outputFile = new File(outputPath);
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", outputFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            ShapefileDataStore ds = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            String outPutFileName = file.getName();
            //数据集合类型
            String geomType = "the_geom:" + strType + ":srid=4326,";
            //属性字段
            String geomAttrs = "";
            for (String attr : attrs.keySet()) {
                geomAttrs = attr + ":String," + geomAttrs;
            }
            geomAttrs = geomAttrs.substring(0, geomAttrs.length() - 1);
            final SimpleFeatureType TYPE =
                    DataUtilities.createType(
                            outPutFileName,
                            //输出文件名称
                            geomType + geomAttrs
                    );
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
                writer.write();
            }
            writer.close();
            ds.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
