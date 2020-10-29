package com.oyogis.controller;

import com.oyogis.ogc.WmsServer;
import org.geotools.map.MapContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OgcController {


    @Autowired
    private static HttpServletResponse response;
    private static MapContent map = null;
    /**
     * wms服务
     *
     * @return
     */
    @GetMapping("/oyogis/wms")
    public static String wms(@RequestParam("request") String request,
                             @RequestParam("layers") String layers,
                             @RequestParam("bbox") String bbox,
                             @RequestParam("width") String width,
                             @RequestParam("height") String height)throws Exception {


        String[] bboxs=bbox.split(",");
        double[] _bbox=new double[]{
                Double.parseDouble(bboxs[0]),
                Double.parseDouble(bboxs[1]),
                Double.parseDouble(bboxs[2]),
                Double.parseDouble(bboxs[3]),

        };
        Map paras = new HashMap();
        paras.put("bbox", _bbox);
        paras.put("width", Integer.parseInt(width));
        paras.put("height", Integer.parseInt(height));
        WmsServer wmsServer = new WmsServer();
        wmsServer.setMapContent(paras, map, response);
        return "这是wms"+request;
    }
}
