package top.melody.cloud.game.bloom.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.melody.cloud.game.bloom.dal.dataobject.RoleDictionaryDO;
import top.melody.cloud.game.bloom.dao.RoleDictionaryMapper;
import top.melody.cloud.game.bloom.service.RoleDictionaryService;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
public class RoleDictionaryServiceImpl extends ServiceImpl<RoleDictionaryMapper, RoleDictionaryDO> implements RoleDictionaryService {

    Logger logger = Logger.getLogger(this.getClass().getName());

    @Resource
    private RoleDictionaryMapper roleDictionaryMapper;

    @Override
    // 开启事务，如果抛出异常回滚，否则提交事务
    @Transactional(rollbackFor = Exception.class)
    public void batchAddOrUpdate(List<RoleDictionaryDO> roleDictionaries) {
        List<RoleDictionaryDO> roleDictionaryDOS = roleDictionaryMapper.selectList(
                new LambdaQueryWrapper<RoleDictionaryDO>()
                        .in(
                                RoleDictionaryDO::getRoleId,
                                roleDictionaries.stream().map(RoleDictionaryDO::getRoleId).collect(Collectors.toList())
                        )
        );
        if (!roleDictionaryDOS.isEmpty()) {
            logger.info("数据库不为空,目前暂时不做更替逻辑,只处理新增,所以本次不做插入!");
            List<String> roleIds = roleDictionaryDOS.stream().map(RoleDictionaryDO::getRoleId).toList();
            // 暂时不管更替逻辑只处理新增
            List<RoleDictionaryDO> collect = roleDictionaries.stream().filter(a -> !roleIds.contains(a.getRoleId())).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                saveBatch(collect);
            }
            roleDictionaries.removeAll(collect);
            Map<String, Long> idMap = roleDictionaryDOS.stream().collect(Collectors.toMap(RoleDictionaryDO::getRoleId, RoleDictionaryDO::getId));
            roleDictionaries.forEach(a -> a.setId(idMap.get(a.getRoleId())));
            updateBatchById(roleDictionaries);
        } else {
            saveBatch(roleDictionaries);
        }
    }
}
