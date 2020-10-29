package com.oyogis.ogc;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

public class WmsServer {
    public  void setMapContent(Map paras, MapContent map, HttpServletResponse response) throws Exception {
        double[] bbox = (double[]) paras.get("bbox");
        double x1 = bbox[0], y1 = bbox[1],
                x2 = bbox[2], y2 = bbox[3];
        int width = (int) paras.get("width"),
                height = (int) paras.get("height");
        //设置输出范围
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
        ReferencedEnvelope mapArea = new ReferencedEnvelope(x1, x2, y1, y2, crs);
        //    初始化渲染器
        StreamingRenderer sr = new StreamingRenderer();
        sr.setMapContent(map);
        //    初始化输出图像
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle rect = new Rectangle(0, 0, width, height);
        //    绘制地图
        sr.paint((Graphics2D) g, rect, mapArea);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean flag = ImageIO.write(bi, "png", out);
        byte[] wmsByte = out.toByteArray();


        OutputStream os = response.getOutputStream();
        InputStream is = new ByteArrayInputStream(wmsByte);
        try {
            int count = 0;
            byte[] buffer = new byte[1024 * 1024];
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            os.close();
            is.close();
        }
    }
}
