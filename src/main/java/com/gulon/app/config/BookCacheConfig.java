package com.gulon.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "book.cache")
@Getter
@Setter
public class BookCacheConfig {
    
    private Integer imageExpiry = 86400; // 24시간 (초)
    private Integer searchExpiry = 3600; // 1시간 (초)
    private Integer detailExpiry = 7200; // 2시간 (초)
} 