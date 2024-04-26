package top.melody.cloud.game.bloom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.melody.cloud.game.bloom.dal.dataobject.RoleDictionaryDO;

import java.util.List;

public interface RoleDictionaryService extends IService<RoleDictionaryDO> {
    void batchAddOrUpdate(List<RoleDictionaryDO> roleDictionaries);
}
