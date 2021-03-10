package com.changgou.search.util;

import com.changgou.search.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 实现fastdfs文件管理
 *      文件上传
 *      文件删除
 *      文件下载
 *      文件信息获取
 *      storage信息获取
 *      tracker信息获取
 */
public class FastDFSUtil {
    /**
     * 加载连接信息
     */
    static {
        //查找classpath下的文件路径
        String filename = new ClassPathResource("fdfs_client.conf").getPath();
        try {
            ClientGlobal.init(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     */
    public static String  upload(FastDFSFile fastDFSFile) throws Exception {
        //附加参数
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author", fastDFSFile.getAuthor());


        //1.连接.static已经做了
        //2、创建一个Tracker访问的对象客户端对象Tracker
        TrackerClient trackerClient = new TrackerClient();
        //3.通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();
        //4.通过TrakcerServer的连接信息可以获取Storage的连接信息，创建StorageClient对象存储的Storage的连接信息
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);

        //5、通过StorageClient访问Storage,实现文件上传，并且获取文件上传后的存储信息
        String uploads = storageClient1.upload_file1(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
        System.out.println(uploads.toString()+"))))))))))))))))))");
        return uploads ;

    }

    /**
     * 获取文件信息、
     *
     * groupName:组名
     * remoteFileName :文件存储完整名
     */
    public static FileInfo getFile(String fileId) throws Exception {
        //1.先创建TrackerClient对象，通过TrackerClient对象访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //2.通过TrackerClient 获取TarackerServer的连接对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //3.通过TrackerServer获取Storage,创建StorageClient对象Storage存储信息
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
        //4.获取文件信息
        FileInfo fileInfo = storageClient1.get_file_info1(fileId);
        return fileInfo;
    }

    /**
     * 文件下载
     * @param
     * @throws
     */
    public static InputStream downloadFile(String fileId) throws Exception {
        //1.先创建TrackerClient对象，通过TrackerClient对象访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //2.通过TrackerClient 获取TarackerServer的连接对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //3.通过TrackerServer获取Storage,创建StorageClient对象Storage存储信息
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
        //下载
        byte[] bytes = storageClient1.download_file1(fileId);
        /*FileOutputStream outputStream = new FileOutputStream("E:\\黑马2020新\\16_畅购商场\\day71_ChangGou\\day02讲义\\1.jpg");
        outputStream.write(bytes);
        outputStream.close();*/
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 删除文件
     * @param
     * @throws
     */
    public static void deleteFile(String fileId) throws Exception {
        //1.先创建TrackerClient对象，通过TrackerClient对象访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //2.通过TrackerClient 获取TarackerServer的连接对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //3.通过TrackerServer获取Storage,创建StorageClient对象Storage存储信息
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
        //删除
        storageClient1.delete_file1(fileId);
    }

    /**
     * 获取storage信息
     * @param
     * @throws
     */
    public static StorageServer getStorages() throws Exception {
        //1.先创建TrackerClient对象，通过TrackerClient对象访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //2.通过TrackerClient 获取TarackerServer的连接对象
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
        return storeStorage;
    }

    /***
     * 获取Storage信息,IP和端口
     * groupName
     * remoteFileName
     * @return
     * @throws IOException
     */
    public static ServerInfo[] getServerInfo(String fileId) throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getFetchStorages1(trackerServer, fileId);
    }

    /**
     * 获取Tracker信息
     */
    public static String getTrackerInfo() throws Exception {
        //1.先创建TrackerClient对象，通过TrackerClient对象访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //2.通过TrackerClient 获取TarackerServer的连接对象
        TrackerServer trackerServer = trackerClient.getConnection();

        //tracker的ip和http端口
        String ip = trackerServer.getInetSocketAddress().getHostString();
        int tracker_http_port = ClientGlobal.getG_tracker_http_port();
        String url = "http://" + ip +":"+ tracker_http_port;
        return url;
    }

    public static void main(String[] args) throws Exception {
//        FileInfo fileInfo = getFile("group1/M00/00/00/wKgqiGBCapCAeu5hAAAWh_Cdpwo640.jpg");
//        System.out.println(fileInfo.getSourceIpAddr()+"::::ip");

//      downloadFile("group1/M00/00/00/wKgqiGBCe3yAHHdsAAATOvfj4Wg695.png");

//        deleteFile("group1/M00/00/00/wKgqiGBCflaAU1ZAAAAZHteHGzw987.png");

        /*StorageServer storages = getStorages();
        System.out.println(storages.getStorePathIndex());*/

        ServerInfo[] serverInfo = getServerInfo("group1/M00/00/00/wKgqiGBCZFqAGDjxAABVzVJZznU29.jpeg ");
        for (ServerInfo info : serverInfo) {
            System.out.println(info.getIpAddr());
        }
    }


}
