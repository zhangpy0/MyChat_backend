package top.zhangpy.mychat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "top.zhangpy.mychat")
@MapperScan("top.zhangpy.mychat.mapper")
@EnableAsync
@EnableTransactionManagement
@EnableScheduling
public class MyChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyChatApplication.class, args);
    }

}
