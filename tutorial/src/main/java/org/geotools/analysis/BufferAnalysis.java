package org.geotools.analysis;

import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.geotools.common.UtilTools;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.GeometryFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓冲区分析
 * Geometry类包含计算面积/长度/缓冲区分析/是否包含/获取边界/获取中心点等分析方法
 */
public class BufferAnalysis {


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        BufferAnalysis geoR = new BufferAnalysis();
        //读取文件
        File shpFile = JFileDataStoreChooser.showOpenFile("shp", null);
        if (shpFile == null) {
            return;
        }
        // 输出文件为同路径下同文件名,不同格式
        String bufFile = shpFile.getAbsolutePath().replace(".shp", "_buf.shp");
        try {
            //读取shp文件
            ShapefileDataStore shpDataStore = new ShapefileDataStore(shpFile.toURI().toURL());
            //设置编码
            Charset charset = Charset.forName("GBK");
            shpDataStore.setCharset(charset);
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);
            SimpleFeatureCollection result = featureSource.getFeatures();
            SimpleFeatureIterator itertor = result.features();
            //创建shape文件对象
            File fileBuf = new File(bufFile);
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put(ShapefileDataStoreFactory.URLP.key, fileBuf.toURI().toURL());
            ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
            SimpleFeatureType sft = featureSource.getSchema();
            List<AttributeDescriptor> attrs = sft.getAttributeDescriptors();
            //定义图形信息和属性信息
            SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
            tb.setCRS(DefaultGeographicCRS.WGS84);
            tb.setName("shapefile");
            for (int i = 0; i < attrs.size(); i++) {
                AttributeDescriptor attr = attrs.get(i);
                String fieldName = attr.getName().toString();
                if (fieldName == "the_geom") {

                    tb.add(fieldName, Polygon.class);
                } else {
                    tb.add(fieldName, String.class);
                }
            }
            String geoType = null;
            while (itertor.hasNext()) {
                SimpleFeature feature = itertor.next();
                Geometry geo = (Geometry) feature.getAttribute("the_geom");
                geoType = geo.getGeometryType();
                if (geoType != null) {
                    break;
                }
            }
            SimpleFeatureType TYPE = UtilTools.getSimpleTypeByAttrs(shpFile, attrs, geoType);
            System.out.println("TYPE = " + TYPE);
            ds.createSchema(TYPE);
            //设置编码
            ds.setCharset(charset);
            //设置Writer
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
            while (itertor.hasNext()) {
                SimpleFeature feature = itertor.next();
                System.out.println("feature = " + feature);
                SimpleFeature featureBuf = writer.next();
                featureBuf.setAttributes(feature.getAttributes());
                Geometry geo = (Geometry) feature.getAttribute("the_geom");
                //计算每一个feature缓冲区
                Geometry geoBuffer = geo.buffer(UtilTools.mToDegrees(1000));
                featureBuf.setAttribute("the_geom", geoBuffer);
            }
            writer.write();
            writer.close();
            itertor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("当前程序耗时：" + (endTime - startTime) + "ms");
    }
}
