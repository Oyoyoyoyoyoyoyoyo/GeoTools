package org.geotools.tutorial.feature;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GeoJSON转Shp文件（WGS84）
 */
public class GeoJSON2Shp {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Map map = new HashMap();
        GeometryJSON gjson = new GeometryJSON();
        String outputPath = null;
        try {
            //读取geojson文件
            File file = JFileDataStoreChooser.showOpenFile("geojson", null);
            if (file == null) {
                return;
            }
            // 输出文件为同路径下同文件名,不同格式
            outputPath = file.getAbsolutePath().replace(".geojson", ".shp");
            //读取文件存入sb
            Reader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
            sb.toString();

            //将geojson数据转json对象
            JSONObject json = JSONObject.parseObject(sb.toString());
            JSONArray features = (JSONArray) json.get("features");
            JSONObject feature0 = JSONObject.parseObject(features.get(0).toString());
            //数据字段
            JSONObject attrs0 = (JSONObject) feature0.get("properties");
            System.out.println("attrs0 = " + attrs0);
            //geom类型
            String strType = ((JSONObject) feature0.get("geometry")).getString("type").toString();
            final SimpleFeatureType TYPE = getSimpleType(file, attrs0, strType);

            //    创建shape文件对象
            File outputFile = new File(outputPath);
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", outputFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            ShapefileDataStore ds = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

            ds.createSchema(TYPE);
            //    设置编码
            Charset charset = Charset.forName("GBK");
            ds.setCharset(charset);
            setFeature(ds, gjson, features, strType);
            ds.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("当前程序耗时：" + (endTime - startTime) + "ms");
    }


    /**
     * 获取SimpleFeatureType
     * @param file
     * @param attrs0
     * @param strType geom数据类型
     * @return
     * @throws Exception
     */
    public static SimpleFeatureType getSimpleType(File file, JSONObject attrs0, String strType) throws Exception {

        String outPutFileName = file.getName();
        //数据集合类型
        String geomType = "the_geom:" + strType + ":srid=4326,";
        //属性字段
        String geomAttrs = "";
        for (String attr : attrs0.keySet()) {
            geomAttrs = attr + ":String," + geomAttrs;
        }
        geomAttrs = geomAttrs.substring(0, geomAttrs.length() - 1);
        SimpleFeatureType TYPE =
                DataUtilities.createType(
                        outPutFileName,
                        //输出文件名称
                        geomType + geomAttrs
                );
        return TYPE;
    }

    /**
     * 读取geojson数据转换成feature
     *
     * @param ds
     * @param gjson
     * @param features
     * @throws IOException
     */
    public static void setFeature(ShapefileDataStore ds, GeometryJSON gjson, JSONArray features, String geomType) throws IOException {

        //    设置writer
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
        for (int i = 0, len = features.size(); i < len; i++) {
            JSONObject featureJson = JSONObject.parseObject(features.get(i).toString());
            Reader reader1 = new StringReader(featureJson.toString());
            SimpleFeature feature = writer.next();
            //根据不同geom类型读取其geom属性
            switch (geomType) {
                case "Point":
                    feature.setAttribute("the_geom", gjson.readPoint(reader1));
                    break;
                case "MultiPoint":
                    feature.setAttribute("the_geom", gjson.readMultiPoint(reader1));
                    break;
                case "LineString":
                    feature.setAttribute("the_geom", gjson.readLine(reader1));
                    break;
                case "MultiLineString":
                    feature.setAttribute("the_geom", gjson.readMultiLine(reader1));
                    break;
                case "Polygon":
                    feature.setAttribute("the_geom", gjson.readPolygon(reader1));
                    break;
                case "MultiPolygon":
                    feature.setAttribute("the_geom", gjson.readMultiPolygon(reader1));
                    break;
                default:
                    return;
            }

            //数据字段
            JSONObject attrs = (JSONObject) featureJson.get("properties");
            for (String attr : attrs.keySet()) {
                System.out.println("attrs.get(i)" + attrs.get(attr));
                //每个字段添加值
                feature.setAttribute(attr, attrs.get(attr));
            }
            writer.write();
        }
        writer.close();
    }

}
