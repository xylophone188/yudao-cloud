package top.melody.cloud.game.bloom.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

@Data
@TableName(value = "role_dictionary",autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDictionaryDO extends BaseDO {

    /**
     * 表id
     */
    @TableId
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色首夜行动顺序
     */
    private Long roleFirstNightOrder;

    /**
     * 角色他夜行动顺序
     */
    private Long roleOtherNightOrder;

    /**
     * 角色能力
     */
    private String roleAbility;

    /**
     * 角色介绍
     */
    private String roleIntroduction;

    /**
     * 运作方式
     */
    private String roleOperationMode;

    /**
     * 提示印记
     */
    private String rolePromptImprint;

    /**
     * 规则细节
     */
    private String roleRuleDetails;

    /**
     * 创意来源
     */
    private String creativeFrom;

    /**
     * 角色id
     */
    private String roleId;

    /**
     * 角色类型
     */
    private String roleType;

    /**
     * 角色阵营
     */
    private String roleCamp;

    /**
     * 角色图标
     */
    private String roleIcon;

    /**
     * 角色背景故事
     */
    private String roleBackgroundStory;

    /**
     * 所属角色合集
     */
    private String collectionOfBelongingRole;

    /**
     * 角色能力类型
     */
    private Integer roleAbilityType;

    /**
     * 角色示例
     */
    private String roleExample;

    /**
     * 提示与技巧
     */
    private String roleTipsAndTechniques;

    /**
     * 伪装技巧
     */
    private String roleCamouflageTechniques;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 版本号
     */
    private Integer version;

}
