package org.Core.Configurations.Redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RedisConfig {

    @Bean
    public RedisTemplate<String,String> gameSessionTemplate(
            RedisConnectionFactory factory) {

        RedisTemplate<String,String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer s = new StringRedisSerializer();
        template.setKeySerializer(s);
        template.setValueSerializer(s);

        template.afterPropertiesSet();
        return template;
    }

}
