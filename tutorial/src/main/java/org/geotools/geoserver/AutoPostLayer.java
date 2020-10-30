package org.geotools.geoserver;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.datastore.GSPostGISDatastoreEncoder;
import it.geosolutions.geoserver.rest.encoder.datastore.GSShapefileDatastoreEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * 自动发布GeoServer图层任务
 * <p>
 * GeoServerRESTManager对象是一个最大的管理者可以获取以下两个对象，创建数据存储
 * GeoServerRESTPublisher，发布对象，用来发布各种数据和创建工作空间（主要用来创建对象）
 * GeoServerRESTReader，获取数据存储、图层、样式、图层组等（主要用来获取信息）
 */
public class AutoPostLayer {
    //GeoServer连接配置
    private static String url = "http://localhost:8081/geoserver";
    private static String username = "admin";
    private static String password = "geoserver";

    public static void main(String[] args) throws Exception {

        //geoserverPublishPostgis(url, username, password);
        geoserverPublishShape(url, username, password);
    }

    /**
     * 发布PostGIS中的数据
     *
     * @param url      GeoServer连接地址
     * @param username GeoServer账号
     * @param password GeoServer密码
     */
    public static void geoserverPublishPostgis(String url, String username, String password) throws Exception {
        //    PostGIS连接配置
        String postgisHost = "localhost";
        int postgisPort = 5432;
        String postgisUser = "postgres";
        String postgisPassword = "postgres";
        String postgisDatabase = "PostGIS";
        //    待创建和发布图层的工作区名称workspace
        String ws = "javaTest";
        //    待创建和发布图层的数据存储名称
        String storeName = "testGeoServer";
        //    数据库要发布的表名称，图层名称和表名称一致
        String tableName = "java_test_publish";
        //    判断工作区是否存在，不存在则创建
        URL u = new URL(url);
        GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);

        GeoServerRESTPublisher publisher = manager.getPublisher();
        List<String> workspaces = manager.getReader().getWorkspaceNames();
        if (!workspaces.contains(ws)) {
            boolean createNewWs = publisher.createWorkspace(ws);
            System.out.println("createNewWs = " + createNewWs);
        } else {
            System.out.println("workspace已经存在：" + ws);
        }
        //判断数据存储（datastore）是否已经存在，不存在则创建
        RESTDataStore restStore = manager.getReader().getDatastore(ws, storeName);
        if (restStore == null) {
            //连接数据库
            GSPostGISDatastoreEncoder store = new GSPostGISDatastoreEncoder(storeName);
            //设置url
            store.setHost(postgisHost);
            //设置端口
            store.setPort(postgisPort);
            // 数据库的用户名
            store.setUser(postgisUser);
            // 数据库的密码
            store.setPassword(postgisPassword);
            // 数据库
            store.setDatabase(postgisDatabase);
            //当前先默认使用public这个schema
            store.setSchema("public");
            // 超时设置
            store.setConnectionTimeout(20);
            // 最大连接数
            store.setMaxConnections(20);
            // 最小连接数
            store.setMinConnections(1);
            store.setExposePrimaryKeys(true);
            boolean createStore = manager.getStoreManager().create(ws, store);
            System.out.println("createStore = " + createStore);
        } else {
            System.out.println("数据存储已经存在 = " + storeName);
        }
        //    判断图层是否存在，不存在则创建发布
        RESTLayer layer = manager.getReader().getLayer(ws, tableName);
        if (layer == null) {
            GSFeatureTypeEncoder pds = new GSFeatureTypeEncoder();
            pds.setTitle(tableName);
            pds.setName(tableName);
            pds.setSRS("EPSG:4326");
            GSLayerEncoder layerEncoder = new GSLayerEncoder();
            boolean publish = manager.getPublisher().publishDBLayer(ws, storeName, pds, layerEncoder);
            System.out.println("publish = " + publish);
        } else {
            System.out.println("图层已经发布 = " + tableName);
        }
    }

    /**
     * 发布shapeFile数据
     *
     * @param url      GeoServer连接地址
     * @param username GeoServer账号
     * @param password GeoServer密码
     */
    public static void geoserverPublishShape(String url, String username, String password) throws Exception {
        String ws = "javaTest1";
        String storeName = "testShapeStore";
        String srs = "EPSG:4326";
        //    压缩文件的完整路径(具体原因还不太清楚，实际操作：将要发布的shp文件压缩后放到该目录下即可)

        File zipFile = new File("C:/apache-tomcat-9.0.21/webapps/geoserver/data/data/shapefiles/java_test_shp.zip");
        //    图层名称
        String layerName = "java_test_shp";
        //    shp文件所在位置
        String urlDataStore = "file:C:/Users/****/Documents/java_test_shp.shp";
        //    判断工作区（workspace）是否存在，不存在则创建
        URL u = new URL(url);
        //    获取管理对象
        GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);
        //    获取发布对象
        GeoServerRESTPublisher publisher = manager.getPublisher();
        //    获取所有的工作空间名称
        List<String> workspaces = manager.getReader().getWorkspaceNames();
        //    判断工作空间是否存在
        if (!workspaces.contains(ws)) {
            boolean createWs = publisher.createWorkspace(ws);
            System.out.println("createWs = " + createWs);
        } else {
            System.out.println("工作空间已经存在 ：" + ws);
        }
        //    判断数据存储（datastore）是否存在，不存在则创建
        URL urlShapeFile = new URL(urlDataStore);
        RESTDataStore restStore = manager.getReader().getDatastore(ws, storeName);
        if (restStore == null) {
            //    创建shp文件存储
            GSShapefileDatastoreEncoder store = new GSShapefileDatastoreEncoder(storeName, urlShapeFile);
            boolean createStore = manager.getStoreManager().create(ws, store);
            System.out.println("createStore = " + createStore);

        } else {
            System.out.println("数据存储已存在storeName = " + storeName);
        }
        //    判断图层是否已经存在，不存在则创建发布
        RESTLayer layer = manager.getReader().getLayer(ws, layerName);
        if (layer == null) {
            //    发布图层
            boolean publish = manager.getPublisher().publishShp(ws, storeName, layerName, zipFile);
            System.out.println("publish = " + publish);
        } else {
            System.out.println("图层已经发布过了storeName = " + storeName);
        }
    }
}
