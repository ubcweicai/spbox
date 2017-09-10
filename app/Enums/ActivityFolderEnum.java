package Enums;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

/**
 *
 */
public enum ActivityFolderEnum {
    VENTURE("创业"), OUTDOOR("户外"), FAMILY("亲子"), SPORTS("体育"), VACATION("度假"), CONCERT("演唱会"), VOLUNTEER("义工"), NON_PROFIT("公益"), SOCIAL("交友"), OTHERS("其他");

    private final String msg;

    ActivityFolderEnum(String msg){
        this.msg = msg;
    }

    public String getMsg(){
        return msg;
    }
}
