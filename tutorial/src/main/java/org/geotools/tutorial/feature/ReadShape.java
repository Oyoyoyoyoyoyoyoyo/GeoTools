package org.geotools.tutorial.feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import javax.activation.FileDataSource;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 读取shp文件，导出字段属性
 */
public class ReadShape {
    public static void main(String[] args) {
        List<Map<String, Object>> list = new ArrayList<>();
        long start = System.currentTimeMillis();
        try {
            //读取shp文件
            File file = JFileDataStoreChooser.showOpenFile("shp", null);
            if (file == null) {
                return;
            }
            ShapefileDataStore store = (ShapefileDataStore) FileDataStoreFinder.getDataStore(file);
            SimpleFeatureSource featureSource = store.getFeatureSource();
            //    设置编码
            Charset charset = Charset.forName("GBK");
            store.setCharset(charset);
            SimpleFeatureCollection simpleFeatureCollection = featureSource.getFeatures();
            SimpleFeatureIterator itertor = simpleFeatureCollection.features();
            //获取shp字段名
            SimpleFeatureType sft = ((ContentFeatureSource) featureSource).getSchema();
            List<AttributeDescriptor> attrs = sft.getAttributeDescriptors();
            for (int i = 0; i < attrs.size(); i++) {
                AttributeDescriptor attr = attrs.get(i);
                String fieldName = attr.getName().toString();
                System.out.println("字段--:" + fieldName);
            }
            //获取shp属性值
            while (itertor.hasNext()) {
                SimpleFeature feature = itertor.next();
                List<Object> attributes = feature.getAttributes();
                System.out.println("attributes--:"+Arrays.toString(attributes.toArray()));
            }
            itertor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("共耗时" + (System.currentTimeMillis() - start) + "ms");
    }
}
