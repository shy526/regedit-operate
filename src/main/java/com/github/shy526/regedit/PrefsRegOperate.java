package com.github.shy526.regedit;


import com.github.shy526.regedit.obj.RegRootEnum;
import com.github.shy526.regedit.obj.RegTypeEnum;
import com.github.shy526.regedit.obj.RegValue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 使用了
 * 对注册表进行操作 只能操作REG_SZ的value数据
 * 对WindowsPreferences进行了反射 以能读取任何位置的注册表数据
 * key/value 取消了 A到/A 的转换
 */
public class PrefsRegOperate extends AbsRegOperate {

    private final static Class<?> WINDOWS_PREFERENCES_CLASS;

    /* Constants used to interpret returns of native functions    */
    private static final int NATIVE_HANDLE = 0;
    private static final int ERROR_CODE = 1;
    private static final int SUBKEYS_NUMBER = 0;
    private static final int VALUES_NUMBER = 2;
    private static final int MAX_KEY_LENGTH = 3;
    private static final int MAX_VALUE_NAME_LENGTH = 4;

    private static final int DISPOSITION = 2;
    private static final int REG_CREATED_NEW_KEY = 1;
    private static final int ERROR_SUCCESS = 0;


    /* Windows security masks */
    private static final int KEY_ALL_ACCESS = 0xf003f;

    private static final Method WINDOWS_REG_QUERY_VALUE_EX;
    private static final Method WINDOWS_REG_SET_VALUE_EX;
    private static final Method WINDOWS_REG_OPEN_KEY;
    private static final Method WINDOWS_REG_CLOSE_KEY;
    private static final Method STRING_TO_BYTE_ARRAY;
    private static final Method TO_JAVA_VALUE_STRING;
    private static final Method TO_WINDOWS_VALUE_STRING;
    private static final Method WINDOWS_REG_QUERY_INFO_KEY;
    private static final Method WINDOWS_REG_ENUM_VALUE;
    private static final Method WINDOWS_REG_ENUM_KEY_EX;
    private static final Method WINDOWS_REG_DELETE_VALUE;
    private static final Method WINDOWS_REG_DELETE_KEY;
    private static final Method WINDOWS_REG_CREATE_KEY_EX;
    private static final Method WINDOWS_REG_FLUSH_KEY;


