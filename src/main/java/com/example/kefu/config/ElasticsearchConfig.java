package com.example.kefu.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;
    
    @Value("${elasticsearch.username:}")
    private String username;

    @Value("${elasticsearch.password:}")
    private String password;

    @Value("${elasticsearch.connectTimeout:5000}")
    private int connectTimeout;

    @Value("${elasticsearch.socketTimeout:60000}")
    private int socketTimeout;

    @Value("${elasticsearch.connectionRequestTimeout:5000}")
    private int connectionRequestTimeout;

    @Value("${elasticsearch.maxConnTotal:100}")
    private int maxConnTotal;

    @Value("${elasticsearch.maxConnPerRoute:100}")
    private int maxConnPerRoute;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, "http"));
        
        // 配置连接超时和Socket超时
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(connectTimeout);
            requestConfigBuilder.setSocketTimeout(socketTimeout);
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeout);
            return requestConfigBuilder;
        });
        
        // 配置连接池
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(maxConnTotal);
            httpClientBuilder.setMaxConnPerRoute(maxConnPerRoute);
            
            // 如果配置了用户名和密码，则设置认证信息
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            
            return httpClientBuilder;
        });
        
        return new RestHighLevelClient(builder);
    }
} 