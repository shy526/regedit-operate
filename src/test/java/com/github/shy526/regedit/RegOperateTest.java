package com.github.shy526.regedit;

import com.github.shy526.regedit.obj.RegRootEnum;
import com.github.shy526.regedit.obj.RegValue;
import junit.framework.Assert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegOperateTest {

    public Stream<Arguments> getRegOperate() {
       return Stream.of(
                Arguments.arguments(new PrefsRegOperate(RegRootEnum.HKEY_LOCAL_MACHINE,"SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment"))
        );
    }

    @ParameterizedTest
    @MethodSource("getRegOperate")
    void getKeys(RegOperate regOperate) {
        List<RegValue> regValues = regOperate.getRegValue();
        Assert.assertFalse(regValues.isEmpty());
    }

}