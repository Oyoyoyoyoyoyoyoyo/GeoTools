package org.geotools.tutorial.feature;

import com.alibaba.fastjson.JSONArray;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Shp文件转换成GeoJSON
 */
public class Shp2GeoJSON {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        FeatureJSON fjson = new FeatureJSON();
        String shpPath = "F:\\temp\\locations.shp";
        String outputPath = null;
        try {
            //读取shp文件
            File file = JFileDataStoreChooser.showOpenFile("shp", null);
            if (file == null) {
                return;
            }
            // 输出文件为同路径下同文件名
            outputPath = file.getAbsolutePath().replace(".shp", ".geojson");
            ShapefileDataStore shpDataStore = (ShapefileDataStore) FileDataStoreFinder.getDataStore(file);
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource();
            //设置geojson文件
            StringBuffer sb = new StringBuffer();
            sb.append("{\"type\": \"FeatureCollection\",\"features\": ");
            //    设置编码
            Charset charset = Charset.forName("GBK");
            shpDataStore.setCharset(charset);
            //获取shp文件feature
            SimpleFeatureCollection simpleFeatureCollection = featureSource.getFeatures();
            SimpleFeatureIterator features = simpleFeatureCollection.features();
            JSONArray array = new JSONArray();
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                StringWriter writer = new StringWriter();
                fjson.writeFeature(feature, writer);
                String temp = writer.toString();
                byte[] b = temp.getBytes("iso8859-1");
                temp = new String(b, "gbk");
                System.out.println("temp--:" + temp);
                array.add(temp);
            }
            features.close();

            sb.append(Arrays.toString(array.toArray()));
            sb.append("}");
            File outputfile = new File(outputPath);
            FileOutputStream fileOutputStream = new FileOutputStream(outputfile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");
            outputStreamWriter.write(String.valueOf(sb));
            outputStreamWriter.flush();
            outputStreamWriter.close();
            System.out.println("dddddddd---:" + sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("当前程序耗时：" + (endTime - startTime) + "ms");
    }
}
