package com.ddbz.daymark.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * 编写jwt配置信息
 */
@Component
@Data
//能从配置文件中获取secret和expire信息
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expire;
}
