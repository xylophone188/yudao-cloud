package top.melody.cloud.game.bloom.reptiles;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@Component
public class MyApplicationRunner implements ApplicationRunner {

    Logger logger = Logger.getLogger(MyApplicationRunner.class.getName());


    private final HttpXxlJob httpXxlJob;

    public MyApplicationRunner(HttpXxlJob httpXxlJob) {
        this.httpXxlJob = httpXxlJob;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("开始爬取百科");
        HashMap<String, Integer> deleteMap = new HashMap<>();
        deleteMap.put("华灯初上",2);
        List<String> todoUpdate = new ArrayList<>();
        todoUpdate.add("山雨欲来");
        httpXxlJob.httpJobHandler(deleteMap,todoUpdate,"镇民", "外来者", "爪牙", "恶魔", "旅行者", "传奇角色", "华灯初上", "%E5%A4%9C%E6%99%9A%E8%A1%8C%E5%8A%A8%E9%A1%BA%E5%BA%8F%E4%B8%80%E8%A7%88");
    }
}
