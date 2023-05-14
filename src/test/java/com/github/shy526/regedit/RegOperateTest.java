package com.github.shy526.regedit;

import com.github.shy526.regedit.obj.RegRootEnum;
import com.github.shy526.regedit.obj.RegTypeEnum;
import com.github.shy526.regedit.obj.RegValue;
import com.github.shy526.regedit.shell.ShellClient;
import junit.framework.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegOperateTest {

    public Stream<Arguments> getRegOperateTestObj() {
        return Stream.of(
                Arguments.arguments(new CmdRegOperate(RegRootEnum.HKEY_LOCAL_MACHINE, AbsRegOperate.SYS_ENVIRONMENT)),
                Arguments.arguments(new PrefsRegOperate(RegRootEnum.HKEY_CURRENT_USER, AbsRegOperate.USER_ENVIRONMENT))
        );
    }

    @Order(3)
    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void getNodes(RegOperate regOperate) {
        Set<String> nodes = regOperate.getNodes();
        Assert.assertTrue(nodes.size() > 0);
    }

    @Order(4)
    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void deleteNode(RegOperate regOperate) {
        boolean test1 = regOperate.deleteNode("test1");
        Assert.assertTrue(test1);
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void getNode(RegOperate regOperate) {
        String test1 = regOperate.getNode("test1");
        String test3 = regOperate.getNode("test3");
        Assert.assertNotNull(test1);
        Assert.assertNull(test3);
    }

    @Order(1)
    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void createNode(RegOperate regOperate) {
        boolean test1 = regOperate.createNode("test1");
        Assert.assertTrue(test1);
    }

    @Order(8)
    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void getRegValue(RegOperate regOperate) {
        List<RegValue> regValue = regOperate.getRegValue();
        Assert.assertTrue(regValue.size() > 0);
    }

    @Order(6)
    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void testGetRegValue(RegOperate regOperate) {
        RegValue test = regOperate.getRegValue("test1");
        RegValue test2 = regOperate.getRegValue("test2");
        Assert.assertNotNull(test);
        Assert.assertNull(test2);
    }

    @Order(7)
    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void deleteRegValue(RegOperate regOperate) {
        boolean test1 = regOperate.deleteRegValue("test1");
        Assert.assertTrue(test1);
    }

    @Order(5)
    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void setRegValue(RegOperate regOperate) {
        RegValue of = RegTypeEnum.REG_SZ.of("test1", "test");
        boolean b = regOperate.setRegValue(of);
        Assert.assertTrue(b);
    }

    @Order(Integer.MAX_VALUE)
    @ParameterizedTest
    @MethodSource("getRegOperateTestObj")
    void flush(RegOperate regOperate) {
        regOperate.refreshEnvironment();
    }


    @Test
    void test(){
        CmdRegOperate cmdRegOperate = new CmdRegOperate(RegRootEnum.HKEY_LOCAL_MACHINE, AbsRegOperate.SYS_ENVIRONMENT);
        RegValue A = RegTypeEnum.REG_SZ.of("A", "test");

        System.out.println( cmdRegOperate.setRegValue(A));
        RegValue B = RegTypeEnum.REG_EXPAND_SZ.of("B", "%A%/bin");

        System.out.println( cmdRegOperate.setRegValue(B));
        RegValue C = RegTypeEnum.REG_EXPAND_SZ.of("C", "%A%/bin/cc");
        System.out.println( cmdRegOperate.setRegValue(C));
        cmdRegOperate.refreshEnvironment();
    }
    @Test
    void test1(){
        int exec = ShellClient.exec("cmd /c scala11 -version 2>&1", str->{
            System.out.println("成功 = " + str);
        }, str -> {
            System.out.println("失败" + str);
        });
        System.out.println("exec = " + exec);
    }



}