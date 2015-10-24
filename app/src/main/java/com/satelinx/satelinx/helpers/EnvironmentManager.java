package com.satelinx.satelinx.helpers;

/**
 * Created by jlh on 1/17/15.
 */
public class EnvironmentManager {

    public enum BuildType {
        PRODUCTION(0), DEVELOPMENT(1), TESTING(2);

        private int value;

        private BuildType(int value) {
            this.value = value;
        }
    }

    public static final String PRODUCTION_IP = "http://satelinx.com/api/v1";
    public static final String DEVELOPMENT_IP = "http://satelinx.com/api/v1";
    //public static final String DEVELOPMENT_IP = "http://192.168.2.92:3000/api/v1";

    private static final BuildType BUILD_TYPE = BuildType.PRODUCTION;

    public static boolean isDevelopment() {
        return BUILD_TYPE == BuildType.DEVELOPMENT;
    }

    public static boolean isTesting() {
        return BUILD_TYPE == BuildType.TESTING;
    }

    public static boolean isProduction() {
        return BUILD_TYPE == BuildType.PRODUCTION;
    }

    public static BuildType getBuildType() {
        return BUILD_TYPE;
    }

    public static String getIp() {
        if (EnvironmentManager.isDevelopment()) {
            return DEVELOPMENT_IP;
        }
        return PRODUCTION_IP;
    }
}
