package org.geotools.common;

import com.alibaba.fastjson.JSONObject;
import org.geotools.data.DataUtilities;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.File;
import java.util.List;

/**
 * 常用工具方法类
 */
public class UtilTools {
    public static final double DISTANCE = 0.000008983153;

    /**
     * 获取SimpleFeatureType(读取geojson/wkt数据用)
     *
     * @param file
     * @param attrs0
     * @param strType geom数据类型
     * @return
     * @throws Exception
     */
    public static SimpleFeatureType getSimpleTypeByAttr0(File file, JSONObject attrs0, String strType) throws Exception {

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
     * 获取SimpleFeatureType(读取shp数据用)
     *
     * @param file
     * @param attrs
     * @param strType geom数据类型
     * @return
     * @throws Exception
     */
    public static SimpleFeatureType getSimpleTypeByAttrs(File file, List<AttributeDescriptor> attrs, String strType) throws Exception {

        String outPutFileName = file.getName();
        //数据集合类型
        String geomType = "the_geom:" + strType + ":srid=4326,";
        //属性字段
        String geomAttrs = "";
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDescriptor attr = attrs.get(i);
            String fieldName = attr.getName().toString();
            if (fieldName == "the_geom") {
                continue;
            }
            geomAttrs = fieldName + ":String," + geomAttrs;
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
     * 距离(m)到度的转换
     * 转换算法：degree=length/(2*Math.PI*6371004)*360;
     *
     * @param length 缓冲区距离 单位m
     * @return
     */
    public static double mToDegrees(double length) {
        //100米=0.0008983153 Degrees
        //degree = meter / (2 * Math.PI * 6371004) * 360
        double degree = length / (2 * Math.PI * 6371004) * 360;
        return degree;
    }
}
