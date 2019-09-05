package com.jnetdata.simple.demo.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.thenicesys.data.api.EntityId;
import org.thenicesys.fastjson.serializer.ToStringSerializer;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Administrator
 */
@Data
public class Customer implements EntityId<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    /**
     * keyword (ES类型)
     */
    private String name;

    /**
     * 试验各种映射
     */
    private String description;

    /**
     * pattern (ES类型)
     */
    private String categorys;

    /**
     * 服务城市
     */
    private String serviceCitys;

    /**
     * text (ES类型)
     */
    private String address;

    /**
     * date
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date birthDate;

    /**
     * 工龄
     */
    private Long workAge;

    /**
     * 年龄
     */
    private Long age;

    private Boolean leader;

}
