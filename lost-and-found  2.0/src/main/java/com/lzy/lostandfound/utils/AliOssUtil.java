package com.lzy.lostandfound.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;

import java.io.InputStream;

public class AliOssUtil {

    // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
   private static final String ENDPOINT = "https://oss-cn-hangzhou.aliyuncs.com";
   private static final String bucketName = "hmleadnews-max";
    // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
    // 填写Bucket所在地域。以华东1（杭州）为例，Region填写为cn-hangzhou。
    private static final String REGION = "cn-hangzhou";


    public static String uploadFile(String objectName, InputStream inputStream) throws Exception {
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。

        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(ENDPOINT)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(REGION)
                .build();

        String url = "";
        try {
            // 填写字符串。
            url = "https://" + bucketName + "." + ENDPOINT.substring(ENDPOINT.lastIndexOf("/")+1) + "/" + objectName;

            String content = "Hello OSS，你好世界";

            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);

            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 上传字符串。
            ossClient.putObject(putObjectRequest);
        } catch (OSSException oe) {
            LogUtil.error("OSS上传失败，errorCode=" + oe.getErrorCode()
                    + ", requestId=" + oe.getRequestId()
                    + ", hostId=" + oe.getHostId(), oe);
            throw oe;
        } catch (ClientException ce) {
            LogUtil.error("OSS客户端异常，可能为网络或配置问题", ce);
            throw ce;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return url;
    }
}

