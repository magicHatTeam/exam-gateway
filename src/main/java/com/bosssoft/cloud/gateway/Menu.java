package com.bosssoft.cloud.gateway;

import java.util.List;

/**
 * @author likang
 * @date 2019/9/11 14:45
 */
public class Menu {

    private Long id;
    private String code;
    private String name;
    private String openImg;
    private Long parentId;
    private Byte status;
    private String url;

    private List children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenImg() {
        return openImg;
    }

    public void setOpenImg(String openImg) {
        this.openImg = openImg;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public List getChildren() {
        return children;
    }

    public void setChildren(List children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "MenuDto{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", openImg='" + openImg + '\'' +
                ", parentId=" + parentId +
                ", status=" + status +
                ", url='" + url + '\'' +
                ", children=" + children +
                '}';
    }
}
