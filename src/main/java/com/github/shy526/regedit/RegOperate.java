package com.github.shy526.regedit;

import com.github.shy526.regedit.obj.RegValue;

import java.util.List;
import java.util.Set;

public interface RegOperate {


    /**
     * 当前key 下的subKey
     *
     * @return Set<String>
     */
    Set<String> getNodes();

    /**
     * 删除节点
     *
     * @param name name
     * @return true
     */
    boolean deleteNode(String name);

    /**
     * 获取单个节点
     *
     * @param name name
     * @return String
     */
    String getNode(String name);

    /**
     * 创建节点
     * 如果节点已经创建会覆盖
     *
     * @param name 节点名称
     * @return bo
     */
    boolean createNode(String name);

    /**
     * 获取 key 下的所有RegValue
     *
     * @return List<RegValue>
     */
    List<RegValue> getRegValue();

    /**
     * getChildren
     *
     * @param regValueName 键
     * @return RegValue
     */
    RegValue getRegValue(String regValueName);

    /**
     * 删除
     *
     * @param regValueName 键
     * @return boolean
     */
    boolean deleteRegValue(String regValueName);

    /**
     * 设置
     *
     * @param regValue regValue
     * @return boolean
     */
    boolean setRegValue(RegValue regValue);

    /**
     * 用来刷新环境
     */
    void refreshEnvironment();


}
