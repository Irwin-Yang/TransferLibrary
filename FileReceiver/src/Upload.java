import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Irwin on 2016/3/16.
 */
@WebServlet("/upload")
public class Upload extends HttpServlet {
    static final Logger mLogger = Logger.getLogger(Upload.class.getSimpleName());

    public Upload() {
        super();
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //设置响应内容类型

        resp.setContentType("text/html");

        //设置逻辑实现
        PrintWriter out = resp.getWriter();

        out.println("<h3>" + "Response form server: Get method" + "</h3>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            doUpload(request, response);
        } catch (FileUploadException e) {
            e.printStackTrace();
            response.sendError(500, "Upload fail: " + e.getMessage());
        }
//        uploadTest(request, response);
    }

    private void uploadTest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Enumeration<String> enumeration = request.getHeaderNames();
        String key = null;
        while (enumeration.hasMoreElements()) {
            key = enumeration.nextElement();
            mLogger.info(String.format("%s=%s", key, request.getHeader(key)));
        }
        String root = "D://UploadFiles/";
        File rootF = new File(root);
        if (!rootF.exists()) {
            rootF.mkdirs();
        }
        File target = new File(rootF, "Test.txt");
        target.createNewFile();
        byte[] buffer = new byte[1024];
        BufferedInputStream ins = null;
        FileOutputStream outs = null;
        try {
            outs = new FileOutputStream(target);
            ins = new BufferedInputStream(request.getInputStream());
            int length = 0;
            int writted = 0;
            while ((length = ins.read(buffer)) != -1) {
                if (length > 0) {
                    outs.write(buffer, 0, length);
                    writted += length;
                    mLogger.info("Write Bytes: " + length);
                }
            }
            response.getWriter().write("This is from Test and ok");
            ins.close();
            ins = null;
            outs.close();
            outs = null;
        } finally {
            if (ins != null) {
                ins.close();
            }
            if (outs != null) {
                outs.close();
            }
        }
    }


    private void doUpload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FileUploadException {

        request.setCharacterEncoding("utf-8");  //设置编码

        //Code below need import library:commons-io.jar,commons-fileupload.jar
        //获得磁盘文件条目工厂
        DiskFileItemFactory factory = new DiskFileItemFactory();
        //获取文件需要上传到的路径
        String path = "D://UploadFiles/";
        File root = new File(path);
        if (!root.exists()) {
            root.mkdirs();
        }

        //如果没以下两行设置的话，上传大的 文件 会占用 很多内存，
        //设置暂时存放的 存储室 , 这个存储室，可以和 最终存储文件 的目录不同
        /**
         * 原理 它是先存到 暂时存储室，然后在真正写到 对应目录的硬盘上，
         * 按理来说 当上传一个文件时，其实是上传了两份，第一个是以 .tem 格式的
         * 然后再将其真正写到 对应目录的硬盘上
         */
        factory.setRepository(new File(path));
        //设置 缓存的大小，当上传文件的容量超过该缓存时，直接放到 暂时存储室
        factory.setSizeThreshold(1024 * 1024);

        //高水平的API文件上传处理
        ServletFileUpload upload = new ServletFileUpload(factory);

        OutputStream outs = null;
        InputStream ins = null;
        try {

            List<FileItem> list = upload.parseRequest(request);

            for (FileItem item : list) {
                //获取表单的属性名字
                String name = item.getFieldName();

                //如果获取的 表单信息是普通的 文本 信息
                if (item.isFormField()) {
                    //获取用户具体输入的字符串 ，名字起得挺好，因为表单提交过来的是 字符串类型的
                    String value = item.getString();

                    request.setAttribute(name, value);
                    mLogger.info(String.format("Form field: %s=%s", name, value));
                }
                //对传入的非 简单的字符串进行处理 ，比如说二进制的 图片，电影这些
                else {
                    /**
                     * 以下三步，主要获取 上传文件的名字
                     */
                    //获取路径名
                    String value = item.getName();
                    //索引到最后一个反斜杠
                    int start = value.lastIndexOf("\\");
                    //截取 上传文件的 字符串名字，加1是 去掉反斜杠，
                    String filename = value.substring(start + 1);

                    request.setAttribute(name, filename);
                    mLogger.info(String.format("File field: %s=%s", name, value));

                    //真正写到磁盘上
                    //它抛出的异常 用exception 捕捉

                    //item.write( new File(path,filename) );//第三方提供的

                    //手动写的
                    outs = new FileOutputStream(new File(path, filename));

                    ins = item.getInputStream();

                    int length = 0;
                    byte[] buf = new byte[1024];

                    while ((length = ins.read(buf)) != -1) {
                        outs.write(buf, 0, length);
                    }
                    ins.close();
                    outs.close();
                    ins = null;
                    outs = null;
                }
            }
            response.setStatus(200);
            response.getWriter().write("Upload success");
        } finally {
            if (outs != null) {
                outs.close();
            }
            if (ins != null) {
                ins.close();
            }
        }


    }


    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return super.getLastModified(req);
    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doTrace(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        super.service(req, res);
    }
}
