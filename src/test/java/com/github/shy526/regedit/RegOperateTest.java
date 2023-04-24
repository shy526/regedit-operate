package com.github.shy526.regedit;

import com.github.shy526.regedit.obj.RegRootEnum;
import com.github.shy526.regedit.obj.RegValue;
import junit.framework.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegOperateTest {

    public Stream<Arguments> getRegOperateTestObj() {
       return Stream.of(
                Arguments.arguments(new CmdRegOperate(RegRootEnum.HKEY_LOCAL_MACHINE,"SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment"))
        );
    }

    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void getKeys(RegOperate regOperate) {
        List<RegValue> regValues = regOperate.getRegValue();
        Assert.assertFalse(regValues.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void getNodes(RegOperate regOperate) {
        Set<String> nodes = regOperate.getNodes();
        System.out.println("nodes = " + nodes);
    }

    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void deleteNode(RegOperate regOperate) {
        boolean test1 = regOperate.deleteNode("test1");
        System.out.println("test1 = " + test1);
    }

    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void getNode(RegOperate regOperate) {
        String test1 = regOperate.getNode("test1");
        String test3 = regOperate.getNode("test3");
        Assert.assertNotNull(test1);
        Assert.assertNull(test3);
    }

    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void createNode(RegOperate regOperate) {
        boolean test1 = regOperate.createNode("test1");
        Assert.assertTrue(test1);
    }

    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void getRegValue(RegOperate regOperate) {
        List<RegValue> regValue = regOperate.getRegValue();
        Assert.assertTrue(regValue.size()>0);
    }

    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void testGetRegValue(RegOperate regOperate) {
        RegValue javaHome = regOperate.getRegValue("JAVA_HOME");
        System.out.println("javaHome = " + javaHome);
      //  Assert.assertTrue(javaHome.size()>0);
    }

    @Test
    void deleteRegValue() {
    }

    @Test
    void setRegValue() {
    }

    @Test
    void flush() {
    }
}