    static {
        try {
            WINDOWS_PREFERENCES_CLASS = Class.forName("java.util.prefs.WindowsPreferences");
            WINDOWS_REG_QUERY_VALUE_EX = getMethod("WindowsRegQueryValueEx", new Class[]{int.class, byte[].class});
            WINDOWS_REG_SET_VALUE_EX = getMethod("WindowsRegSetValueEx", new Class[]{int.class, byte[].class, byte[].class});
            WINDOWS_REG_OPEN_KEY = getMethod("WindowsRegOpenKey", new Class[]{int.class, byte[].class, int.class});
            WINDOWS_REG_CLOSE_KEY = getMethod("WindowsRegCloseKey", new Class[]{int.class});
            STRING_TO_BYTE_ARRAY = getMethod("stringToByteArray", new Class[]{String.class});
            TO_WINDOWS_VALUE_STRING = getMethod("toWindowsValueString", new Class[]{String.class});
            TO_JAVA_VALUE_STRING = getMethod("toJavaValueString", new Class[]{byte[].class});
            WINDOWS_REG_QUERY_INFO_KEY = getMethod("WindowsRegQueryInfoKey", new Class[]{int.class});
            WINDOWS_REG_ENUM_VALUE = getMethod("WindowsRegEnumValue", new Class[]{int.class, int.class, int.class});
            WINDOWS_REG_ENUM_KEY_EX = getMethod("WindowsRegEnumKeyEx", new Class[]{int.class, int.class, int.class});
            WINDOWS_REG_DELETE_VALUE = getMethod("WindowsRegDeleteValue", new Class[]{int.class, byte[].class});
            WINDOWS_REG_DELETE_KEY = getMethod("WindowsRegDeleteKey", new Class[]{int.class, byte[].class});
            WINDOWS_REG_CREATE_KEY_EX = getMethod("WindowsRegCreateKeyEx", new Class[]{int.class, byte[].class});
            WINDOWS_REG_FLUSH_KEY = getMethod("WindowsRegFlushKey", new Class[]{int.class});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public PrefsRegOperate(RegRootEnum rootEnum, String keyName) {
        super(rootEnum, keyName);
    }

    private <T> T invoke(Method method, Class<T> returnClass, Object... arg) {
        try {
            return (T) method.invoke(null, arg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getMethod(String method, Class<?>[] par) {
        Method m = null;
        try {
            m = WINDOWS_PREFERENCES_CLASS.getDeclaredMethod(method, par);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        m.setAccessible(true);
        return m;
    }

    private int[] windowsRegOpenKey(int root, byte[] pathBytes, int securityMask) {
        return invoke(WINDOWS_REG_OPEN_KEY, int[].class, root, pathBytes, securityMask);
    }

    private int windowsRegCloseKey(Integer nativeHandle) {
        if (nativeHandle != null) {
            return invoke(WINDOWS_REG_CLOSE_KEY, int.class, nativeHandle);
        }
        return -1;
    }

    private byte[] windowsRegQueryValueEx(int nativeHandle, byte[] windowsName) {
        return invoke(WINDOWS_REG_QUERY_VALUE_EX, byte[].class, nativeHandle, windowsName);
    }

    private byte[] stringToByteArray(String str) {
        return invoke(STRING_TO_BYTE_ARRAY, byte[].class, str);
    }

    private String toJavaValueString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return invoke(TO_JAVA_VALUE_STRING, String.class, (Object) bytes);
    }

    private int windowsRegSetValueEx(int nativeHandle, byte[] key, byte[] val) {
        return invoke(WINDOWS_REG_SET_VALUE_EX, int.class, nativeHandle, key, val);
    }

    private byte[] toWindowsValueString(String str) {
        return invoke(TO_WINDOWS_VALUE_STRING, byte[].class, str);
    }

    private int[] windowsRegQueryInfoKey(int nativeHandle) {
        return invoke(WINDOWS_REG_QUERY_INFO_KEY, int[].class, nativeHandle);
    }

    private byte[] windowsRegEnumKeyEx(int nativeHandle, int subKeyIndex, int maxKeyLen) {
        return invoke(WINDOWS_REG_ENUM_KEY_EX, byte[].class, nativeHandle, subKeyIndex, maxKeyLen);
    }

    private byte[] windowsRegEnumValue(int nativeHandle, int valueIndex, int maxValueNameLength) {
        return invoke(WINDOWS_REG_ENUM_VALUE, byte[].class, nativeHandle, valueIndex, maxValueNameLength);
    }

    private int windowsRegDeleteValue(int nativeHandle, byte[] valueName) {
        return invoke(WINDOWS_REG_DELETE_VALUE, int.class, nativeHandle, valueName);
    }

    private int windowsRegDeleteKey(int nativeHandle, byte[] subKey) {
        return invoke(WINDOWS_REG_DELETE_KEY, int.class, nativeHandle, subKey);
    }

    private int[] windowsRegCreateKeyEx(int nativeHandle, byte[] subKey) {
        return invoke(WINDOWS_REG_CREATE_KEY_EX, int[].class, nativeHandle, subKey);
    }

    public int windowsRegFlushKey(Integer nativeHandle) {
        if (nativeHandle != null) {
            return invoke(WINDOWS_REG_FLUSH_KEY, int.class, nativeHandle);
        }
        return 0;
    }

    private <T> T handle(Function<Integer, T> function) {
        Integer nativeHandle = null;
        T result = null;
        try {
            int[] openKeyResult = windowsRegOpenKey(getRootEnum().getCode(), stringToByteArray(getKeyName()), KEY_ALL_ACCESS);
            if (openKeyResult[ERROR_CODE] != ERROR_SUCCESS) {
                return null;
            }
            nativeHandle = openKeyResult[NATIVE_HANDLE];
            result = function.apply(nativeHandle);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            int close = windowsRegFlushKey(nativeHandle);
            close = windowsRegCloseKey(nativeHandle);

        }
        return result;
    }


    @Override
    public Set<String> getNodes() {
        return handle((nativeHandle) -> {
            Set<String> result = new HashSet<>();
            int[] infoKeys = windowsRegQueryInfoKey(nativeHandle);
            if (infoKeys[ERROR_CODE] != ERROR_SUCCESS) {
                return result;
            }
            int maxKeyLength = infoKeys[MAX_KEY_LENGTH];
            int subKeysNumber = infoKeys[SUBKEYS_NUMBER];
            if (subKeysNumber == 0) {
                return result;
            }
            for (int i = 0; i < subKeysNumber; i++) {
                byte[] windowsName = windowsRegEnumKeyEx(nativeHandle, i, maxKeyLength + 1);
                if (windowsName == null) {
                    continue;
                }
                result.add(toJavaValueString(windowsName));
            }
            return result;
        });
    }

    @Override
    public boolean deleteNode(String subKey) {
        return Boolean.TRUE.equals(handle((nativeHandle) -> windowsRegDeleteKey(nativeHandle, stringToByteArray(subKey)) == SUBKEYS_NUMBER));
    }

    @Override
    public String getNode(String name) {
        return null;
    }

    @Override
    public boolean createNode(String name) {
        return Boolean.TRUE.equals(handle((nativeHandle) -> {
            int[] result = windowsRegCreateKeyEx(nativeHandle, stringToByteArray(name));
            if (result[ERROR_CODE] == ERROR_SUCCESS) {
                return result[DISPOSITION] == REG_CREATED_NEW_KEY;
            }
            return false;
        }));
    }

    public List<RegValue> getRegValue() {
        return handle((nativeHandle) -> {
            List<RegValue> result = new ArrayList<>();
            int[] infoKeys = windowsRegQueryInfoKey(nativeHandle);
            if (infoKeys[ERROR_CODE] != ERROR_SUCCESS) {
                return result;
            }
            int maxValueNameLength = infoKeys[MAX_VALUE_NAME_LENGTH];
            int valuesNumber = infoKeys[VALUES_NUMBER];
            if (valuesNumber == 0) {
                return result;
            }
            for (int i = 0; i < valuesNumber; i++) {
                byte[] windowsName = windowsRegEnumValue(nativeHandle, i, maxValueNameLength + 1);
                if (windowsName == null) {
                    continue;
                }
                byte[] valBytes = windowsRegQueryValueEx(nativeHandle, windowsName);
                RegValue regItem = RegTypeEnum.REG_SZ.of(toJavaValueString(windowsName), toJavaValueString(valBytes));
                result.add(regItem);
            }
            return result;
        });

    }

    public RegValue getRegValue(String regValueName) {
        return handle((nativeHandle) -> {
            byte[] valBytes = windowsRegQueryValueEx(nativeHandle, stringToByteArray(regValueName));
            return RegTypeEnum.REG_SZ.of(regValueName, toJavaValueString(valBytes));
        });
    }

    public boolean deleteRegValue(String regValueName) {
        return Boolean.TRUE.equals(handle((nativeHandle) -> windowsRegDeleteValue(nativeHandle, stringToByteArray(regValueName)) == SUBKEYS_NUMBER));
    }

    public boolean setRegValue(RegValue regValue) {
        return Boolean.TRUE.equals(handle((nativeHandle) -> {
            int result = windowsRegSetValueEx(nativeHandle, stringToByteArray(regValue.getName()), stringToByteArray(regValue.getValue()));
            return result == ERROR_SUCCESS;
        }));
    }
}
